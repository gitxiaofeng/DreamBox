package com.didi.carmate.dreambox.core.data;

import com.didi.carmate.dreambox.core.base.DBNode;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.render.DBTemplate;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBMeta extends DBNode implements IDBData {
    private IDBData dbData = new DBData();

    @Override
    public void processAttr() {
        // do nothing
    }

    public <D> void addMetaData(DBTemplate template, String key, D value) {
        dbData.addData(template, key, value);
    }

    public <D> D getMetaData(DBTemplate template, String key, Class<D> clazz) {
        return dbData.getData(template, key, clazz);
    }

    @Override
    public void observeData(DBTemplate template, DBData.IDBObserveData observeData) {
        dbData.observeData(template, observeData);
    }

    @Override
    public void unObserveData(DBTemplate template, DBData.IDBObserveData observeData) {
        dbData.unObserveData(template, observeData);
    }

    @Override
    public void removeObservers(DBTemplate template) {
        dbData.removeObservers(template);
    }

    @Override
    public <D> void changeData(DBTemplate template, String key, D value) {
        dbData.changeData(template, key, value);
    }

    @Override
    public <D> void addData(DBTemplate template, String key, D value) {
        dbData.addData(template, key, value);
    }

    @Override
    public void removeTemplate(DBTemplate template) {
        dbData.removeTemplate(template);
    }

    @Override
    public <D> D getData(DBTemplate template, String key, Class<D> clazz) {
        return dbData.getData(template, key, clazz);
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBMeta createNode() {
            return new DBMeta();
        }
    }

    public static String getNodeTag() {
        return "meta";
    }
}
