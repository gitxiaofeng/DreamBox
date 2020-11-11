package com.didi.carmate.dreambox.core.base;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.Lifecycle;

import com.didi.carmate.dreambox.core.action.DBAlias;
import com.didi.carmate.dreambox.core.bridge.DBBridgeHandler;
import com.didi.carmate.dreambox.core.data.DBData;
import com.didi.carmate.dreambox.core.data.DBMeta;
import com.didi.carmate.dreambox.core.render.DBTemplate;
import com.didi.carmate.dreambox.wrapper.Log;
import com.google.gson.JsonArray;
import com.didi.carmate.dreambox.wrapper.inner.WrapperInner;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * author: chenjing
 * date: 2020/5/11
 */
public class DBContext {
    private Application mApplication;
    private WrapperInner mWrapperImpl;
    private Context mCurrentContext;
    private Lifecycle mLifecycle;
    private DBTemplate mDBTemplate;
    private DBMeta mDBMeta;
    private Map<String, DBAlias> mDBAliasMap;
    private DBBridgeHandler mBridgeHandler;

    public DBContext(Application application, WrapperInner wrapperImpl, Context currentContext, Lifecycle lifecycle) {
        mApplication = application;
        mWrapperImpl = wrapperImpl;
        mCurrentContext = currentContext;
        mLifecycle = lifecycle;
        mBridgeHandler = new DBBridgeHandler(this);
    }

    public WrapperInner getWrapperImpl() {
        return mWrapperImpl;
    }

    public Lifecycle getLifecycle() {
        return mLifecycle;
    }

    public void setDBMeta(DBMeta dbMeta, JsonObject extJsonObject) {
        mDBMeta = dbMeta;
        if (null != mDBMeta && null != extJsonObject) {
            mDBMeta.addMetaData(mDBTemplate, DBConstants.DATA_EXT_PREFIX, extJsonObject);
        }
    }

    public void setExtDBMeta(JsonObject extJsonObject) {
        if (null != mDBMeta && null != extJsonObject) {
            mDBMeta.addMetaData(mDBTemplate, DBConstants.DATA_EXT_PREFIX, extJsonObject);
        }
    }

    public void setExtDBMeta(JsonObject extJsonObject, String key) {
        if (null != mDBMeta && null != extJsonObject) {
            mDBMeta.addMetaData(mDBTemplate, key, extJsonObject);
        }
    }

    public void setDBAlias(Map<String, DBAlias> aliasCollection) {
        mDBAliasMap = aliasCollection;
    }

    public DBAlias getDBAlias(String aliasId) {
        DBAlias alias = null;
        if (null != mDBAliasMap) {
            alias = mDBAliasMap.get(aliasId);
        }
        return alias;
    }

    public void setDBTemplate(DBTemplate template) {
        this.mDBTemplate = template;
    }

    public DBTemplate getDBTemplate() {
        return mDBTemplate;
    }

    // ------------- meta pool -------------
    public void putStringValue(String key, String value) {
        if (null != mDBMeta) {
            mDBMeta.addMetaData(mDBTemplate, key, value);
        }
    }

    public String getStringValue(String key) {
        if (null != mDBMeta) {
            Object value = mDBMeta.getMetaData(mDBTemplate, key, String.class);
            if (null != value) {
                return value.toString();
            }
        }
        return "";
    }

    public void putBooleanValue(String key, boolean value) {
        if (null != mDBMeta) {
            mDBMeta.addMetaData(mDBTemplate, key, value);
        }
    }

    public boolean getBooleanValue(String key) {
        if (null != mDBMeta && null != key) {
            Boolean value = mDBMeta.getMetaData(mDBTemplate, key, Boolean.class);
            return null != value ? value : false;
        } else {
            return false;
        }
    }

    public void putIntValue(String key, int value) {
        if (null != mDBMeta) {
            mDBMeta.addMetaData(mDBTemplate, key, value);
        }
    }

    public int getIntValue(String key) {
        if (null != mDBMeta) {
            return mDBMeta.getMetaData(mDBTemplate, key, Integer.class);
        }
        return -1;
    }

    public void putJsonValue(String key, JsonObject jsonObject) {
        if (null != mDBMeta) {
            mDBMeta.addMetaData(mDBTemplate, key, jsonObject);
        }
    }

    public JsonObject getJsonValue(String key) {
        if (null != mDBMeta) {
            return mDBMeta.getMetaData(mDBTemplate, key, JsonObject.class);
        }
        return null;
    }

    public void putJsonArray(String key, JsonArray jsonArray) {
        if (null != mDBMeta) {
            mDBMeta.addMetaData(mDBTemplate, key, jsonArray);
        }
    }

    public JsonArray getJsonArray(String key) {
        if (null != mDBMeta) {
            return mDBMeta.getMetaData(mDBTemplate, key, JsonArray.class);
        }
        return null;
    }

    public void changeStringValue(String key, String value) {
        if (null != mDBMeta) {
            mDBMeta.changeData(mDBTemplate, key, value);
        }
    }

    public void changeBooleanValue(String key, boolean value) {
        if (null != mDBMeta) {
            mDBMeta.changeData(mDBTemplate, key, value);
        }
    }

    public void changeIntValue(String key, int value) {
        if (null != mDBMeta) {
            mDBMeta.changeData(mDBTemplate, key, value);
        }
    }

    public void observeData(DBData.IDBObserveData observeData) {
        if (null != mDBMeta) {
            mDBMeta.observeData(mDBTemplate, observeData);
        }
    }

    // ------------- meta pool -------------

    public String getAccessKey() {
        return mDBTemplate.getAccessKey();
    }

    public String getTemplateId() {
        return mDBTemplate.getTemplateId();
    }

    public Log getLog() {
        return mWrapperImpl.log();
    }

    public Context getContext() {
        return null != mCurrentContext ? mCurrentContext : mApplication;
    }

    public Application getApplication() {
        return mApplication;
    }

    public DBBridgeHandler getBridgeHandler() {
        return mBridgeHandler;
    }

    public void release() {
        mApplication = null;
        mWrapperImpl = null;
        mCurrentContext = null;
        mLifecycle = null;
        mDBTemplate = null;
        if (null != mDBMeta) {
            mDBMeta.release();
            mDBMeta = null;
        }
        mDBAliasMap.clear();
        mDBAliasMap = null;
    }
}
