package com.didi.carmate.dreambox.core.render;

import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.render.view.DBRootView;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public interface IDBRender extends IDBNode {
    /**
     * 渲染视图
     *
     * @param parentView 根节点视图对象
     */
    void processRender(DBRootView parentView);
}
