package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.wrapper.Log;
import com.google.gson.JsonObject;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBLog extends DBAction {
    private static final String TAG = "DBLog";

    private Log dbLog;

    private String level;
    private String tag;
    private String rawMsg;
    private String msg;

    @DBDomAttr(key = "level")
    public void setLevel(String level) {
        this.level = level;
    }

    @DBDomAttr(key = "tag")
    public void setTag(String tag) {
        this.tag = tag;
    }

    @DBDomAttr(key = "msg")
    public void setRawMsg(String rawMsg) {
        this.rawMsg = rawMsg;
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        dbLog = mDBContext.getWrapperImpl().log();
    }

    @Override
    public void doInvoke() {
        if (null != rawMsg) {
            msg = getVariableString(rawMsg);
        } else {
            msg = "";
        }
        printLog();
    }

    @Override
    public void doInvoke(JsonObject dict) {
        if (null != rawMsg) {
            msg = getVariableString(rawMsg, dict);
        } else {
            msg = "";
        }
        printLog();
    }

    private void printLog() {
        switch (level) {
            case DBConstants.LOG_LEVEL_E:
                dbLog.e("tag: " + tag + " msg: " + msg);
                break;
            case DBConstants.LOG_LEVEL_W:
                dbLog.w("tag: " + tag + " msg: " + msg);
                break;
            case DBConstants.LOG_LEVEL_I:
                dbLog.i("tag: " + tag + " msg: " + msg);
                break;
            case DBConstants.LOG_LEVEL_D:
                dbLog.d("tag: " + tag + " msg: " + msg);
                break;
            case DBConstants.LOG_LEVEL_V:
                dbLog.v("tag: " + tag + " msg: " + msg);
                break;
            default:
                break;
        }
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBLog createNode() {
            return new DBLog();
        }
    }

    public static String getNodeTag() {
        return "log";
    }
}