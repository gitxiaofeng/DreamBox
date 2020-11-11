package com.didi.carmate.dreambox.core.bridge;

import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/7/15
 */
public class DBOnEvent extends DBEvent {
    private String eid;
    private String msgTo;

    @DBDomAttr(key = "eid")
    public void setEid(String eid) {
        this.eid = eid;
    }

    @DBDomAttr(key = "msgTo")
    public void setMsgTo(String msgTo) {
        this.msgTo = msgTo;
    }

    public String getEid() {
        return eid;
    }

    public String getMsgTo() {
        return msgTo;
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBOnEvent createNode() {
            return new DBOnEvent();
        }
    }

    public static String getNodeTag() {
        return "onEvent";
    }
}
