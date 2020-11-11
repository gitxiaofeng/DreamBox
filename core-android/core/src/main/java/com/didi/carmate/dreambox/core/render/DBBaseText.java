package com.didi.carmate.dreambox.core.render;

import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.utils.DBScreenUtils;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public abstract class DBBaseText<V extends TextView> extends DBBaseView<V> {
    private Map<String, Integer> mapGravity = new HashMap<>();
    private String rawSize;
    private String rawColor;
    private String rawStyle;
    private String rawGravity;

    String rawSrc;
    String src;
    int size;
    String color;
    String style;
    int gravity;

    protected DBBaseText() {
        mapGravity.put(DBConstants.STYLE_GRAVITY_LEFT, Gravity.LEFT);
        mapGravity.put(DBConstants.STYLE_GRAVITY_RIGHT, Gravity.RIGHT);
        mapGravity.put(DBConstants.STYLE_GRAVITY_TOP, Gravity.TOP);
        mapGravity.put(DBConstants.STYLE_GRAVITY_BOTTOM, Gravity.BOTTOM);
        mapGravity.put(DBConstants.STYLE_GRAVITY_CENTER, Gravity.CENTER);
    }

    @DBDomAttr(key = "gravity")
    public void setGravity(String gravity) {
        this.rawGravity = gravity;
    }

    @DBDomAttr(key = "src")
    public void setRawSrc(String rawSrc) {
        this.rawSrc = rawSrc;
    }

    @DBDomAttr(key = "size")
    public void setRawSize(String rawSize) {
        this.rawSize = rawSize;
    }

    @DBDomAttr(key = "color")
    public void setRawColor(String rawColor) {
        this.rawColor = rawColor;
    }

    @DBDomAttr(key = "style")
    public void setRawStyle(String rawStyle) {
        this.rawStyle = rawStyle;
    }

    @Override
    public void processAttr() {
        super.processAttr();

        src = getVariableString(rawSrc);
        processNormalAttr();
    }

    @Override
    public void processAttr(JsonObject dict) {
        super.processAttr(dict);

        src = getVariableString(rawSrc, dict);
        processNormalAttr();
    }

    @CallSuper
    protected void doRender(V view) {
        super.doRender(view);

        // gravity
        if (gravity != 0) {
            view.setGravity(gravity);
        }
    }

    private void processNormalAttr() {
        String rawSizePool = getVariableString(rawSize);
        size = DBScreenUtils.processSize(mDBContext, rawSizePool, DBConstants.DEFAULT_SIZE_TEXT);
        color = getVariableString(rawColor);
        style = getVariableString(rawStyle);
        gravity = convertGravity(rawGravity);
    }

    private int convertGravity(String gravity) {
        if (null == gravity) {
            return 0;
        }
        String[] gravityArr = gravity.split("\\|");
        int iGravity = 0;
        for (String strGravity : gravityArr) {
            Integer tmp = mapGravity.get(strGravity);
            if (null != tmp) {
                iGravity |= tmp;
            }
        }
        return iGravity;
    }
}
