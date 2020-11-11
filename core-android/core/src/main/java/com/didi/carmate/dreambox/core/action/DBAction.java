package com.didi.carmate.dreambox.core.action;

import android.app.Activity;
import android.content.Context;

import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.DBNode;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.callback.IDBCallback;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.google.gson.JsonObject;

import java.util.Set;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public abstract class DBAction extends DBNode implements IDBAction {
    private String dependOn;
    private String invokeJsonObject;
    private String aliasJsonObject;

    @DBDomAttr(key = "dependOn")
    public void setDependOn(String dependOn) {
        this.dependOn = dependOn;
    }

    @Override
    public boolean isActionNode() {
        return true;
    }

    @Override
    public void setInvokeJsonObject(String invokeJsonObject) {
        this.invokeJsonObject = invokeJsonObject;
    }

    /**
     * 设置alias的数据源到action节点
     */
    @Override
    public void setAliasJsonObject(String aliasJsonObject) {
        this.aliasJsonObject = aliasJsonObject;
    }

    @Override
    public void invoke() {
        if (null == mDBContext) {
            return;
        }
        Context context = mDBContext.getContext();
        if (null == context || (context instanceof Activity && ((Activity) context).isFinishing())) {
            return;
        }

        // 数据优先级覆盖
        JsonObject jsonMerged = null; // src合并后的值
        JsonObject jsonRawInvoke = getVariableJsonObject(invokeJsonObject); // 从属性里拿到的原始值
        JsonObject jsonRawAlias = getVariableJsonObject(aliasJsonObject);   // 从属性里拿到的原始值
        if (null != jsonRawInvoke && null != jsonRawAlias) {
            jsonMerged = combineJson(jsonRawAlias, jsonRawInvoke);
        } else if (null != jsonRawInvoke) {
            jsonMerged = jsonRawInvoke;
        } else if (null != jsonRawAlias) {
            jsonMerged = jsonRawAlias;
        }

        if (DBUtils.isEmpty(dependOn)) {
            if (null == jsonMerged) {
                doInvoke();
            } else {
                doInvoke(jsonMerged);
            }
        } else {
            String[] keys = dependOn.split(";");
            if (keys.length == 1 && getVariableBoolean(keys[0])) {
                if (null == jsonMerged) {
                    doInvoke();
                } else {
                    doInvoke(jsonMerged);
                }
            } else if (getVariableBoolean(keys[0]) && getVariableBoolean(keys[1])) {
                if (null == jsonMerged) {
                    doInvoke();
                } else {
                    doInvoke(jsonMerged);
                }
            }
        }
    }

    @Override
    public IDBCallback getActionCallback() {
        IDBNode dbNode = getParentNode();
        if (dbNode instanceof DBActions) {
            dbNode = dbNode.getParentNode();
            if (dbNode instanceof IDBCallback) {
                return (IDBCallback) dbNode;
            }
        }
        return null;
    }

    /**
     * 合并两个JsonObject对象，如果有相同的key，addedObj 优先级高于 srcObj
     *
     * @param srcObj   原json对象
     * @param addedObj 待合并json对象
     */
    private static JsonObject combineJson(JsonObject srcObj, JsonObject addedObj) {
        Set<String> addObjKeys = addedObj.keySet();
        for (String key : addObjKeys) {
            srcObj.add(key, addedObj.get(key));
        }
        return srcObj;
    }

    protected abstract void doInvoke();

    protected abstract void doInvoke(JsonObject dict);
}
