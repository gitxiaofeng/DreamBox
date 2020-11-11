package com.didi.carmate.dreambox.core.action;

import androidx.annotation.Nullable;

import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.wrapper.Net;
import com.didi.carmate.dreambox.wrapper.Wrapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBNet extends DBAction {
    private Net net;

    private String rawUrl;
    private String url;
    private String to;

    @DBDomAttr(key = "url")
    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    @DBDomAttr(key = "to")
    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        net = mDBContext.getWrapperImpl().net();
    }

    @Override
    public boolean processChildAction() {
        return false;
    }

    @Override
    public void doInvoke() {
        url = getVariableString(rawUrl);
        netRequest();
    }

    @Override
    public void doInvoke(JsonObject dict) {
        url = getVariableString(rawUrl, dict);
        netRequest();
    }

    private void netRequest() {
        net.get(url, new Net.Callback() {
            @Override
            public void onSuccess(@Nullable String json) {
                try {
                    JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
                    // 数据更新到数据池
                    if (null != to) {
                        mDBContext.putJsonValue(to, jsonObject);
                    }
                } catch (JsonSyntaxException e) {
                    Wrapper.get(mDBContext.getAccessKey()).log().e("not json String: " + json);
                    return;
                }

                List<IDBAction> actionList = mDBActionPool.getCallbackNetSuccessAction();
                if (null != actionList) {
                    for (IDBAction action : actionList) {
                        action.invoke();
                    }
                }
            }

            @Override
            public void onError(int httpCode, @Nullable Exception error) {
                List<IDBAction> actionList = mDBActionPool.getCallbackNetErrorAction();
                if (null != actionList) {
                    for (IDBAction action : actionList) {
                        action.invoke();
                    }
                }
            }
        });
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBNet createNode() {
            return new DBNet();
        }
    }

    public static String getNodeTag() {
        return "net";
    }
}