package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.wrapper.Dialog;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBDialog extends DBAction {

    private Dialog dialog;

    private String rawTitle;
    private String rawMsg;
    private String rawPositiveBtn;
    private String rawNegativeBtn;
    private String title;
    private String msg;
    private String positiveBtn;
    private String negativeBtn;

    @DBDomAttr(key = "title")
    public void setRawTitle(String rawTitle) {
        this.rawTitle = rawTitle;
    }

    @DBDomAttr(key = "msg")
    public void setRawMsg(String rawMsg) {
        this.rawMsg = rawMsg;
    }

    @DBDomAttr(key = "positiveBtn")
    public void setRawPositiveBtn(String rawPositiveBtn) {
        this.rawPositiveBtn = rawPositiveBtn;
    }

    @DBDomAttr(key = "negativeBtn")
    public void setRawNegativeBtn(String rawNegativeBtn) {
        this.rawNegativeBtn = rawNegativeBtn;
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        dialog = mDBContext.getWrapperImpl().dialog();
    }

    @Override
    public void doInvoke() {
        title = getVariableString(rawTitle);
        msg = getVariableString(rawMsg);
        positiveBtn = getVariableString(rawPositiveBtn);
        negativeBtn = getVariableString(rawNegativeBtn);
        invokeDialog();
    }

    @Override
    public void doInvoke(JsonObject dict) {
        title = getVariableString(rawTitle, dict);
        msg = getVariableString(rawMsg, dict);
        positiveBtn = getVariableString(rawPositiveBtn, dict);
        negativeBtn = getVariableString(rawNegativeBtn, dict);
        invokeDialog();
    }

    private void invokeDialog() {
        Dialog.OnClickListener onPositive = new Dialog.OnClickListener() {
            @Override
            public void onClick() {
                List<IDBAction> actionList = mDBActionPool.getCallbackDialogPositiveAction();
                if (null != actionList) {
                    for (IDBAction action : actionList) {
                        action.invoke();
                    }
                }
            }
        };
        Dialog.OnClickListener onNegative = new Dialog.OnClickListener() {
            @Override
            public void onClick() {
                List<IDBAction> actionList = mDBActionPool.getCallbackDialogNegativeAction();
                if (null != actionList) {
                    for (IDBAction action : actionList) {
                        action.invoke();
                    }
                }
            }
        };
        dialog.show(mDBContext.getContext(), title, msg, positiveBtn, negativeBtn, onPositive, onNegative);
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBDialog createNode() {
            return new DBDialog();
        }
    }

    public static String getNodeTag() {
        return "dialog";
    }
}