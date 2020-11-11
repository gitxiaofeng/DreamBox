package com.didi.carmate.dreambox.core.render;

import android.view.View;
import android.widget.ImageView;

import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.render.view.DBNinePatchView;
import com.didi.carmate.dreambox.core.render.view.DBRootView;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.didi.carmate.dreambox.wrapper.ImageLoader;
import com.google.gson.JsonObject;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBImage extends DBBaseView<View> {
    private String rawSrc;
    private String src;
    private String scaleType;
    private ImageLoader imageLoader;
    private boolean isNinePatch;

    @DBDomAttr(key = "src")
    public void setRawSrc(String rawSrc) {
        this.rawSrc = rawSrc;
    }

    @DBDomAttr(key = "scaleType")
    public void setScaleType(String scaleType) {
        this.scaleType = scaleType;
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        imageLoader = mDBContext.getWrapperImpl().imageLoader();
    }

    @Override
    public void processAttr() {
        super.processAttr();

        src = getVariableString(rawSrc);
//        isNinePatch = src.endsWith(".9.png");
        // TODO image增加[isNinePatch]属性
    }

    @Override
    public void processAttr(JsonObject dict) {
        super.processAttr(dict);

        src = getVariableString(rawSrc, dict);
//        isNinePatch = src.endsWith(".9.png");
    }

    @Override
    protected View createView(DBRootView parentView) {
        View view;
        if (isNinePatch) {
            view = new DBNinePatchView(parentView.getContext());
        } else {
            view = new ImageView(parentView.getContext());
        }
        return view;
    }

    @Override
    protected void doRender(View view) {
        super.doRender(view);

        loadImage(view);
    }

    @Override
    public void changeOnCallback(View view, String key, Object oldValue, Object newValue) {
        if (null != newValue) {
            src = getVariableString(rawSrc);
//            isNinePatch = src.endsWith(".9.png");

            loadImage(view);
        }
    }

    private void loadImage(View view) {
        if (isNinePatch && view instanceof DBNinePatchView) {
            DBNinePatchView ninePatchView = (DBNinePatchView) view;
            if (!DBUtils.isEmpty(src) && null != imageLoader) {
                imageLoader.load(src, ninePatchView);
            }
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            // scaleType
            if ("crop".equals(scaleType)) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else if ("inside".equals(scaleType)) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } else if ("fitXY".equals(scaleType)) {
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            // src
            if (!DBUtils.isEmpty(src) && null != imageLoader) {
                imageLoader.load(src, imageView);
            }
        }
    }

    public static String getNodeTag() {
        return "image";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBImage createNode() {
            return new DBImage();
        }
    }
}
