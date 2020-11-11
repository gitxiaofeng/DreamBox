package com.didi.carmate.dreambox.core.base;

import com.didi.carmate.dreambox.core.action.IDBAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: chenjing
 * date: 2020/5/12
 */
public class DBActionPool {
    private String TYPE_CLICK = "click";
    private String TYPE_VISIBLE = "visible";
    private String TYPE_INVISIBLE = "invisible";
    private String TYPE_CALLBACK_NET_SUCCESS = "callback_net_success";
    private String TYPE_CALLBACK_NET_ERROR = "callback_net_error";
    private String TYPE_CALLBACK_DIALOG_POSITIVE = "callback_dialog_positive";
    private String TYPE_CALLBACK_DIALOG_NEGATIVE = "callback_dialog_negative";
    private String TYPE_LIST_ON_PULL = "list_on_pull";
    private String TYPE_LIST_ON_MORE = "list_on_more";
    private String TYPE_BRIDGE_ON_EVENT = "bridge_on_event";
    private String TYPE_BRIDGE_CALLBACK_DB_TO_N = "bridge_on_event"; // DBSendEvent 的回调

    private Map<String, List<IDBAction>> mActionMap = new HashMap<>();

    public void putClickAction(IDBAction action) {
        List<IDBAction> clickActions = mActionMap.get(TYPE_CLICK);
        if (null == clickActions) {
            clickActions = new ArrayList<>();
            mActionMap.put(TYPE_CLICK, clickActions);
        }
        clickActions.add(action);
    }

    public void putVisibleAction(IDBAction action) {
        List<IDBAction> visibleActions = mActionMap.get(TYPE_VISIBLE);
        if (null == visibleActions) {
            visibleActions = new ArrayList<>();
            mActionMap.put(TYPE_VISIBLE, visibleActions);
        }
        visibleActions.add(action);
    }

    public void putInvisibleAction(IDBAction action) {
        List<IDBAction> invisibleActions = mActionMap.get(TYPE_INVISIBLE);
        if (null == invisibleActions) {
            invisibleActions = new ArrayList<>();
            mActionMap.put(TYPE_INVISIBLE, invisibleActions);
        }
        invisibleActions.add(action);
    }

    public void putCallbackNetSuccessAction(IDBAction action) {
        List<IDBAction> successActions = mActionMap.get(TYPE_CALLBACK_NET_SUCCESS);
        if (null == successActions) {
            successActions = new ArrayList<>();
            mActionMap.put(TYPE_CALLBACK_NET_SUCCESS, successActions);
        }
        successActions.add(action);
    }

    public void putCallbackNetErrorAction(IDBAction action) {
        List<IDBAction> errorActions = mActionMap.get(TYPE_CALLBACK_NET_ERROR);
        if (null == errorActions) {
            errorActions = new ArrayList<>();
            mActionMap.put(TYPE_CALLBACK_NET_ERROR, errorActions);
        }
        errorActions.add(action);
    }

    public void putCallbackDialogPositiveAction(IDBAction action) {
        List<IDBAction> dialogPositiveActions = mActionMap.get(TYPE_CALLBACK_DIALOG_POSITIVE);
        if (null == dialogPositiveActions) {
            dialogPositiveActions = new ArrayList<>();
            mActionMap.put(TYPE_CALLBACK_DIALOG_POSITIVE, dialogPositiveActions);
        }
        dialogPositiveActions.add(action);
    }

    public void putCallbackDialogNegativeAction(IDBAction action) {
        List<IDBAction> dialogNegativeActions = mActionMap.get(TYPE_CALLBACK_DIALOG_NEGATIVE);
        if (null == dialogNegativeActions) {
            dialogNegativeActions = new ArrayList<>();
            mActionMap.put(TYPE_CALLBACK_DIALOG_NEGATIVE, dialogNegativeActions);
        }
        dialogNegativeActions.add(action);
    }

    public void putListOnPull(IDBAction action) {
        List<IDBAction> actionList = mActionMap.get(TYPE_LIST_ON_PULL);
        if (null == actionList) {
            actionList = new ArrayList<>();
            mActionMap.put(TYPE_LIST_ON_PULL, actionList);
        }
        actionList.add(action);
    }

    public void putListOnMore(IDBAction action) {
        List<IDBAction> actionList = mActionMap.get(TYPE_LIST_ON_MORE);
        if (null == actionList) {
            actionList = new ArrayList<>();
            mActionMap.put(TYPE_LIST_ON_MORE, actionList);
        }
        actionList.add(action);
    }

    public void putBridgeOnEvent(IDBAction action) {
        List<IDBAction> actionList = mActionMap.get(TYPE_BRIDGE_ON_EVENT);
        if (null == actionList) {
            actionList = new ArrayList<>();
            mActionMap.put(TYPE_BRIDGE_ON_EVENT, actionList);
        }
        actionList.add(action);
    }

    public void putBridgeCallbackDBToN(IDBAction action) {
        List<IDBAction> actionList = mActionMap.get(TYPE_BRIDGE_CALLBACK_DB_TO_N);
        if (null == actionList) {
            actionList = new ArrayList<>();
            mActionMap.put(TYPE_BRIDGE_CALLBACK_DB_TO_N, actionList);
        }
        actionList.add(action);
    }

    public List<IDBAction> getClickAction() {
        return mActionMap.get(TYPE_CLICK);
    }

    public List<IDBAction> getVisibleAction() {
        return mActionMap.get(TYPE_VISIBLE);
    }

    public List<IDBAction> getInvisibleAction() {
        return mActionMap.get(TYPE_INVISIBLE);
    }

    public List<IDBAction> getCallbackNetSuccessAction() {
        return mActionMap.get(TYPE_CALLBACK_NET_SUCCESS);
    }

    public List<IDBAction> getCallbackNetErrorAction() {
        return mActionMap.get(TYPE_CALLBACK_NET_ERROR);
    }

    public List<IDBAction> getCallbackDialogPositiveAction() {
        return mActionMap.get(TYPE_CALLBACK_DIALOG_POSITIVE);
    }

    public List<IDBAction> getCallbackDialogNegativeAction() {
        return mActionMap.get(TYPE_CALLBACK_DIALOG_NEGATIVE);
    }

    public List<IDBAction> getListOnPullAction() {
        return mActionMap.get(TYPE_LIST_ON_PULL);
    }

    public List<IDBAction> getListOnMoreAction() {
        return mActionMap.get(TYPE_LIST_ON_MORE);
    }

    public List<IDBAction> getBridgeOnEventAction() {
        return mActionMap.get(TYPE_BRIDGE_ON_EVENT);
    }

    public List<IDBAction> getBridgeCallbackDBToNAction() {
        return mActionMap.get(TYPE_BRIDGE_CALLBACK_DB_TO_N);
    }

    public void release() {
        mActionMap.clear();
    }
}
