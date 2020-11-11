package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/5/8
 */
public class DBOnNegative extends DBCallback {

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBOnNegative createNode() {
            return new DBOnNegative();
        }
    }

    public static String getNodeTag() {
        return "onNegative";
    }
}
