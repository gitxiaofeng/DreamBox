package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.DBNode;
import com.didi.carmate.dreambox.core.base.INodeCreator;

/**
 * author: chenjing
 * date: 2020/5/7
 */
public class DBActions extends DBNode {
    public static String getNodeTag() {
        return "actions";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBActions createNode() {
            return new DBActions();
        }
    }
}
