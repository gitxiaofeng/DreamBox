package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.callback.IDBCallback;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public interface IDBAction extends IDBNode {
    /**
     * 执行某个节点
     */
    void invoke();

    /**
     * 设置invoke的数据源到action节点
     */
    void setInvokeJsonObject(String invokeJsonObject);

    /**
     * 设置alias的数据源到action节点
     */
    void setAliasJsonObject(String aliasJsonObject);

    /**
     * 获取action所属的callback
     */
    IDBCallback getActionCallback();
}
