package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.IDBNode;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public interface IDBCallback extends IDBNode {
    /**
     * 获取当前callback action子节点集合
     */
    List<IDBNode> getActions();
}
