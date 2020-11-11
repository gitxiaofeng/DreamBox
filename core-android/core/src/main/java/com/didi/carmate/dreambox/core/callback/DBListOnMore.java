package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/5/9
 * <p>
 * implement in future
 */
public class DBListOnMore extends DBCallback {

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBListOnMore createNode() {
            return new DBListOnMore();
        }
    }

    public static String getNodeTag() {
        return "onMore";
    }
}
