package com.didi.carmate.dreambox.core.base;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.didi.carmate.dreambox.core.data.DBGlobalPool;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.didi.carmate.dreambox.wrapper.Wrapper;
import com.didi.carmate.dreambox.wrapper.inner.WrapperMonitor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author: chenjing
 * date: 2020/4/30
 * 提供节点接口的默认实现，同时提供节点通用的处理方法，如数据源占位替换相关方法<br/><br/>
 *
 * <h2>生命周期定义</h2>
 * <p>callback</p>
 * protected void onParserAttribute(Map<String, String> attrs);<br/>
 * protected void onParserNode();<br/>
 * protected void onParserNodeFinished(Map<String, String> attrs);<br/>
 *
 * <p>action</p>
 * protected void onParserAttribute(Map<String, String> attrs);<br/>
 * protected void onParserNode();<br/>
 * protected void onParserNodeFinished(Map<String, String> attrs);<br/>
 * ------------------------<br/>
 * doInvoke(Map<String, String> attrs);<br/>
 *
 * <p>view</p>
 * protected void onParserAttribute(Map<String, String> attrs);<br/>
 * protected void onParserNode();<br/>
 * protected void onParserNodeFinished(Map<String, String> attrs);<br/>
 * ------callback和action节点作为视图节点的基础能力，需要先解析完毕再开始自己特有的生命周期-----<br/>
 * protected abstract V onCreateView();<br/>
 * protected void onAttributesBind(V selfView, Map<String, String> attrs);<br/>
 * protected void onCallbackBind(V selfView, List<DBCallback> callbacks);
 */
public abstract class DBNode implements IDBNode {
    // 节点通过[type]字段来标识类型，如：text/image/onClick/cell
    public static final String KEY_NODE_TYPE = "type";

    private String tagName;
    private final Map<String, String> attrs = new HashMap<>();
    private IDBNode parent;
    private final List<IDBNode> children = new ArrayList<>();
    private JsonObject data;

    protected DBContext mDBContext;

    protected DBNode(DBContext dbContext) {
        this.mDBContext = dbContext;
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * 获取节点所有属性，禁止外部类直接访问
     */
    Map<String, String> getAttrs() {
        return attrs;
    }

    @Override
    public void putAttr(String key, String value) {
        attrs.put(key, value);
    }

    @Override
    public void setParent(IDBNode parent) {
        this.parent = parent;
    }

    @Override
    public IDBNode getParent() {
        return parent;
    }

    @Override
    public void addChild(IDBNode child) {
        children.add(child);
    }

    @Override
    public List<IDBNode> getChildren() {
        return children;
    }

    @CallSuper
    @Override
    public void parserAttribute() {
        List<IDBNode> children = getChildren();
        for (IDBNode child : children) {
            child.parserAttribute();
        }

        onParserAttribute(getAttrs());
    }

    @CallSuper
    @Override
    public void parserNode() {
        List<IDBNode> children = getChildren();
        for (IDBNode child : children) {
            child.parserNode();
        }

        onParserNode();
    }

    @CallSuper
    @Override
    public void finishParserNode() {
        List<IDBNode> children = getChildren();
        for (IDBNode child : children) {
            child.finishParserNode();
        }

        onParserNodeFinished();
    }

    @CallSuper
    protected void onParserAttribute(Map<String, String> attrs) {

    }

    @CallSuper
    protected void onParserNode() {

    }

    @CallSuper
    protected void onParserNodeFinished() {

    }

    @Override
    public void setData(JsonObject data) {
        List<IDBNode> children = getChildren();
        for (IDBNode child : children) {
            child.setData(data);
        }
        this.data = data;
    }

    @Override
    public JsonObject getData() {
        return data;
    }

    @Override
    public void release() {

    }

    /**
     * 根据给定的key拿String数据
     */
    protected String getString(String rawKey) {
        if (DBUtils.isEmpty(rawKey)) {
            return "";
        }

        Pattern p = Pattern.compile("\\$\\{[\\w\\.]+\\}");
        Matcher m = p.matcher(rawKey);
        List<String> variableList = new ArrayList<>();
        while (m.find()) {
            if (!variableList.contains(m.group())) {
                variableList.add(m.group());
            }
        }
        for (int i = 0; i < variableList.size(); i++) {
            rawKey = rawKey.replace(variableList.get(i), getVariableString(variableList.get(i)));
        }
        return rawKey;
    }

    protected String getVariableString(String rawKey) {
        // 尝试从提供的数据源里拿
        if (null != data) {
            return getVariableString(rawKey, data);
        }

        // 尝试从meta、global pool、ext里拿
        if (rawKey.startsWith("${") && rawKey.endsWith("}")) {
            String variable = rawKey.substring(2, rawKey.length() - 1);
            String[] keys = variable.split("\\.");
            if (keys.length == 1) {
                return mDBContext.getStringValue(variable);
            } else {
                // 全局对象池只支持简单的KV对，不支持多级对象
                if (keys[0].equals(DBConstants.DATA_GLOBAL_PREFIX)) {
                    return DBGlobalPool.get(mDBContext.getAccessKey()).getData(keys[1], String.class);
                }

                JsonPrimitive jsonPrimitive = null;
                if (keys[0].equals(DBConstants.DATA_EXT_PREFIX)) {
                    JsonObject ext = mDBContext.getJsonValue(DBConstants.DATA_EXT_PREFIX);
                    if (null == ext) {
                        Wrapper.get(mDBContext.getAccessKey()).log().e("[ext] node is empty, but use it in: [" + rawKey + "]");
                        if (Wrapper.getInstance().debug) {
                            return rawKey;
                        } else {
                            reportParserDataFail();
                            return "";
                        }
                    }

                    if (keys.length == 2) {
                        JsonElement extValue = ext.get(keys[1]);
                        if (extValue != null && extValue.isJsonPrimitive()) {
                            jsonPrimitive = extValue.getAsJsonPrimitive();
                        }
                    } else {
                        String[] keysExt = new String[keys.length - 1];
                        System.arraycopy(keys, 1, keysExt, 0, keysExt.length);
                        jsonPrimitive = getNestJsonPrimitive(keysExt, ext.getAsJsonObject(keysExt[0]));
                    }
                } else {
                    jsonPrimitive = getNestJsonPrimitive(keys, mDBContext.getJsonValue(keys[0]));
                }
                if (null != jsonPrimitive) {
                    return jsonPrimitive.getAsString();
                } else {
                    if (Wrapper.getInstance().debug) {
                        return rawKey;
                    } else {
                        reportParserDataFail();
                        return "";
                    }
                }
            }
        }
        return rawKey;
    }

    /**
     * 根据给定的key从给定的字典数据源里拿String数据
     */
    private String getVariableString(String rawKey, @NonNull JsonObject dict) {
        if (rawKey.startsWith("${") && rawKey.endsWith("}")) {
            String variable = rawKey.substring(2, rawKey.length() - 1);

            String[] keys = variable.split("\\.");
            JsonPrimitive jsonPrimitive;
            if (keys.length == 1) {
                jsonPrimitive = dict.getAsJsonPrimitive(variable);
            } else {
                jsonPrimitive = getNestJsonPrimitive(keys, dict.getAsJsonObject(keys[0]));
            }
            if (jsonPrimitive != null) {
                return jsonPrimitive.getAsString();
            }
        }
        return rawKey;
    }

    /**
     * 根据给定的key拿boolean数据
     */
    protected boolean getBoolean(String rawKey) {
        if (DBUtils.isEmpty(rawKey)) {
            return false;
        }

        // 尝试从提供的数据源里拿
        if (null != data) {
            return getBoolean(rawKey, data);
        }

        // 尝试从meta、global pool、ext里拿
        if (rawKey.startsWith("${") && rawKey.endsWith("}")) {
            String variable = rawKey.substring(2, rawKey.length() - 1);

            String[] keys = variable.split("\\.");
            if (keys.length == 1) {
                return mDBContext.getBooleanValue(variable);
            } else {
                // 全局对象池只支持简单的KV对，不支持多级对象
                if (keys[0].equals(DBConstants.DATA_GLOBAL_PREFIX)) {
                    return DBGlobalPool.get(mDBContext.getAccessKey()).getData(keys[1], Boolean.class);
                }

                JsonPrimitive jsonPrimitive = null;
                if (keys[0].equals(DBConstants.DATA_EXT_PREFIX)) {
                    JsonObject ext = mDBContext.getJsonValue(DBConstants.DATA_EXT_PREFIX);
                    if (null == ext) {
                        Wrapper.get(mDBContext.getAccessKey()).log().e("[ext] node is empty, but use it in: [" + rawKey + "]");
                        return false;
                    }
                    if (keys.length == 2) {
                        JsonElement extValue = ext.get(keys[1]);
                        if (extValue.isJsonPrimitive()) {
                            jsonPrimitive = extValue.getAsJsonPrimitive();
                        }
                    } else {
                        String[] keysExt = new String[keys.length - 1];
                        System.arraycopy(keys, 1, keysExt, 0, keysExt.length);
                        jsonPrimitive = getNestJsonPrimitive(keysExt, ext.getAsJsonObject(keysExt[0]));
                    }
                } else {
                    jsonPrimitive = getNestJsonPrimitive(keys, mDBContext.getJsonValue(keys[0]));
                }
                if (null != jsonPrimitive) {
                    return jsonPrimitive.getAsString().equals("true");
                }
            }
        }
        return "true".equals(rawKey);
    }

    /**
     * 根据给定的key从给定的字典数据源里拿String数据
     */
    protected boolean getBoolean(String rawKey, JsonObject dict) {
        if (rawKey.startsWith("${") && rawKey.endsWith("}")) {
            String variable = rawKey.substring(2, rawKey.length() - 1);

            String[] keys = variable.split("\\.");
            JsonPrimitive jsonPrimitive;
            if (keys.length == 1) {
                jsonPrimitive = dict.getAsJsonPrimitive(variable);
            } else {
                jsonPrimitive = getNestJsonPrimitive(keys, dict);
            }
            if (null != jsonPrimitive) {
                return jsonPrimitive.getAsString().equals("true");
            }
        }
        return "true".equals(rawKey);
    }

    /**
     * 根据给定的key从data pool里拿int数据
     */
    protected int getInt(String rawKey) {
        if (DBUtils.isEmpty(rawKey)) {
            return -1;
        }

        if (DBUtils.isNumeric(rawKey)) {
            return Integer.parseInt(rawKey);
        }

        // 尝试从提供的数据源里拿
        if (null != data) {
            return getInt(rawKey, data);
        }

        // 尝试从meta、global pool、ext里拿
        if (rawKey.startsWith("${") && rawKey.endsWith("}")) {
            String variable = rawKey.substring(2, rawKey.length() - 1);
            String[] keys = variable.split("\\.");
            if (keys.length == 1) {
                return mDBContext.getIntValue(variable);
            } else {
                // 全局对象池只支持简单的KV对，不支持多级对象
                if (keys[0].equals(DBConstants.DATA_GLOBAL_PREFIX)) {
                    return DBGlobalPool.get(mDBContext.getAccessKey()).getData(keys[1], Integer.class);
                }

                JsonPrimitive jsonPrimitive = null;
                if (keys[0].equals(DBConstants.DATA_EXT_PREFIX)) {
                    JsonObject ext = mDBContext.getJsonValue(DBConstants.DATA_EXT_PREFIX);
                    if (null == ext) {
                        Wrapper.get(mDBContext.getAccessKey()).log().e("[ext] node is empty, but use it in: [" + rawKey + "]");
                        return -1;
                    }
                    if (keys.length == 2) {
                        JsonElement extValue = ext.get(keys[1]);
                        if (extValue.isJsonPrimitive()) {
                            jsonPrimitive = extValue.getAsJsonPrimitive();
                        }
                    } else {
                        String[] keysExt = new String[keys.length - 1];
                        System.arraycopy(keys, 1, keysExt, 0, keysExt.length);
                        jsonPrimitive = getNestJsonPrimitive(keysExt, ext.getAsJsonObject(keysExt[0]));
                    }
                } else {
                    jsonPrimitive = getNestJsonPrimitive(keys, mDBContext.getJsonValue(keys[0]));
                }
                if (null != jsonPrimitive) {
                    return jsonPrimitive.getAsInt();
                }
            }
        }
        return -1;
    }

    /**
     * 根据给定的key从data pool里拿int数据
     */
    protected int getInt(String rawKey, JsonObject dict) {
        if (rawKey.startsWith("${") && rawKey.endsWith("}")) {
            String variable = rawKey.substring(2, rawKey.length() - 1);

            String[] keys = variable.split("\\.");
            JsonPrimitive jsonPrimitive;
            if (keys.length == 1) {
                jsonPrimitive = dict.getAsJsonPrimitive(variable);
            } else {
                jsonPrimitive = getNestJsonPrimitive(keys, dict.getAsJsonObject(keys[0]));
            }
            if (null != jsonPrimitive) {
                return jsonPrimitive.getAsInt();
            }
        }
        return -1;
    }

    /**
     * 根据给定的key从meta数据源里拿json对象
     */
    protected JsonObject getJsonObject(String rawKey) {
        if (DBUtils.isEmpty(rawKey)) {
            return null;
        }

        if (rawKey.startsWith("${") && rawKey.endsWith("}")) {
            String variable = rawKey.substring(2, rawKey.length() - 1);

            String[] keys = variable.split("\\.");
            if (keys.length == 1) {
                return mDBContext.getJsonValue(variable);
            } else {
                return getNestJsonObject(keys, mDBContext.getJsonValue(keys[0]));
            }
        }
        return null;
    }

    /**
     * 根据给定的key从dict数据源里拿json对象
     */
    protected JsonObject getJsonObject(String rawKey, JsonObject dict) {
        if (DBUtils.isEmpty(rawKey) || null == dict) {
            return null;
        }

        if (rawKey.startsWith("${") && rawKey.endsWith("}")) {
            String variable = rawKey.substring(2, rawKey.length() - 1);

            String[] keys = variable.split("\\.");
            if (keys.length == 1) {
                return dict.getAsJsonObject(keys[0]);
            } else {
                return getNestJsonObject(keys, dict.getAsJsonObject(keys[0]));
            }
        }
        return null;
    }

    protected List<JsonObject> getJsonObjectList(String rawKey) {
        List<JsonObject> jsonObjects = new ArrayList<>();

        if (!DBUtils.isEmpty(rawKey) && rawKey.startsWith("${") && rawKey.endsWith("}")) {
            JsonArray jsonArray;
            String variable = rawKey.substring(2, rawKey.length() - 1);
            String[] keys = variable.split("\\.");
            if (keys.length == 1) {
                jsonArray = mDBContext.getJsonArray(variable);
            } else {
                jsonArray = getNestJsonArray(keys, mDBContext.getJsonValue(keys[0]));
            }
            // 添加到数组
            if (jsonArray != null) {
                for (JsonElement jsonElement : jsonArray) {
                    if (jsonElement instanceof JsonObject) {
                        jsonObjects.add((JsonObject) jsonElement);
                    }
                }
            }
        }
        return jsonObjects;
    }

    private JsonArray getNestJsonArray(String[] keys, JsonObject jsonObject) {
        String lastKey = keys[keys.length - 1]; // 最后一个key用来获取JsonArray对象
        String[] prefixKeys = Arrays.copyOf(keys, keys.length - 1); // 前面n-1个key用来获取JsonObject对象
        int i = 1;
        StringBuilder tmpKeys = new StringBuilder(prefixKeys[0]);
        while (i < prefixKeys.length) {
            if (null == jsonObject) {
                return null;
            }
            tmpKeys.append(".").append(prefixKeys[i]);
            JsonElement jsonElement = jsonObject.get(prefixKeys[i]);
            if (jsonElement instanceof JsonObject) {
                jsonObject = (JsonObject) jsonElement;
            } else {
                throw new IllegalArgumentException("[" + tmpKeys.toString() + "]" + " must be a [JsonObject] in json data");
            }
            i++;
        }
        if (jsonObject == null) {
            return null;
        }
        return jsonObject.getAsJsonArray(lastKey);
    }

    private JsonPrimitive getNestJsonPrimitive(String[] keys, JsonObject jsonObject) {
        String lastKey = keys[keys.length - 1]; // 最后一个key用来获取字符串对象
        String[] prefixKeys = Arrays.copyOf(keys, keys.length - 1); // 前面n-1个key用来获取JsonObject对象
        int i = 1;
        StringBuilder tmpKeys = new StringBuilder(prefixKeys[0]);
        while (i < prefixKeys.length) {
            if (null == jsonObject) {
                return null;
            }
            tmpKeys.append(".").append(prefixKeys[i]);
            // type check
            JsonElement jsonElement = jsonObject.get(prefixKeys[i]);
            if (jsonElement instanceof JsonObject) {
                jsonObject = (JsonObject) jsonElement;
            } else {
                Wrapper.get(mDBContext.getAccessKey()).log().e("[" + tmpKeys.toString() + "]" + " must be a [JsonObject] in json data");
                return null;
            }
            i++;
        }
        if (jsonObject == null) {
            return null;
        }

        tmpKeys.append(".").append(lastKey);
        // last element type check
        JsonElement jsonElement = jsonObject.get(lastKey);
        if (jsonElement instanceof JsonPrimitive) {
            return (JsonPrimitive) jsonElement;
        } else {
            Wrapper.get(mDBContext.getAccessKey()).log().e("[" + tmpKeys.toString() + "]" + " must be a [JsonObject] in json data");
            return null;
        }
    }

    private JsonObject getNestJsonObject(String[] keys, JsonObject jsonObject) {
        int i = 1;
        StringBuilder tmpKeys = new StringBuilder(keys[0]);
        while (i < keys.length) {
            if (null == jsonObject) {
                return null;
            }
            tmpKeys.append(".").append(keys[i]);
            // type check
            JsonElement jsonElement = jsonObject.get(keys[i]);
            if (jsonElement instanceof JsonObject) {
                jsonObject = (JsonObject) jsonElement;
            } else {
                Wrapper.get(mDBContext.getAccessKey()).log().e("[" + tmpKeys.toString() + "]" + " must be a [JsonObject] in json data");
                return null;
            }
            i++;
        }
        return jsonObject;
    }

    private void reportParserDataFail() {
        Wrapper.get(mDBContext.getAccessKey()).monitor().report(
                mDBContext.getTemplateId(),
                DBConstants.TRACE_PARSER_DATA_FAIL,
                WrapperMonitor.TRACE_NUM_ONCE
        );
    }
}
