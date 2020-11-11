package com.didi.carmate.dreambox.core.render;

import com.didi.carmate.dreambox.core.render.view.DBRootView;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public interface IDBRecycleRender extends IDBRender {
    /**
     * 视图复用，用户List场景，视图复用
     *
     * @param parentView 根节点视图对象
     */
    void recycleRender(DBRootView parentView);
}
