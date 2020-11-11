package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/5/8
 */
public class DBOnError extends DBCallback {

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBOnError createNode() {
            return new DBOnError();
        }
    }

    public static String getNodeTag() {
        return "onError";
    }
}
