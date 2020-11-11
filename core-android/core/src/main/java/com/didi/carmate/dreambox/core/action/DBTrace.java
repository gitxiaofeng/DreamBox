package com.didi.carmate.dreambox.core.action;

import android.text.TextUtils;

import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.DBNode;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.wrapper.inner.WrapperTrace;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBTrace extends DBAction {
    private String rawKey;
    private String key;
    private JsonArray jsonArray;
    private List<TraceAttr> attrs = new ArrayList<>();

    @DBDomAttr(key = "key")
    public void setRawKey(String rawKey) {
        this.rawKey = rawKey;
    }

    @DBDomAttr(key = "attr")
    public void addAttr(TraceAttr traceAttr) {
        attrs.add(traceAttr);
    }

    public void setJsonArray(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        // 多个attr属性，碰撞为JsonArray的处理
        if (null != jsonArray) {
            for (JsonElement node : jsonArray) {
                if (node.isJsonObject()) {
                    JsonObject jsonObject = (JsonObject) node;
                    String key = jsonObject.get("key").getAsString();
                    String value = jsonObject.get("value").getAsString();
                    if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                        attrs.add(new TraceAttr(dbContext, key, value));
                    }
                }
            }
        } else {
            // 单个attr属性处理
            List<IDBNode> childNodes = getChildNodes();
            if (null != childNodes) {
                for (IDBNode node : childNodes) {
                    attrs.add((TraceAttr) node);
                }
            }
        }
    }

    @Override
    public void doInvoke() {
        key = getVariableString(rawKey);

        WrapperTrace.TraceAdder traceAdder = mDBContext.getWrapperImpl().trace().addTrace(key);
        for (int i = 0; i < attrs.size(); i++) {
            TraceAttr attr = attrs.get(i);
            attr.doInvoke();
            traceAdder.add(attr.key, attr.value);
        }
        traceAdder.report();
    }

    @Override
    public void doInvoke(JsonObject dict) {
        key = getVariableString(rawKey, dict);

        WrapperTrace.TraceAdder traceAdder = mDBContext.getWrapperImpl().trace().addTrace(key);
        for (int i = 0; i < attrs.size(); i++) {
            TraceAttr attr = attrs.get(i);
            attr.doInvoke(dict);
            traceAdder.add(attr.key, attr.value);
        }
        traceAdder.report();
    }

    public static String getNodeTag() {
        return "trace";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBTrace createNode() {
            return new DBTrace();
        }
    }

    public static class TraceAttr extends DBNode {
        private String rawKey;
        private String rawValue;
        private String key;
        private String value;

        @DBDomAttr(key = "key")
        public void setRawKey(String rawKey) {
            this.rawKey = rawKey;
        }

        @DBDomAttr(key = "value")
        public void setRawValue(String rawValue) {
            this.rawValue = rawValue;
        }

        TraceAttr() {
        }

        TraceAttr(DBContext dbContext, String rawKey, String rawValue) {
            this.mDBContext = dbContext;
            this.rawKey = rawKey;
            this.rawValue = rawValue;
        }

        public void doInvoke() {
            key = getVariableString(rawKey);
            value = getVariableString(rawValue);
        }

        public void doInvoke(JsonObject dict) {
            key = getVariableString(rawKey, dict);
            value = getVariableString(rawValue, dict);
        }

        public static class NodeCreator implements INodeCreator {
            @Override
            public TraceAttr createNode() {
                return new TraceAttr();
            }
        }

        public static String getNodeTag() {
            return "attr";
        }
    }
}
