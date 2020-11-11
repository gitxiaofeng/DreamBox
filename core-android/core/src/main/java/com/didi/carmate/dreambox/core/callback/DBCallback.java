package com.didi.carmate.dreambox.core.callback;

import com.didi.carmate.dreambox.core.base.DBNode;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.base.INodeCreator;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/5/9
 */
public class DBCallback extends DBNode implements IDBCallback {
    @Override
    public List<IDBNode> getActions() {
        final List<IDBNode> childNotes = getChildNodes();
        if (null != childNotes && childNotes.size() > 0) {
            return childNotes.get(0).getChildNodes();
        }
        return null;
    }


}
