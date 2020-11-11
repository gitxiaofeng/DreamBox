package com.didi.carmate.dreambox.core.action;

import android.app.Activity;
import android.content.Context;

import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.google.gson.JsonObject;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBClosePage extends DBAction {
    @Override
    public void doInvoke() {
        Context context = mDBContext.getContext();
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    @Override
    public void doInvoke(JsonObject dict) {
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBClosePage createNode() {
            return new DBClosePage();
        }
    }

    public static String getNodeTag() {
        return "closePage";
    }
}