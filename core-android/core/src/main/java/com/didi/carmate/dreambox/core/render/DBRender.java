package com.didi.carmate.dreambox.core.render;

import com.didi.carmate.dreambox.core.base.DBNode;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.render.view.DBRootView;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/5/7
 */
public class DBRender extends DBNode implements IDBRender {
    private static final String TAG = "DBRender";

    @Override
    public void processRender(DBRootView parentView) {
        // nothing to do in render node

        // 子节点渲染处理
        List<IDBNode> childNodes = getChildNodes();
        if (null != childNodes && childNodes.size() > 0) {
            for (IDBNode node : childNodes) {
                ((IDBRender) node).processRender(parentView);
            }
        }
    }

    public static String getNodeTag() {
        return "render";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBRender createNode() {
            return new DBRender();
        }
    }
}
