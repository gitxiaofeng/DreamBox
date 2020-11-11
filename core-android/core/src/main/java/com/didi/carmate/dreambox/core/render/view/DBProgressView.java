package com.didi.carmate.dreambox.core.render.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.IntRange;

import com.didi.carmate.dreambox.core.base.DBConstants;

/**
 * author: chenjing
 * date: 2020/5/23
 */
public class DBProgressView extends FrameLayout {
    private int progress;
    private View foregroundView;
    private String patchType;
    private boolean isNinePatchBg;
    private boolean isNinePatchFg;

    public DBProgressView(Context context, String patchType, boolean isNinePatchBg, boolean isNinePatchFg) {
        this(context, null, patchType, isNinePatchBg, isNinePatchFg);
    }

    public DBProgressView(Context context, AttributeSet attrs, String patchType, boolean isNinePatchBg, boolean isNinePatchFg) {
        super(context, attrs);
        this.patchType = patchType;
        this.isNinePatchBg = isNinePatchBg;
        this.isNinePatchFg = isNinePatchFg;
        init();
    }

    private void init() {
        if (isNinePatchFg) {
            if (patchType.equals(DBConstants.STYLE_PATCH_TYPE_REPEAT)) {
                foregroundView = new DBProgressRepeat(getContext());
            } else {
                foregroundView = new DBProgressStretch(getContext());
            }
        } else {
            ImageView imageView = new ImageView(getContext());
            foregroundView = imageView;
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        addView(foregroundView, lp);
    }

    @Override
    public void setBackground(Drawable background) {
        if (!(background instanceof BitmapDrawable)) {
            return;
        }

        Drawable backgroundDrawable;
        if (isNinePatchBg) {
            Bitmap bitmap = ((BitmapDrawable) background).getBitmap();
            if (patchType.equals(DBConstants.STYLE_PATCH_TYPE_REPEAT)) {
                backgroundDrawable = PatchDrawableFactory.createRepeatPatchDrawable(getResources(), bitmap);
            } else {
                backgroundDrawable = PatchDrawableFactory.createNinePatchDrawable(getResources(), bitmap);
            }
        } else {
            backgroundDrawable = background;
        }
        super.setBackground(backgroundDrawable);
    }

    public void setProgress(@IntRange(from = 0, to = 100) int progress) {
        int value;
        if (progress > 100) {
            value = 100;
        } else if (progress < 0) {
            value = 0;
        } else {
            value = progress;
        }
        this.progress = value;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        ViewGroup.LayoutParams lp = foregroundView.getLayoutParams();
        lp.width = Math.max((w * progress) / 100, 20); // 影响绘制效果，最少20个像素
        lp.height = h;
        foregroundView.setLayoutParams(lp);
    }

    public View getForegroundView() {
        return foregroundView;
    }
}
