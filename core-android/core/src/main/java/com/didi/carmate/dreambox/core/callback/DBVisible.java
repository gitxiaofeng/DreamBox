package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/5/8
 */
public class DBVisible extends DBCallback {

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBVisible createNode() {
            return new DBVisible();
        }
    }

    public static String getNodeTag() {
        return "onVisible";
    }
}
