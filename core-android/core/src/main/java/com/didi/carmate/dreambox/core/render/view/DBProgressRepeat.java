package com.didi.carmate.dreambox.core.render.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * author: chenjing
 * date: 2020/6/28
 */
public class DBProgressRepeat extends View {
    public DBProgressRepeat(Context context) {
        this(context, null);
    }

    public DBProgressRepeat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setBackground(Drawable background) {
        if (!(background instanceof BitmapDrawable)) {
            return;
        }

        Bitmap bitmap = ((BitmapDrawable) background).getBitmap();
        DBRepeatPatchDrawable drawable = PatchDrawableFactory.createRepeatPatchDrawable(getResources(), bitmap);
        super.setBackground(drawable);
    }
}
