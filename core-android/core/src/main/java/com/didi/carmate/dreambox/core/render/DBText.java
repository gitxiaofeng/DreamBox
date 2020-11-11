package com.didi.carmate.dreambox.core.render;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.render.view.DBRootView;
import com.didi.carmate.dreambox.core.utils.DBUtils;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBText extends DBBaseText<TextView> {

    @Override
    protected TextView createView(DBRootView parentView) {
        return new TextView(parentView.getContext());
    }

    @Override
    protected void doRender(TextView textView) {
        super.doRender(textView);

        // text
        if (!DBUtils.isEmpty(src)) {
            src = src.replace("\\n", "\n");
            textView.setText(src);
        }
        // color
        if (!DBUtils.isEmpty(color)) {
            textView.setTextColor(DBUtils.parseColor(this, color));
        }
        // size
        if (size != DBConstants.DEFAULT_SIZE_TEXT) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
        // style
        if (!DBUtils.isEmpty(style)) {
            if (style.equals(DBConstants.STYLE_TXT_NORMAL)) {
                textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            } else if (style.equals(DBConstants.STYLE_TXT_BOLD)) {
                textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
        }
    }

    @Override
    public void changeOnCallback(TextView view, String key, Object oldValue, Object newValue) {
        if (null != newValue && null != view) {
            src = getVariableString(rawSrc);
            view.setText(src);
        }
    }

    public static String getNodeTag() {
        return "text";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBText createNode() {
            return new DBText();
        }
    }
}
