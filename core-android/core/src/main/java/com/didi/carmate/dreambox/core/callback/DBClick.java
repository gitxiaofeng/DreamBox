package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/5/9
 */
public class DBClick extends DBCallback {

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBClick createNode() {
            return new DBClick();
        }
    }

    public static String getNodeTag() {
        return "onClick";
    }
}
