package com.didi.carmate.dreambox.core.render;

import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.render.view.DBProgressView;
import com.didi.carmate.dreambox.core.render.view.DBRootView;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.didi.carmate.dreambox.wrapper.ImageLoader;

/**
 * author: chenjing
 * date: 2020/5/11
 */
public class DBProgress extends DBBaseView<DBProgressView> {
    private static final String TAG = "DBProgress";

    private String value;
    private String rawBarBg;
    private String rawBarFg;
    private String barBg;
    private String barFg;
    private ImageLoader imageLoader;
    private boolean isNinePatchBg = true;
    private boolean isNinePatchFg = true;
    private String patchType = DBConstants.STYLE_PATCH_TYPE_STRETCH;
//    private String patchType = DBConstants.STYLE_PATCH_TYPE_REPEAT;

    @DBDomAttr(key = "value")
    public void setValue(String value) {
        this.value = value;
    }

    @DBDomAttr(key = "barBg")
    public void setRawBarBg(String rawBarBg) {
        this.rawBarBg = rawBarBg;
    }

    @DBDomAttr(key = "barFg")
    public void setRawBarFg(String rawBarFg) {
        this.rawBarFg = rawBarFg;
    }

    @DBDomAttr(key = "patchType")
    public void setPatchType(String patchType) {
        this.patchType = patchType;
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        imageLoader = mDBContext.getWrapperImpl().imageLoader();
    }

    @Override
    public void processAttr() {
        super.processAttr();

        barBg = getVariableString(rawBarBg);
        barFg = getVariableString(rawBarFg);
//        isNinePatchBg = barBg.endsWith(".9.png");
//        isNinePatchFg = barFg.endsWith(".9.png");
        // TODO patchType->normal 普通图片
    }

    @Override
    protected DBProgressView createView(DBRootView parentView) {
        return new DBProgressView(parentView.getContext(), patchType, isNinePatchBg, isNinePatchFg);
    }

    @Override
    protected void doRender(final DBProgressView progressView) {
        super.doRender(progressView);

        // progress
        if (DBUtils.isNumeric(value)) {
            progressView.setProgress(Integer.parseInt(value));
        } else {
            progressView.setProgress(0);
        }
        progressView.post(new Runnable() {
            @Override
            public void run() {
                // background image
                if (!DBUtils.isEmpty(barBg) && null != imageLoader) {
                    imageLoader.load(barBg, progressView);
                }
                // foreground image
                if (!DBUtils.isEmpty(barFg) && null != imageLoader) {
                    imageLoader.load(barFg, progressView.getForegroundView());
                }
            }
        });
    }

    public static String getNodeTag() {
        return "progress";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBProgress createNode() {
            return new DBProgress();
        }
    }
}
