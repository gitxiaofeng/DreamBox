package com.didi.carmate.dreambox.core.data;

import com.didi.carmate.dreambox.core.render.DBTemplate;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBProperties implements IDBData {
    private DBData dbData;

    public DBProperties() {
        dbData = new DBData();
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
}
