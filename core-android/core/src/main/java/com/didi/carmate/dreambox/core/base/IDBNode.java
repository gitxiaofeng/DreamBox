package com.didi.carmate.dreambox.core.base;

import com.google.gson.JsonObject;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public interface IDBNode {
    /**
     * 设置当前节点的父节点
     */
    void setParentNode(IDBNode parentNode);

    /**
     * 获取当前节点的父节点，用于从父节点拿相关数据
     */
    IDBNode getParentNode();

    /**
     * 设置当前节点的子节点
     */
    void addChildNote(IDBNode childNode);

    /**
     * 设置当前节点的子节点
     */
    List<IDBNode> getChildNodes();

    /**
     * 获取当前节点的DBChildren子节点集合
     *
     * @return children 子节点集合
     */
    List<IDBNode> getChildren();

    /**
     * 获取当前节点DBChildren节点里某个child
     *
     * @param clz 子child类型class
     * @param <C> 子child类型
     * @return 子child，找不到返回null
     */
    <C> C getChild(Class<C> clz);

    /**
     * 做一些预处理的工作，例：将树形节点进一步做结构化处理，方便使用
     */
    void preProcess(DBContext dbContext);

    /**
     * 属性数据处理
     */
    void processAttr();

    /**
     * 属性数据处理，在给定的字典
     *
     * @param dict 数据源采用传进来的字典，不采用meta数据池数据
     */
    void processAttr(JsonObject dict);

    /**
     * 处理action
     */
    void processAction();

    /**
     * 当前节点如果有Event类型子节点，需要处理该方法。
     * runtime会提供默认处理，如果需要特殊处理则需覆写此方法
     */
    void processEvent();

    /**
     * 是否是Action节点
     */
    boolean isActionNode();

    /**
     * 节点相关资源释放和清理
     */
    void release();
}
