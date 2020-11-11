package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/5/9
 * <p>
 * implement in future
 */
public class DBListOnPull extends DBCallback {

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBListOnPull createNode() {
            return new DBListOnPull();
        }
    }

    public static String getNodeTag() {
        return "onPull";
    }
}
