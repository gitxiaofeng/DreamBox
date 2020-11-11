package com.didi.carmate.dreambox.core.data;

import com.didi.carmate.dreambox.core.render.DBTemplate;

/**
 * author: chenjing
 * date: 2020/5/7
 */
public interface IDBData {
    void observeData(DBTemplate template, DBData.IDBObserveData observeData);

    void unObserveData(DBTemplate template, DBData.IDBObserveData observeData);

    /**
     * 移除某个模板所有的属性观察者，主要用户模板销毁时，批量删除
     *
     * @param template 模板实例
     */
    void removeObservers(DBTemplate template);

    /**
     * 暴露给外部，用来改版属性的值，同时触发property changed事件给观察者
     *
     * @param template 模板实例
     * @param key      属性key
     * @param value    新的属性值
     */
    <D> void changeData(DBTemplate template, String key, D value);

    /**
     * 暴露给外部，用来往属性池里添加新的值，值用来作为view对象、埋点等的数据源可以
     *
     * @param template 模板实例
     * @param key      属性key
     * @param value    属性值
     */
    <D> void addData(DBTemplate template, String key, D value);

    /**
     * 移除某个模板所有添加进来的属性，主要用户模板销毁时，批量删除
     *
     * @param template dream box对象实例
     */
    void removeTemplate(DBTemplate template);

    <D> D getData(DBTemplate template, String key, Class<D> clazz);
}
