package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.google.gson.JsonObject;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBInvoke extends DBAction {

    private String alias;
    private String src;

    @DBDomAttr(key = "alias")
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @DBDomAttr(key = "src")
    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrc() {
        return src;
    }

    @Override
    public void doInvoke() {
    }

    @Override
    public void doInvoke(JsonObject dict) {
    }

    String getAlias() {
        return alias;
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBInvoke createNode() {
            return new DBInvoke();
        }
    }

    public static String getNodeTag() {
        return "invoke";
    }
}