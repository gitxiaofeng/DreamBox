package com.didi.carmate.dreambox.core.bridge;

import com.didi.carmate.dreambox.core.action.IDBAction;
import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/7/15
 */
public class DBSendEvent extends DBEvent {
    private String eid;
    private JsonObject jsonObject;
    private DBBridgeDBToNativeCallback callback;
    private DBBridgeHandler bridgeHandler;

    @DBDomAttr(key = "eid")
    public void setEid(String eid) {
        this.eid = eid;
    }

    public void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public boolean preProcessChild() {
        return false; // 截断子节点树形遍历逻辑
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        bridgeHandler = mDBContext.getBridgeHandler();
        List<IDBNode> childNodes = getChildNodes();
        if (null != childNodes && childNodes.size() > 0) {
            for (IDBNode node : childNodes) {
                if (node instanceof DBBridgeDBToNativeCallback) {
                    callback = (DBBridgeDBToNativeCallback) node;
                }
                node.preProcess(dbContext);
            }
        }
    }

    @Override
    protected void doInvoke() {
        if (null == callback) {
            bridgeHandler.dbInvokeNative(this, eid, jsonObject);
        } else {
            bridgeHandler.dbInvokeNative(this, eid, jsonObject, callback);
        }
    }

    public void onCallback() {
        List<IDBAction> actionList = mDBActionPool.getBridgeCallbackDBToNAction();
        if (null != actionList) {
            for (IDBAction action : actionList) {
                action.invoke();
            }
        }
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBSendEvent createNode() {
            return new DBSendEvent();
        }
    }

    public static String getNodeTag() {
        return "sendEvent";
    }
}
