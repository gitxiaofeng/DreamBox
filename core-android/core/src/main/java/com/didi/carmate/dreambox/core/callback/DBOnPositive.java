package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/5/8
 */
public class DBOnPositive extends DBCallback {

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBOnPositive createNode() {
            return new DBOnPositive();
        }
    }

    public static String getNodeTag() {
        return "onPositive";
    }
}
