package com.didi.carmate.dreambox.core.base;

import com.didi.carmate.dreambox.core.action.DBAction;
import com.didi.carmate.dreambox.core.action.DBTrace;
import com.didi.carmate.dreambox.core.bridge.DBSendEvent;
import com.didi.carmate.dreambox.core.data.DBMeta;
import com.didi.carmate.dreambox.core.render.DBBaseView;
import com.didi.carmate.dreambox.core.render.DBChildren;
import com.didi.carmate.dreambox.core.render.DBRender;
import com.didi.carmate.dreambox.core.render.DBTemplate;
import com.didi.carmate.dreambox.wrapper.Wrapper;
import com.didi.carmate.dreambox.wrapper.inner.WrapperMonitor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBNodeParser {
    private Map<Class, Map<String, Method>> methodsCache = new HashMap<>();
    private Map<String, String> templateMap = new HashMap<>();
    private Gson gson;
    private String accessKey;
    private String templateId;

    // 性能统计
    private Map<String, Integer> nodeStatistics = new HashMap<>(32);
    private Map<String, Integer> nodeTypeCount = new HashMap<>(32);

    public DBTemplate parser(String accessKey, String templateId, String jsonData) {
        this.accessKey = accessKey;
        this.templateId = templateId;
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapter(DBTemplate.class, new DBTemplateDeserializer())
                    .create();
        }
        // 初始化性能统计容器
        nodeStatistics.clear();
        nodeStatistics.put("node_count", 0);
        Map<String, String> params = new HashMap<>();
        params.put("file_size", String.valueOf(jsonData.length()));

        // 解析模板
        WrapperMonitor.ReportStart reportStart = Wrapper.get(accessKey).monitor()
                .start(templateId, DBConstants.TRACE_PARSER_TEMPLATE, WrapperMonitor.TRACE_NUM_ONCE);

        DBTemplate template = null;
        try {
            template = gson.fromJson(jsonData, DBTemplate.class);

            // 上报模板详细信息
            for (String key : nodeStatistics.keySet()) {
                params.put(key, String.valueOf(nodeStatistics.get(key)));
            }
            // 上报各节点个数
            if (nodeTypeCount.size() > 0) {
                StringBuilder b = new StringBuilder();
                boolean isFirst = true;
                for (String key : nodeTypeCount.keySet()) {
                    if (isFirst) {
                        b.append(key).append(":").append(nodeTypeCount.get(key));
                        isFirst = false;
                    } else {
                        b.append(",").append(key).append(":").append(nodeTypeCount.get(key));
                    }
                }
                params.put("nodes", b.toString());
            }
            reportStart.stop().addAll(params).report();

            template.setAccessKey(accessKey);
            template.setTemplateId(templateId);
        } catch (JsonSyntaxException e) {
            Wrapper.get(accessKey).log().e("json syntax error: " + jsonData);
//            Wrapper.get(accessKey).monitor().crash(e);
        }
        return template;
    }

    private class DBTemplateDeserializer implements JsonDeserializer<DBTemplate> {
        @Override
        public DBTemplate deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            DBTemplate rootNode = createRootNode(jsonObject);
            if (null == rootNode) {
                Wrapper.get(accessKey).log().e("template is null.");
            }
            return rootNode;
        }
    }

    private DBTemplate createRootNode(JsonObject jsonObject) {
        DBTemplate rootNode = null;
        Set<String> nodeNameSet = jsonObject.keySet();

        if (nodeNameSet.size() == 1 && nodeNameSet.contains(DBConstants.T_ROOT_NODE_NAME)) {
            Object dblObj = jsonObject.get(DBConstants.T_ROOT_NODE_NAME);
            rootNode = createDBTemplate(dblObj);
        } else if (nodeNameSet.size() == 2 &&
                nodeNameSet.contains(DBConstants.T_ROOT_NODE_NAME) &&
                nodeNameSet.contains(DBConstants.T_MAP_NODE_NAME)) {
            // 隐射表构建
            JsonObject mapObj = jsonObject.getAsJsonObject(DBConstants.T_MAP_NODE_NAME);
            for (Map.Entry<String, JsonElement> entry : mapObj.entrySet()) {
                templateMap.put(entry.getKey(), entry.getValue().getAsString());
            }
            // 模板根对象
            Object dblObj = jsonObject.getAsJsonObject(DBConstants.T_ROOT_NODE_NAME);
            rootNode = createDBTemplate(dblObj);
        } else {
            Wrapper.get(accessKey).log().e("root node must be only contains [dbl] or [dbl,map] node");
        }
        return rootNode;
    }

    private DBTemplate createDBTemplate(Object jsonObject) {
        DBTemplate rootNode = null;
        INodeCreator creator = DBNodeRegistry.getNodeMap().get(DBConstants.T_ROOT_NODE_NAME);
        if (null != creator) {
            rootNode = (DBTemplate) creator.createNode();
            loopCreateNode(jsonObject, rootNode, rootNode);
        } else {
            Wrapper.get(accessKey).log().e("node name[" + DBConstants.T_ROOT_NODE_NAME + "] should register before use it!");
            Map<String, String> map = new HashMap<>();
            map.put("node_name", DBConstants.T_ROOT_NODE_NAME);
            Wrapper.get(accessKey).monitor()
                    .report(templateId, DBConstants.TRACE_NODE_UNKNOWN, WrapperMonitor.TRACE_NUM_EVERY)
                    .addAll(map).report();
        }
        return rootNode;
    }

    private void loopCreateNode(Object jsonObj, IDBNode parentNode, DBTemplate rootNode) {
        if (jsonObj instanceof JsonArray) { // [render]节点array对象单独拿出来处理
            JsonArray objArray = (JsonArray) jsonObj;
            // render 和 children 数组节点，通过[type]来获取子节点类型
            if (parentNode instanceof DBRender || parentNode instanceof DBChildren) {
                for (int i = 0; i < objArray.size(); i++) {
                    JsonObject viewObject = (JsonObject) objArray.get(i);
                    String viewNodeName = viewObject.getAsJsonPrimitive(getProguardKey("type")).getAsString();
                    createNodeAndLoop(viewNodeName, viewObject, parentNode, rootNode);
                }
            } else { // 其他数组节点，通过[key]来获取子节点类型
                for (int i = 0; i < objArray.size(); i++) {
                    JsonObject element = (JsonObject) objArray.get(i);
                    String key = element.keySet().iterator().next(); // 获取子节点的key
                    createNodeAndLoop(key, element.get(key).getAsJsonObject(), parentNode, rootNode);
                }
            }
        } else if (jsonObj instanceof JsonObject) { // 如果为json对象
            JsonObject jsonObject = (JsonObject) jsonObj;
            Set<String> proguardKeySet = jsonObject.keySet();
            for (String proguardKey : proguardKeySet) {
                Object object = jsonObject.get(proguardKey);
                if (object instanceof JsonArray) { // 如果key的value是json数组
                    if (parentNode instanceof DBTrace) { // trace需特殊处理，attr碰撞出来的数组按照JsonArray处理
                        if ("attr".equals(getOriginKey(proguardKey))) {
                            DBTrace dbTrace = ((DBTrace) parentNode);
                            dbTrace.setJsonArray((JsonArray) object);
                        }
                    } else {
                        createNodeAndLoop(proguardKey, object, parentNode, rootNode);
                    }
                } else if (object instanceof JsonObject) { // 如果key的value是json对象
                    if (parentNode instanceof DBMeta) {
                        DBMeta dbMeta = ((DBMeta) parentNode);
                        dbMeta.addMetaData(rootNode, proguardKey, object);
                    } else if (parentNode instanceof DBSendEvent) {
                        // bridge [sendEvent][msg] 节点是kv数据源结构，类似于meta，需要特殊处理
                        if ("msg".equals(getOriginKey(proguardKey))) {
                            DBSendEvent dbSendEvent = ((DBSendEvent) parentNode);
                            dbSendEvent.setJsonObject((JsonObject) object);
                        } else {
                            createNodeAndLoop(proguardKey, object, parentNode, rootNode);
                        }
                    } else {
                        createNodeAndLoop(proguardKey, object, parentNode, rootNode);
                    }
                } else if (object instanceof JsonPrimitive) { // 如果key的value是json属性
                    JsonPrimitive jsonPrimitive = ((JsonPrimitive) object);
                    if (parentNode instanceof DBMeta) {
                        DBMeta dbMeta = ((DBMeta) parentNode); // meta nodeName 不混淆

                        if (jsonPrimitive.isString()) {
                            String value = jsonPrimitive.getAsString();
                            if (value.equals("true")) {
                                dbMeta.addMetaData(rootNode, proguardKey, true);
                            } else if (value.equals("false")) {
                                dbMeta.addMetaData(rootNode, proguardKey, false);
                            } else if (value.startsWith("{") && value.endsWith("}")) {
                                dbMeta.addMetaData(rootNode, proguardKey, gson.fromJson(value, JsonObject.class));
                            } else {
                                dbMeta.addMetaData(rootNode, proguardKey, value);
                            }
                        } else if (jsonPrimitive.isNumber()) {
                            dbMeta.addMetaData(rootNode, proguardKey, jsonPrimitive.getAsInt());
                        } else if (jsonPrimitive.isBoolean()) {
                            Wrapper.get(accessKey).log().w("dream box [boolean] type is present as [true] or [false] String");
                        } else {
                            Wrapper.get(accessKey).log().e("object: " + object);
                        }
                    } else {
                        attrParser(getOriginKey(proguardKey), jsonPrimitive, parentNode);
                    }
                } else {
                    Wrapper.get(accessKey).log().e("object: " + object);
                }
            }
        }
    }

    private void createNodeAndLoop(String proguardKey, Object object, IDBNode parentNode, DBTemplate rootNode) {
        INodeCreator creator = DBNodeRegistry.getNodeMap().get(getOriginKey(proguardKey));
        if (null != creator) {
            IDBNode node = creator.createNode();
            // 节点数据统计
            if (node instanceof DBAction || node instanceof DBBaseView) {
                statisticNode(nodeStatistics, nodeTypeCount, getOriginKey(proguardKey));
            }
            // 添加父节点
            node.setParentNode(parentNode);
            loopCreateNode(object, node, rootNode);
            // 添加child node
            parentNode.addChildNote(node);
        } else {
            Wrapper.get(accessKey).log().e("node name[" + getOriginKey(proguardKey) + "] should register before use it!");
            Map<String, String> map = new HashMap<>();
            map.put("parent_node", parentNode.toString());
            map.put("node_name", getOriginKey(proguardKey));
            Wrapper.get(accessKey).monitor()
                    .report(templateId, DBConstants.TRACE_NODE_UNKNOWN, WrapperMonitor.TRACE_NUM_EVERY)
                    .addAll(map).report();
        }
    }

    private void attrParser(String nodeName, JsonPrimitive jsonPrimitive, IDBNode parentNode) {
        try {
            Method method = findNodeMethod(nodeName, parentNode);
            if (null != method) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                List<Object> argsList = new ArrayList<>();
                for (Class type : parameterTypes) {
                    if (type == String.class) {
                        argsList.add((jsonPrimitive).getAsString());
                    } else if (type == int.class || type == Integer.class) {
                        argsList.add((jsonPrimitive).getAsInt());
                    }
                }
                method.invoke(parentNode, argsList.toArray());
            } else {
                Map<String, String> params = new HashMap<>();
                params.put("node_name", parentNode.toString());
                params.put("attr_name", nodeName);
                Wrapper.get(accessKey).monitor()
                        .report(templateId, DBConstants.TRACE_ATTR_UNKNOWN, WrapperMonitor.TRACE_NUM_ONCE)
                        .addAll(params).report();
            }
        } catch (NullPointerException e) {
            Wrapper.get(accessKey).log().e("can not find nodeName " + parentNode + "->attr " + nodeName);
            Wrapper.get(accessKey).monitor().crash(e);
        } catch (IllegalAccessException e) {
            Wrapper.get(accessKey).monitor().crash(e);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Wrapper.get(accessKey).monitor().crash(e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Wrapper.get(accessKey).monitor().crash(e);
            e.printStackTrace();
        }
    }

    private Method findNodeMethod(String attrName, IDBNode parentNode) {
        Method targetMethod;

        Class clazz = parentNode.getClass();
        Map<String, Method> methodsMap = methodsCache.get(clazz);
        if (null == methodsMap) {
            methodsMap = new HashMap<>();
            methodsCache.put(clazz, methodsMap);
        }
        targetMethod = methodsMap.get(attrName);
        if (null != targetMethod) {
            return targetMethod;
        } else {
            Method[] methods = parentNode.getClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(DBDomAttr.class)) {
                    DBDomAttr domAttr = method.getAnnotation(DBDomAttr.class);
                    if (domAttr.key().equals(attrName)) {
                        targetMethod = method;
                        methodsMap.put(attrName, method); // 缓存
                    }
                }
            }
            return targetMethod;
        }
    }

    private String getOriginKey(String proguardKey) {
        if (templateMap.containsKey(proguardKey)) {
            return templateMap.get(proguardKey);
        }
        return proguardKey;
    }

    private String getProguardKey(String originKey) {
        for (Map.Entry<String, String> entry : templateMap.entrySet()) {
            if (entry.getValue().equals(originKey)) {
                return entry.getKey();
            }
        }
        return originKey;
    }

    private void statisticNode(Map<String, Integer> statisticMap, Map<String, Integer> nodeTypeCount, String originKey) {
        statisticMap.put("node_count", statisticMap.get("node_count") + 1);
        Integer count = nodeTypeCount.get(originKey);
        if (null != count) {
            nodeTypeCount.put(originKey, count + 1);
        } else {
            nodeTypeCount.put(originKey, 1);
        }
    }
}
