package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.wrapper.Toast;
import com.google.gson.JsonObject;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBToast extends DBAction {
    private Toast toast;

    private String rawSrc;
    private String rawLong;
    private String src;
    private boolean isLong;

    @DBDomAttr(key = "src")
    public void setRawSrc(String rawSrc) {
        this.rawSrc = rawSrc;
    }

    @DBDomAttr(key = "long")
    public void setRawLong(String rawLong) {
        this.rawLong = rawLong;
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        toast = mDBContext.getWrapperImpl().toast();
    }

    @Override
    public void doInvoke() {
        if (null != rawSrc) {
            src = getVariableString(rawSrc);
        } else {
            src = "";
        }
        isLong = "true".equals(rawLong);

        toast.show(mDBContext.getContext(), src, isLong);
    }

    @Override
    public void doInvoke(JsonObject dict) {
        if (null != rawSrc) {
            src = getVariableString(rawSrc, dict);
        } else {
            src = "";
        }
        isLong = "true".equals(rawLong);

        toast.show(mDBContext.getContext(), src, isLong);
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBToast createNode() {
            return new DBToast();
        }
    }

    public static String getNodeTag() {
        return "toast";
    }
}