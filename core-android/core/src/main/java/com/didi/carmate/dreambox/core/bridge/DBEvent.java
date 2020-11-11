package com.didi.carmate.dreambox.core.bridge;

import com.didi.carmate.dreambox.core.action.DBAction;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.callback.IDBCallback;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/7/15
 */
public class DBEvent extends DBAction implements IDBCallback {
    @Override
    protected void doInvoke() {

    }

    @Override
    public void doInvoke(JsonObject dict) {

    }

    @Override
    public List<IDBNode> getActions() {
        final List<IDBNode> childNotes = getChildNodes();
        if (null != childNotes && childNotes.size() > 0) {
            return childNotes.get(0).getChildNodes();
        }
        return null;
    }
}
