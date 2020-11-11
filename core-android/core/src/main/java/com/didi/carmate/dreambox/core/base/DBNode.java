package com.didi.carmate.dreambox.core.base;

import com.didi.carmate.dreambox.core.action.DBActionHelper;
import com.didi.carmate.dreambox.core.data.DBGlobalPool;
import com.didi.carmate.dreambox.core.render.DBChildren;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.didi.carmate.dreambox.wrapper.Wrapper;
import com.didi.carmate.dreambox.wrapper.inner.WrapperMonitor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * author: chenjing
 * date: 2020/4/30
 * <p>
 * 提供节点接口的默认实现，同时提供节点通用的处理方法，如数据源占位替换相关方法
 */
public abstract class DBNode implements IDBNode {

    protected DBActionPool mDBActionPool = new DBActionPool();
    protected DBContext mDBContext;

    private IDBNode parentNode;
    private List<IDBNode> childNodes;

    public void setParentNode(IDBNode parentNode) {
        this.parentNode = parentNode;
    }

    public IDBNode getParentNode() {
        return parentNode;
    }

    public void addChildNote(IDBNode childNode) {
        if (null == childNodes) {
            childNodes = new ArrayList<>();
        }
        childNodes.add(childNode);
    }

    public List<IDBNode> getChildNodes() {
        return childNodes;
    }

    public List<IDBNode> getChildren() {
        List<IDBNode> childNodes = getChildNodes();
        if (null == childNodes) {
            Wrapper.get(mDBContext.getAccessKey()).log().i(getClass().getSimpleName() + " -> child node is null.");
            return null;
        }
        for (IDBNode node : childNodes) {
            if (node instanceof DBChildren) {
                return node.getChildNodes();
            }
        }
        return null;
    }

    public <C> C getChild(Class<C> clazz) {
        List<IDBNode> childNodes = getChildren();
        if (null == childNodes) {
            Wrapper.get(mDBContext.getAccessKey()).log().e("[children] node should not be null.");
            return null;
        }
        for (IDBNode targetNode : childNodes) {
            if (null != targetNode && targetNode.getClass().equals(clazz)) {
                return (C) targetNode;
            }
        }
        return null;
    }

    public boolean isActionNode() {
        return false;
    }

    @Override
    public void preProcess(DBContext dbContext) {
        mDBContext = dbContext;

        if (preProcessChild()) {
            List<IDBNode> childNodes = getChildNodes();
            if (null != childNodes) {
                for (IDBNode node : childNodes) {
                    node.preProcess(dbContext);
                }
            }
        }
    }

    @Override
    public void processAttr() {
        if (processChildAttr()) {
            List<IDBNode> childNodes = getChildNodes();
            if (null != childNodes) {
                for (IDBNode node : childNodes) {
                    node.processAttr();
                }
            }
        }
    }

    @Override
    public void processAttr(JsonObject dict) {
        if (processChildAttr()) {
            List<IDBNode> childNodes = getChildNodes();
            if (null != childNodes) {
                for (IDBNode node : childNodes) {
                    node.processAttr(dict);
                }
            }
        }
    }

    @Override
    public void processAction() {
        if (processChildAction()) {
            List<IDBNode> childNodes = getChildNodes();
            if (null != childNodes) {
                for (IDBNode node : childNodes) {
                    node.processAction();
                }
            }
        }

        processEvent();
    }

    @Override
    public void processEvent() {
        // 防止action节点重复添加，每次添加前做清除操作
        mDBActionPool.release();
        DBActionHelper.addNodeActionToPool(this, mDBActionPool, mDBContext);
    }

    @Override
    public void release() {

    }

    /**
     * 可覆写此方法来决定是否递归调用本节点所有子节点[预处理]方法<br/>
     * 默认返回true
     *
     * @return false 截断递归调用本
     */
    public boolean preProcessChild() {
        return true;
    }

    /**
     * 可覆写此方法来决定是否递归调用本节点所有子节点[属性]处理方法<br/>
     * 默认返回true
     *
     * @return false 截断递归调用
     */
    public boolean processChildAttr() {
        return true;
    }

    /**
     * 可覆写此方法来决定是否递归调用本节点所有子节点[动作]处理方法<br/>
     * 默认返回true
     *
     * @return false 截断递归调用
     */
    public boolean processChildAction() {
        return true;
    }

    /**
     * 根据给定的key从data pool里拿String数据
     */
    protected String getVariableString(String variableKey) {
        if (DBUtils.isEmpty(variableKey)) {
            return "";
        }

        if (variableKey.startsWith("${") && variableKey.endsWith("}")) {
            String variable = variableKey.substring(2, variableKey.length() - 1);
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
                        Wrapper.get(mDBContext.getAccessKey()).log().e("[ext] node is empty, but use it in: [" + variableKey + "]");
                        if (DBUtils.isAppDebug(mDBContext.getApplication())) {
                            return variableKey;
                        } else {
                            reportParserDataFail();
                            return "";
                        }
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
                    return jsonPrimitive.getAsString();
                } else {
                    if (DBUtils.isAppDebug(mDBContext.getApplication())) {
                        return variableKey;
                    } else {
                        reportParserDataFail();
                        return "";
                    }
                }
            }
        }
        return variableKey;
    }

    /**
     * 根据给定的key从给定的字典数据源里拿String数据
     */
    protected String getVariableString(String variableKey, JsonObject dict) {
        if (DBUtils.isEmpty(variableKey)) {
            return "";
        }

        // 尝试从meta、global pool、ext里拿
        if (null == dict) {
            return getVariableString(variableKey);
        }

        if (variableKey.startsWith("${") && variableKey.endsWith("}")) {
            String variable = variableKey.substring(2, variableKey.length() - 1);

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
        return variableKey;
    }

    /**
     * 根据给定的key从data pool里拿boolean数据
     */
    protected boolean getVariableBoolean(String variableKey) {
        if (DBUtils.isEmpty(variableKey)) {
            return false;
        }

        if (variableKey.startsWith("${") && variableKey.endsWith("}")) {
            String variable = variableKey.substring(2, variableKey.length() - 1);

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
                        Wrapper.get(mDBContext.getAccessKey()).log().e("[ext] node is empty, but use it in: [" + variableKey + "]");
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
        return false;
    }

    /**
     * 根据给定的key从给定的字典数据源里拿String数据
     */
    protected boolean getVariableBoolean(String variableKey, JsonObject dict) {
        if (DBUtils.isEmpty(variableKey)) {
            return false;
        }

        // 尝试从meta、global pool、ext里拿
        if (null == dict) {
            return getVariableBoolean(variableKey);
        }

        if (variableKey.startsWith("${") && variableKey.endsWith("}")) {
            String variable = variableKey.substring(2, variableKey.length() - 1);

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
        return false;
    }

    /**
     * 根据给定的key从data pool里拿int数据
     */
    protected int getVariableInt(String variableKey) {
        if (DBUtils.isEmpty(variableKey)) {
            return -1;
        }

        if (variableKey.startsWith("${") && variableKey.endsWith("}")) {
            String variable = variableKey.substring(2, variableKey.length() - 1);
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
                        Wrapper.get(mDBContext.getAccessKey()).log().e("[ext] node is empty, but use it in: [" + variableKey + "]");
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
    protected int getVariableInt(String variableKey, JsonObject dict) {
        if (DBUtils.isEmpty(variableKey)) {
            return -1;
        }

        // 尝试从meta、global pool、ext里拿
        if (null == dict) {
            return getVariableInt(variableKey);
        }

        if (variableKey.startsWith("${") && variableKey.endsWith("}")) {
            String variable = variableKey.substring(2, variableKey.length() - 1);

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
    protected JsonObject getVariableJsonObject(String variableKey) {
        if (DBUtils.isEmpty(variableKey)) {
            return null;
        }

        if (variableKey.startsWith("${") && variableKey.endsWith("}")) {
            String variable = variableKey.substring(2, variableKey.length() - 1);

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
    protected JsonObject getVariableJsonObject(String variableKey, JsonObject dict) {
        if (DBUtils.isEmpty(variableKey) || null == dict) {
            return null;
        }

        if (variableKey.startsWith("${") && variableKey.endsWith("}")) {
            String variable = variableKey.substring(2, variableKey.length() - 1);

            String[] keys = variable.split("\\.");
            if (keys.length == 1) {
                return dict.getAsJsonObject(keys[0]);
            } else {
                return getNestJsonObject(keys, dict.getAsJsonObject(keys[0]));
            }
        }
        return null;
    }

    protected List<JsonObject> getVariableList(String variableKey) {
        List<JsonObject> jsonObjects = new ArrayList<>();

        if (!DBUtils.isEmpty(variableKey) && variableKey.startsWith("${") && variableKey.endsWith("}")) {
            JsonArray jsonArray;
            String variable = variableKey.substring(2, variableKey.length() - 1);
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
