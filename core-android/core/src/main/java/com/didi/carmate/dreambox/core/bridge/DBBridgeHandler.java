package com.didi.carmate.dreambox.core.bridge;

import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.render.DBTemplate;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * author: chenjing
 * date: 2020/7/15
 */
public class DBBridgeHandler {
    private Map<String, IDBEventReceiver> eventReceiverMap = new HashMap<>();
    private DBContext dbContext;

    public DBBridgeHandler(DBContext dbContext) {
        this.dbContext = dbContext;
    }

    public void registerEventReceiver(String eid, IDBEventReceiver eventReceiver) {
        if (!eventReceiverMap.containsKey(eid)) {
            eventReceiverMap.put(eid, eventReceiver);
        }
    }

    public void unRegisterEventReceiver(String eid) {
        eventReceiverMap.remove(eid);
    }

    public void nativeInvokeDb(String eid, String eventData) {
        DBTemplate dbTemplate = dbContext.getDBTemplate();
        DBOnEvent onEvent = dbTemplate.getDBOnEvent();
        if (null != onEvent && onEvent.getEid() != null && onEvent.getEid().equals(eid)) {
            String key = onEvent.getMsgTo();
            if (!DBUtils.isEmpty(key)) {
                if (eventData.startsWith("{") && eventData.endsWith("}")) {
                    dbContext.setExtDBMeta(new Gson().fromJson(eventData, JsonObject.class), key);
                } else {
                    dbContext.putStringValue(key, eventData);
                }
                onEvent.processAttr(); // 重新解析子节点属性
            }
            dbTemplate.onEvent(); // action invoke
        }
    }

    void dbInvokeNative(DBSendEvent sendEvent, String eid, JsonObject msg) {
        dbInvokeNative(sendEvent, eid, msg, null);
    }

    void dbInvokeNative(final DBSendEvent sendEvent, String eid, JsonObject msg, final DBBridgeDBToNativeCallback dbToNativeCallback) {
        IDBEventReceiver eventReceiver = eventReceiverMap.get(eid);
        if (null != eventReceiver) {
            if (null == dbToNativeCallback) {
                eventReceiver.onEvent(msg, null);
            } else {
                eventReceiver.onEvent(msg, new IDBEventReceiver.Callback() {
                    @Override
                    public void onCallback(String msgTo) {
                        String key = dbToNativeCallback.getMsgTo();
                        if (!DBUtils.isEmpty(key)) {
                            dbContext.setExtDBMeta(new Gson().fromJson(msgTo, JsonObject.class), key);
                            dbToNativeCallback.processAttr(); // 重新解析子节点属性
                        }
                        sendEvent.onCallback(); // action invoke
                    }
                });
            }
        }
    }
}
