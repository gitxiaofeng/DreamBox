package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/5/8
 */
public class DBInVisible extends DBCallback {

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBInVisible createNode() {
            return new DBInVisible();
        }
    }

    public static String getNodeTag() {
        return "onInvisible";
    }
}
