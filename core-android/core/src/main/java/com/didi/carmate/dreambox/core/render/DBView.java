package com.didi.carmate.dreambox.core.render;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;

import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.render.view.DBRichView;
import com.didi.carmate.dreambox.core.render.view.DBRootView;
import com.didi.carmate.dreambox.core.utils.DBScreenUtils;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.google.gson.JsonObject;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBView extends DBBaseView<DBRichView> {
    private String shape; // 绘制形状
    private String rawRadius; // 圆角半径
    private String rawBorderWidth; // 边框宽度
    private String rawBorderColor; // 边框颜色
    private String gradientColor; // 过渡颜色
    private String gradientOrientation; // 过渡色方向

    private int radius;
    private int borderWidth;
    private String borderColor;

    @DBDomAttr(key = "shape")
    public void setShape(String shape) {
        this.shape = shape;
    }

    @DBDomAttr(key = "radius")
    public void setRadius(String radius) {
        this.rawRadius = radius;
    }

    @DBDomAttr(key = "borderWidth")
    public void setBorderWidth(String borderWidth) {
        this.rawBorderWidth = borderWidth;
    }

    @DBDomAttr(key = "borderColor")
    public void setBorderColor(String borderColor) {
        this.rawBorderColor = borderColor;
    }

    @DBDomAttr(key = "gradientColor")
    public void setGradientColor(String gradientColor) {
        this.gradientColor = gradientColor;
    }

    @DBDomAttr(key = "gradientOrientation")
    public void setGradientOrientation(String gradientOrientation) {
        this.gradientOrientation = gradientOrientation;
    }

    @Override
    public void processAttr() {
        super.processAttr();

        radius = DBScreenUtils.processSize(mDBContext, rawRadius, 0);
        borderWidth = DBScreenUtils.processSize(mDBContext, rawBorderWidth, 0);
        borderColor = getVariableString(rawBorderColor);
    }

    @Override
    public void processAttr(JsonObject dict) {
        super.processAttr(dict);

        radius = DBScreenUtils.processSize(mDBContext, rawRadius, 0);
        borderWidth = DBScreenUtils.processSize(mDBContext, rawBorderWidth, 0);
        borderColor = getVariableString(rawBorderColor, dict);
    }

    @Override
    protected DBRichView createView(DBRootView parentView) {
        return new DBRichView(parentView.getContext());
    }

    @Override
    protected void doRender(DBRichView richView) {
        super.doRender(richView);

        // 覆写父类背景颜色
        richView.setBackgroundColor(Color.TRANSPARENT);
//        // pressed颜色
//        richView.setPressed(false);
//        richView.setCoverColor(Color.parseColor("#66AAAA"));
        // 边框宽度
        if (borderWidth > 0) {
            richView.setBorderWidth(borderWidth);
        }
        // 边框颜色
        if (!DBUtils.isEmpty(borderColor)) {
            richView.setBorderColor(DBUtils.parseColor(this, borderColor));
        }
        // 背景色
        if (!DBUtils.isEmpty(gradientColor)) {
            GradientDrawable dynamicDrawable = new GradientDrawable();
            dynamicDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            dynamicDrawable.setUseLevel(false);
            // 过渡方向
            if (DBConstants.STYLE_ORIENTATION_H.equals(gradientOrientation)) {
                dynamicDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            } else {
                dynamicDrawable.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
            }
            // 过渡颜色
            String[] colorsStr = gradientColor.split("-");
            int[] colors = new int[colorsStr.length];
            for (int i = 0; i < colorsStr.length; i++) {
                String color = colorsStr[i];
                if (color.charAt(0) != '#' || (color.length() != 7 && color.length() != 9)) {
                    StringBuilder errorMsg = new StringBuilder();
                    if (id != DBConstants.DEFAULT_ID_VIEW) {
                        errorMsg.append("id: ").append(id).append(",");
                    }
                    errorMsg.append(" type: ").append(getClass().getSimpleName())
                            .append(", gradient color error: ").append(gradientColor);
                    throw new IllegalArgumentException(errorMsg.toString());
                }

                colors[i] = Color.parseColor(color);
            }
            dynamicDrawable.setColors(colors);
            richView.setImageDrawable(dynamicDrawable);
        } else if (!DBUtils.isEmpty(backgroundColor)) {
            richView.setImageDrawable(new ColorDrawable(Color.parseColor(backgroundColor)));
        } else {
            richView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 形状
        if (DBConstants.STYLE_VIEW_SHAPE_CIRCLE.equals(shape)) {
            richView.setShape(DBRichView.SHAPE_CIRCLE);
        } else {
            richView.setShape(DBRichView.SHAPE_REC);
        }
        richView.setRoundRadius(radius);
    }

    public static String getNodeTag() {
        return "view";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBView createNode() {
            return new DBView();
        }
    }
}
