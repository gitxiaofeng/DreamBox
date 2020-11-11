package com.didi.carmate.dreambox.core.data;

import com.didi.carmate.dreambox.core.render.DBTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author: chenjing
 * date: 2020/5/7
 */
public class DBData implements IDBData {
    private Map<DBTemplate, ConcurrentHashMap<String, Object>> mDataPool = new HashMap<>();
    private Map<DBTemplate, List<IDBObserveData>> mObservePool = new HashMap<>();

    DBData() {

    }

    public interface IDBObserveData<T> {
        void onDataChanged(DBTemplate template, String key, T oldValue, T newValue);

        String getDataKey();
    }

    public void observeData(DBTemplate template, IDBObserveData observeData) {
        List<IDBObserveData> observers = mObservePool.get(template);
        if (null == observers) {
            observers = new ArrayList<>();
            mObservePool.put(template, observers);
        }

        if (!observers.contains(observeData)) {
            observers.add(observeData);
        }
    }

    public void unObserveData(DBTemplate template, IDBObserveData observeData) {
        List<IDBObserveData> observers = mObservePool.get(template);
        if (null != observers) {
            observers.remove(observeData);
        }
    }

    /**
     * 移除某个模板所有的属性观察者，主要用户模板销毁时，批量删除
     *
     * @param template 模板实例
     */
    public void removeObservers(DBTemplate template) {
        mObservePool.remove(template);
    }

    /**
     * 暴露给外部，用来改版属性的值，同时触发property changed事件给观察者
     *
     * @param template 模板实例
     * @param key      属性key
     * @param value    新的属性值
     */
    public <D> void changeData(DBTemplate template, String key, D value) {
        Map<String, Object> properties = mDataPool.get(template);
        if (null != properties) {
            // 更新属性值
            Object oldValue = properties.get(key);
            properties.put(key, value);

            // 通知观察者
            List<IDBObserveData> observers = mObservePool.get(template);
            if (null != observers) {
                for (IDBObserveData observeData : observers) {
                    if (key.equals(observeData.getDataKey())) {
                        observeData.onDataChanged(template, key, oldValue, value);
                    }
                }
            }
        }
    }

    /**
     * 暴露给外部，用来往属性池里添加新的值，值用来作为view对象、埋点等的数据源可以
     *
     * @param template 模板实例
     * @param key      属性key
     * @param value    属性值
     */
    public <D> void addData(DBTemplate template, String key, D value) {
        ConcurrentHashMap<String, Object> properties = mDataPool.get(template);
        if (null == properties) {
            properties = new ConcurrentHashMap<>();
            mDataPool.put(template, properties);
        }

        // 更新属性值
        Object oldValue = properties.get(key);
        properties.put(key, value);
        // 通知观察者
        List<IDBObserveData> observers = mObservePool.get(template);
        if (null != observers) {
            for (IDBObserveData observeData : observers) {
                if (key.equals(observeData.getDataKey())) {
                    observeData.onDataChanged(template, key, oldValue, value);
                }
            }
        }
    }

    /**
     * 移除某个模板所有添加进来的属性，模板销毁时使用
     *
     * @param template dream box对象实例
     */
    public void removeTemplate(DBTemplate template) {
        mDataPool.remove(template);
    }

    public <D> D getData(DBTemplate template, String key, Class<D> clazz) {
        D ret = null;
        Map<String, Object> properties = mDataPool.get(template);
        if (null != properties) {
            Object obj = properties.get(key);
            if (null != obj) {
                if (obj.getClass().equals(clazz) || String.class.equals(clazz)) {
                    ret = (D) obj;
                }
            }
        }

        return ret;
    }
}
