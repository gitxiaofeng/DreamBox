package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.didi.carmate.dreambox.wrapper.Storage;
import com.google.gson.JsonObject;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBStorage extends DBAction {
    private Storage storage;

    private String rawKey;
    private String key;
    private String rawWrite;
    private String write;
    private String readTo;

    @DBDomAttr(key = "key")
    public void setRawKey(String rawKey) {
        this.rawKey = rawKey;
    }

    @DBDomAttr(key = "write")
    public void setRawWrite(String rawWrite) {
        this.rawWrite = rawWrite;
    }

    @DBDomAttr(key = "readTo")
    public void setReadTo(String readTo) {
        this.readTo = readTo;
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        storage = mDBContext.getWrapperImpl().storage();
    }

    @Override
    public boolean processChildAttr() {
        return false;
    }

    @Override
    public void doInvoke() {
        key = getVariableString(rawKey);
        write = getVariableString(rawWrite);
        invokeStorage();
    }

    @Override
    public void doInvoke(JsonObject dict) {
        key = getVariableString(rawKey, dict);
        write = getVariableString(rawWrite, dict);
        invokeStorage();
    }

    private void invokeStorage() {
        if (!DBUtils.isEmpty(write)) {
            storage.put(key, write);
        } else if (!DBUtils.isEmpty(readTo)) {
            String value = storage.get(key, "");
            mDBContext.putStringValue(readTo, value);
        } else {
            mDBContext.getLog().e("[write] or [readTo] all null is not expected");
        }
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBStorage createNode() {
            return new DBStorage();
        }
    }

    public static String getNodeTag() {
        return "storage";
    }
}