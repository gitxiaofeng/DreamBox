package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.callback.IDBCallback;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBAlias extends DBAction implements IDBCallback {
    private String id;
    private String src;

    @DBDomAttr(key = "id")
    public void setId(String id) {
        this.id = id;
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

    public String getId() {
        return id;
    }

    @Override
    public List<IDBNode> getActions() {
        final List<IDBNode> childNotes = getChildNodes();
        if (null != childNotes && childNotes.size() > 0) {
            return childNotes.get(0).getChildNodes();
        }
        return null;
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBAlias createNode() {
            return new DBAlias();
        }
    }

    public static String getNodeTag() {
        return "actionAlias";
    }
}
