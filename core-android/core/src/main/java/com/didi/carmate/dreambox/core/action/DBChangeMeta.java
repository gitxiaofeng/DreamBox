package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.google.gson.JsonObject;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBChangeMeta extends DBAction {
    private String key;
    private String rawValue;
    private String value;

    @DBDomAttr(key = "key")
    public void setKey(String key) {
        this.key = key;
    }

    @DBDomAttr(key = "value")
    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    @Override
    public void doInvoke() {
        if (!changeMeta()) {
            value = getVariableString(rawValue);
            mDBContext.changeStringValue(key, value);
        }
    }

    @Override
    public void doInvoke(JsonObject dict) {
        if (!changeMeta()) {
            value = getVariableString(rawValue, dict);
            mDBContext.changeStringValue(key, value);
        }
    }

    private boolean changeMeta() {
        if (DBUtils.isNumeric(rawValue)) {
            mDBContext.changeIntValue(key, Integer.parseInt(rawValue));
            return true;
        } else if ("true".equals(rawValue)) {
            mDBContext.changeBooleanValue(key, true);
            return true;
        } else if ("false".equals(rawValue)) {
            mDBContext.changeBooleanValue(key, false);
            return true;
        }
        return false;
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBChangeMeta createNode() {
            return new DBChangeMeta();
        }
    }

    public static String getNodeTag() {
        return "changeMeta";
    }
}