package com.didi.carmate.dreambox.core.render;

import android.graphics.Color;
import android.view.View;

import com.didi.carmate.dreambox.core.R;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.render.view.DBCircleLoading;
import com.didi.carmate.dreambox.core.render.view.DBDotLoading;
import com.didi.carmate.dreambox.core.render.view.DBRootView;
import com.didi.carmate.dreambox.core.utils.DBUtils;

/**
 * author: chenjing
 * date: 2020/5/11
 */
public class DBLoading extends DBBaseView<View> {
    private static final String TAG = "DBLoading";

    private String style;

    @DBDomAttr(key = "style")
    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    protected View createView(DBRootView parentView) {
        View view;
        if (DBUtils.isEmpty(style) || "circle".equals(style)) {
            view = new DBCircleLoading(parentView.getContext());
        } else {
            view = new DBDotLoading(parentView.getContext());
        }
        return view;
    }

    @Override
    public void doRender(View view) {
        super.doRender(view);

        if ((DBUtils.isEmpty(style) || "circle".equals(style)) && view instanceof DBCircleLoading) {
            DBCircleLoading progress = (DBCircleLoading) view;
            progress.setDrawable(R.drawable.db_loading_circle_anim);
        } else {
            DBDotLoading progress = (DBDotLoading) view;
            progress.setBgColor(Color.GREEN);
            progress.startLoading();
        }
    }

    public static String getNodeTag() {
        return "loading";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBLoading createNode() {
            return new DBLoading();
        }
    }
}
