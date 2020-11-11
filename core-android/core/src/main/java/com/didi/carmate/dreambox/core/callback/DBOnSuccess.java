package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/5/8
 */
public class DBOnSuccess extends DBCallback {

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBOnSuccess createNode() {
            return new DBOnSuccess();
        }
    }

    public static String getNodeTag() {
        return "onSuccess";
    }
}
