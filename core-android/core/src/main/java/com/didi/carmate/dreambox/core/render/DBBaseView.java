package com.didi.carmate.dreambox.core.render;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.didi.carmate.dreambox.core.action.IDBAction;
import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.DBNode;
import com.didi.carmate.dreambox.core.data.DBData;
import com.didi.carmate.dreambox.core.render.view.DBRootView;
import com.didi.carmate.dreambox.core.utils.DBScreenUtils;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.didi.carmate.dreambox.wrapper.Wrapper;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public abstract class DBBaseView<V extends View> extends DBNode implements IDBRecycleRender {
    private static final String TAG = "DBBaseView";

    private V mNativeView;

    private String type;
    private String rawWidth;
    private String rawHeight;
    private String rawMarginTop;
    private String rawMarginBottom;
    private String rawMarginLeft;
    private String rawMarginRight;
    private String rawVisibleOn;
    private String rawBackgroundUrl;   // 背景图片对应的网络地址
    private String rawBackgroundColor; // 背景颜色
    private int leftToLeft = DBConstants.DEFAULT_ID_VIEW;
    private int leftToRight = DBConstants.DEFAULT_ID_VIEW;
    private int rightToRight = DBConstants.DEFAULT_ID_VIEW;
    private int rightToLeft = DBConstants.DEFAULT_ID_VIEW;
    private int topToTop = DBConstants.DEFAULT_ID_VIEW;
    private int topToBottom = DBConstants.DEFAULT_ID_VIEW;
    private int bottomToTop = DBConstants.DEFAULT_ID_VIEW;
    private int bottomToBottom = DBConstants.DEFAULT_ID_VIEW;
    private String chainStyle;
    private String changeOn;
    private boolean visibleOn;
    private String backgroundUrl;
    private int marginTop;
    private int marginBottom;
    private int marginLeft;
    private int marginRight;

    protected int width;
    protected int height;

    int id = DBConstants.DEFAULT_ID_VIEW;
    String backgroundColor; // 背景颜色

    @DBDomAttr(key = "type")
    public void setType(String type) {
        this.type = type;
    }

    @DBDomAttr(key = "id")
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 设置宽度，单位可能是dp或px，render时根据单位动态计算
     *
     * @param rawWidth 宽度
     */
    @DBDomAttr(key = "width")
    public void setRawWidth(String rawWidth) {
        this.rawWidth = rawWidth;
    }

    public String getRawHeight() {
        return rawHeight;
    }

    /**
     * 设置高度，单位可能是dp或px，render时根据单位动态计算
     *
     * @param rawHeight 高度
     */
    @DBDomAttr(key = "height")
    public void setRawHeight(String rawHeight) {
        this.rawHeight = rawHeight;
    }

    @DBDomAttr(key = "marginTop")
    public void setRawMarginTop(String rawMarginTop) {
        this.rawMarginTop = rawMarginTop;
    }

    @DBDomAttr(key = "marginBottom")
    public void setRawMarginBottom(String rawMarginBottom) {
        this.rawMarginBottom = rawMarginBottom;
    }

    @DBDomAttr(key = "marginLeft")
    public void setRawMarginLeft(String rawMarginLeft) {
        this.rawMarginLeft = rawMarginLeft;
    }

    @DBDomAttr(key = "marginRight")
    public void setRawMarginRight(String rawMarginRight) {
        this.rawMarginRight = rawMarginRight;
    }

    @DBDomAttr(key = "leftToLeft")
    public void setLeftToLeft(int leftToLeft) {
        this.leftToLeft = leftToLeft;
    }

    @DBDomAttr(key = "leftToRight")
    public void setLeftToRight(int leftToRight) {
        this.leftToRight = leftToRight;
    }

    @DBDomAttr(key = "rightToRight")
    public void setRightToRight(int rightToRight) {
        this.rightToRight = rightToRight;
    }

    @DBDomAttr(key = "rightToLeft")
    public void setRightToLeft(int rightToLeft) {
        this.rightToLeft = rightToLeft;
    }

    @DBDomAttr(key = "topToTop")
    public void setTopToTop(int topToTop) {
        this.topToTop = topToTop;
    }

    @DBDomAttr(key = "topToBottom")
    public void setTopToBottom(int topToBottom) {
        this.topToBottom = topToBottom;
    }

    @DBDomAttr(key = "bottomToTop")
    public void setBottomToTop(int bottomToTop) {
        this.bottomToTop = bottomToTop;
    }

    @DBDomAttr(key = "bottomToBottom")
    public void setBottomToBottom(int bottomToBottom) {
        this.bottomToBottom = bottomToBottom;
    }

    @DBDomAttr(key = "visibleOn")
    public void setRawVisibleOn(String rawVisibleOn) {
        this.rawVisibleOn = rawVisibleOn;
    }

    @DBDomAttr(key = "changeOn")
    public void setChangeOn(String changeOn) {
        this.changeOn = changeOn;
    }

    @DBDomAttr(key = "backgroundColor")
    public void setBackgroundColor(String backgroundColor) {
        this.rawBackgroundColor = backgroundColor;
    }

    @DBDomAttr(key = "backgroundUrl")
    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    @DBDomAttr(key = "chainStyle")
    public void setChainStyle(String chainStyle) {
        this.chainStyle = chainStyle;
    }

    public int getId() {
        return id;
    }

    public int getLeftToLeft() {
        return leftToLeft;
    }

    public int getTopToTop() {
        return topToTop;
    }

    public String getChainStyle() {
        return chainStyle;
    }

    @Override
    public void processAttr() {
        super.processAttr();
        doProcessAttr(null);
    }

    @Override
    public void processAttr(JsonObject data) {
        super.processAttr(data);
        doProcessAttr(data);
    }

    private void doProcessAttr(JsonObject data) {
        width = DBScreenUtils.processSize(mDBContext, rawWidth, DBConstants.DEFAULT_SIZE_WIDTH);
        height = DBScreenUtils.processSize(mDBContext, rawHeight, DBConstants.DEFAULT_SIZE_HEIGHT);
        marginTop = DBScreenUtils.processSize(mDBContext, rawMarginTop, DBConstants.DEFAULT_SIZE_MARGIN);
        marginBottom = DBScreenUtils.processSize(mDBContext, rawMarginBottom, DBConstants.DEFAULT_SIZE_MARGIN);
        marginLeft = DBScreenUtils.processSize(mDBContext, rawMarginLeft, DBConstants.DEFAULT_SIZE_MARGIN);
        marginRight = DBScreenUtils.processSize(mDBContext, rawMarginRight, DBConstants.DEFAULT_SIZE_MARGIN);

        // width/height 为fill的填充父布局的逻辑处理
        if (null != rawWidth && rawWidth.equals(DBConstants.FILL_TYPE_FILL)) {
            width = 0;
            leftToLeft = DBConstants.DEFAULT_ID_ROOT;
            rightToRight = DBConstants.DEFAULT_ID_ROOT;
        }
        if (null != rawHeight && rawHeight.equals(DBConstants.FILL_TYPE_FILL)) {
            height = 0;
            topToTop = DBConstants.DEFAULT_ID_ROOT;
            bottomToBottom = DBConstants.DEFAULT_ID_ROOT;
        }

        backgroundColor = getVariableString(rawBackgroundColor);
        visibleOn = getVariableBoolean(rawVisibleOn, data);

        // observe meta [visibleOn]
        if (visibleOn) {
            mDBContext.observeData(new DBData.IDBObserveData<Boolean>() {
                @Override
                public void onDataChanged(DBTemplate template, String key, @Nullable Boolean oldValue, @NonNull Boolean newValue) {
                    Wrapper.get(mDBContext.getAccessKey()).log().d("key: " + key + " oldValue: " + oldValue + " newValue: " + newValue);
                    if (null != mNativeView) {
                        mNativeView.setVisibility(newValue ? View.VISIBLE : View.INVISIBLE);
                    }
                }

                @Override
                public String getDataKey() {
                    return rawVisibleOn;
                }
            });
        }

        // observe meta [changeOn]
        if (null != changeOn) {
            String[] keys = changeOn.split("\\|");
            for (final String key : keys) {
                mDBContext.observeData(new DBData.IDBObserveData<Object>() {
                    @Override
                    public void onDataChanged(DBTemplate template, String key, Object oldValue, Object newValue) {
                        Wrapper.get(mDBContext.getAccessKey()).log().d("key: " + key + " oldValue: " + oldValue + " newValue: " + newValue);
                        changeOnCallback(mNativeView, key, oldValue, newValue); // View 统一监听onChange事件
                    }

                    @Override
                    public String getDataKey() {
                        return key;
                    }
                });
            }
        }
    }

    @Override
    public void processRender(DBRootView parentView) {
        mNativeView = createView(parentView);
        doRender(mNativeView);
        // 布局
        ConstraintLayout.LayoutParams childLp = new ConstraintLayout.LayoutParams(width, height);
        doLayout(childLp);
        // 添加到父容器
        parentView.addView(mNativeView, childLp);
    }

    @Override
    public void recycleRender(DBRootView parentView) {
        doRender((V) parentView.getViewById(id));
    }

    @Override
    public void processAction() {
        super.processAction();

        processClick();
    }

    /**
     * subclass must override this to provide native view instance
     */
    protected abstract V createView(DBRootView parentView);

    /**
     * subclass must override this to process render logic
     */
    protected void doRender(V view) {
        // id
        if (id != DBConstants.DEFAULT_ID_VIEW) {
            view.setId(id);
        }
        // visibleOn
        if (DBUtils.isEmpty(rawVisibleOn) || visibleOn) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
        // background color
        if (!DBUtils.isEmpty(backgroundColor)) {
            view.setBackgroundColor(DBUtils.parseColor(this, backgroundColor));
        }
    }

    /**
     * subclass show override if has [changeOn] attribute
     */
    protected void changeOnCallback(V view, String key, Object oldValue, Object newValue) {
    }

    private void doLayout(ConstraintLayout.LayoutParams childLp) {
        // 位置
        if (leftToLeft != DBConstants.DEFAULT_ID_VIEW) {
            childLp.leftToLeft = leftToLeft;
        }
        if (leftToRight != DBConstants.DEFAULT_ID_VIEW) {
            childLp.leftToRight = leftToRight;
        }
        if (rightToRight != DBConstants.DEFAULT_ID_VIEW) {
            childLp.rightToRight = rightToRight;
        }
        if (rightToLeft != DBConstants.DEFAULT_ID_VIEW) {
            childLp.rightToLeft = rightToLeft;
        }
        if (topToTop != DBConstants.DEFAULT_ID_VIEW) {
            childLp.topToTop = topToTop;
        }
        if (topToBottom != DBConstants.DEFAULT_ID_VIEW) {
            childLp.topToBottom = topToBottom;
        }
        if (bottomToBottom != DBConstants.DEFAULT_ID_VIEW) {
            childLp.bottomToBottom = bottomToBottom;
        }
        if (bottomToTop != DBConstants.DEFAULT_ID_VIEW) {
            childLp.bottomToTop = bottomToTop;
        }
        // 边距
        childLp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    private void processClick() {
        List<IDBAction> clickActions = mDBActionPool.getClickAction();
        if (clickActions == null || clickActions.isEmpty()) {
            return;
        }

        mNativeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<IDBAction> actionList = mDBActionPool.getClickAction();
                if (null != actionList) {
                    for (IDBAction action : actionList) {
                        action.invoke();
                    }
                }
            }
        });
    }
}
