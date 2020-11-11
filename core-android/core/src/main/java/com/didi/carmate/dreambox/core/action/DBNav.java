package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.wrapper.Navigator;
import com.google.gson.JsonObject;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBNav extends DBAction {
    private static final String TAG = "DBNav";

    private Navigator nav;
    private String rawSchema;
    private String schema;

    @DBDomAttr(key = "schema")
    public void setRawSchema(String rawSchema) {
        this.rawSchema = rawSchema;
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        nav = mDBContext.getWrapperImpl().navigator();
    }

    @Override
    public boolean processChildAction() {
        return false;
    }

    @Override
    public void doInvoke() {
        if (null != rawSchema) {
            schema = getVariableString(rawSchema);
            nav.navigator(mDBContext.getContext(), schema);
        }
    }

    @Override
    public void doInvoke(JsonObject dict) {
        if (null != rawSchema) {
            schema = getVariableString(rawSchema, dict);
            nav.navigator(mDBContext.getContext(), schema);
        }
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBNav createNode() {
            return new DBNav();
        }
    }

    public static String getNodeTag() {
        return "nav";
    }
}