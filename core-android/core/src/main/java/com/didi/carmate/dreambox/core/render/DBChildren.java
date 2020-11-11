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
public class DBChildren extends DBNode implements IDBRender {

    @Override
    public void processRender(DBRootView parentView) {
        // nothing to do in render node

        // 子节点渲染处理
        List<IDBNode> renderChildNodes = getChildNodes();
        for (IDBNode node : renderChildNodes) {
            if (node instanceof IDBRender) {
                ((IDBRender) node).processRender(parentView);
            }
        }
    }

    public static String getNodeTag() {
        return "children";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBChildren createNode() {
            return new DBChildren();
        }
    }
}
