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
public class DBListHeader extends DBNode implements IDBRecycleRender {

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

    @Override
    public void recycleRender(DBRootView parentView) {
        List<IDBNode> renderChildNodes = getChildNodes();
        for (IDBNode node : renderChildNodes) {
            if (node instanceof IDBRecycleRender) {
                ((IDBRecycleRender) node).recycleRender(parentView);
            }
        }
    }

    public static String getNodeTag() {
        return "header";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBListHeader createNode() {
            return new DBListHeader();
        }
    }
}
