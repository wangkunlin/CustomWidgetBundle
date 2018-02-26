package com.wkl.widget.bundle.fiv;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.Gravity;

/**
 * Created by <a href="mailto:kunlin.wang@mtime.com">Wang kunlin</a>
 * <p>
 * On 2017-09-18
 * <p>
 * 提供对 imageview 设置前景的 view
 * <p>
 * 支持设置 gravity  ，如果不设置 则 前景 drawable 会铺满
 */

public class ForegroundImageView extends AppCompatImageView {

    private Drawable mForeground;
    private int mForegroundGravity = Gravity.NO_GRAVITY;
    private Rect mSelfBounds;
    private Rect mOverlayBounds;

    private static final int[] FOREGROUND_ATTRS = new int[]{
            android.R.attr.foreground,
            android.R.attr.foregroundGravity
    };

    public ForegroundImageView(Context context) {
        super(context);
        init(context, null);
    }

    public ForegroundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ForegroundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mSelfBounds = new Rect();
        mOverlayBounds = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, FOREGROUND_ATTRS);
        setForegroundDrawable(a.getDrawable(0));
        setForegroundGravity(a.getInt(1, Gravity.NO_GRAVITY));
        a.recycle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            super.setForeground(null);
        }
    }

    @Override
    public void setForeground(Drawable foreground) {
        setForegroundDrawable(foreground);
    }

    @Override
    public Drawable getForeground() {
        return mForeground;
    }

    public Drawable getForegroundDrawable() {
        return mForeground;
    }

    public void setForegroundDrawable(Drawable foreground) {
        if (mForeground == foreground) return;
        if (mForeground != null) {
            mForeground.setCallback(null);
            unscheduleDrawable(mForeground);
        }
        mForeground = foreground;
        if (foreground != null) {
            if (foreground.isStateful()) {
                foreground.setState(getDrawableState());
            }
            foreground.setCallback(this);
        }
        requestLayout();
        invalidate();
    }

    @Override
    public void setForegroundGravity(int gravity) {
        if (mForegroundGravity != gravity) {
            if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.START;
            }

            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.TOP;
            }

            mForegroundGravity = gravity;
            requestLayout();
        }
    }

    @Override
    public int getForegroundGravity() {
        return mForegroundGravity;
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (mForeground != null) {
            mForeground.setVisible(isVisible, false);
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable dr) {
        return dr == mForeground || super.verifyDrawable(dr);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        boolean changed = false;
        if (mForeground != null) {
            changed = mForeground.setState(getDrawableState());
        }
        if (changed) {
            invalidate();
        }
    }

    public void setForegroundColor(@ColorInt int color) {
        setForegroundDrawable(new ColorDrawable(color));
    }

    public void setForegroundResource(@DrawableRes int res) {
        if (res != 0) {
            setForegroundDrawable(getResources().getDrawable(res));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mForeground != null) {
            mSelfBounds.set(0, 0, getWidth(), getHeight());
            if (mForegroundGravity == Gravity.NO_GRAVITY
                    || mForeground instanceof ColorDrawable) {
                mOverlayBounds.set(mSelfBounds);
            } else {
                Gravity.apply(mForegroundGravity, mForeground.getIntrinsicWidth()
                        , mForeground.getIntrinsicHeight(), mSelfBounds, mOverlayBounds);
            }
            mForeground.setBounds(mOverlayBounds);
            mForeground.draw(canvas);
        }
    }


}
