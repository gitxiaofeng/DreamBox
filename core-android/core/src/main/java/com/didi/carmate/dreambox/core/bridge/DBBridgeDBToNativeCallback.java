package com.didi.carmate.dreambox.core.bridge;

import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/7/23
 */
public class DBBridgeDBToNativeCallback extends DBEvent {
    private String msgTo;

    @DBDomAttr(key = "msgTo")
    public void setMsgTo(String msgTo) {
        this.msgTo = msgTo;
    }

    public String getMsgTo() {
        return msgTo;
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBBridgeDBToNativeCallback createNode() {
            return new DBBridgeDBToNativeCallback();
        }
    }

    public static String getNodeTag() {
        return "callback";
    }
}
