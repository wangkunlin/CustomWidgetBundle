package com.wkl.widget.bundle.vsb;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.wkl.widget.bundle.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by <a href="mailto:kunlin.wang@mtime.com">Wang kunlin</a>
 * <p>
 * On 2018-08-06
 */

public class VerticalSeekBar extends View {

    public interface OnSeekBarChangeListener {

        void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(VerticalSeekBar seekBar);

        void onStopTrackingTouch(VerticalSeekBar seekBar);
    }

    private List<OnSeekBarChangeListener> mOnSeekBarChangeListeners = new ArrayList<>();

    public void addOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListeners.add(l);
    }

    public void removeOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListeners.remove(l);
    }

    private static final int MAX_LEVEL = 10000;

    public VerticalSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public VerticalSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private int mMax;
    private int mProgress;
    private Drawable mProgressDrawable;
    private Drawable mCurrentDrawable;
    private Drawable mThumb;
    private boolean mIsDragging = false;
    private int mScaledTouchSlop;
    private float mTouchDownX;

    private String mText = "Seek";

    private void init(Context context, AttributeSet attrs) {
        initProgressBar();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VerticalSeekBar);
        int count = ta.getIndexCount();
        for (int i = 0; i < count; i++) {
            int index = ta.getIndex(i);
            if (index == R.styleable.VerticalSeekBar_android_max) {
                setMax(ta.getInt(index, 0));
            } else if (index == R.styleable.VerticalSeekBar_android_progress) {
                setProgress(ta.getInt(index, 0));
            } else if (index == R.styleable.VerticalSeekBar_android_progressDrawable) {
                setProgressDrawable(ta.getDrawable(index));
            } else if (index == R.styleable.VerticalSeekBar_android_thumb) {
                setThumb(ta.getDrawable(index));
            } else if (index == R.styleable.VerticalSeekBar_android_text) {
                setText(ta.getString(index));
            }
        }
        ta.recycle();
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int dw = 0;
        int dh = 0;
        final Drawable d = mCurrentDrawable;
        int thumbWidth = mThumb == null ? 0 : mThumb.getIntrinsicWidth();
        if (d != null) {
            dw = d.getIntrinsicWidth();
            dh = d.getIntrinsicHeight();
        }

        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();
        dw = Math.max(dw, thumbWidth);

        setMeasuredDimension(resolveSizeAndState(dw, widthMeasureSpec, 0),
                resolveSizeAndState(dh, heightMeasureSpec, 0));
    }

    public void setThumb(Drawable thumb) {
        final boolean needUpdate;
        // This way, calling setThumb again with the same bitmap will result in
        // it recalcuating mThumbOffset (if for example it the bounds of the
        // drawable changed)
        if (mThumb != null && thumb != mThumb) {
            mThumb.setCallback(null);
            needUpdate = true;
        } else {
            needUpdate = false;
        }

        if (thumb != null) {
            thumb.setCallback(this);

            // If we're updating get the new states
            if (needUpdate &&
                    (thumb.getIntrinsicWidth() != mThumb.getIntrinsicWidth()
                            || thumb.getIntrinsicHeight() != mThumb.getIntrinsicHeight())) {
                requestLayout();
            }
        }

        mThumb = thumb;

        invalidate();

        if (needUpdate) {
            updateThumbAndTrackPos(getWidth(), getHeight());
            if (thumb != null && thumb.isStateful()) {
                // Note that if the states are different this won't work.
                // For now, let's consider that an app bug.
                int[] state = getDrawableState();
                thumb.setState(state);
            }
        }
    }

    private void updateThumbAndTrackPos(int w, int h) {
        final Drawable track = mCurrentDrawable;
        final Drawable thumb = mThumb;

        int trackBottom = h - getPaddingBottom();
        int trackRight = w - getPaddingRight();
        if (track != null) {
            track.setBounds(getPaddingLeft(), getPaddingTop(), trackRight, trackBottom);
        }

        if (thumb != null) {
            setThumbPos(thumb, mProgress * 1f / mMax);
        }
    }

    private void setThumbPos(Drawable thumb, float progress) {
        if (thumb == null) return;
        int available = getHeight() - getPaddingTop() - getPaddingBottom();

        final int thumbPos = getPaddingTop() + (int) ((1 - progress) * available + 0.5f);

        final int thumbWidth = thumb.getIntrinsicWidth();
        final int thumbHeight = thumb.getIntrinsicHeight();
        int left = (getWidth() - thumbWidth) / 2;
        int top = thumbPos - thumbHeight / 2;

        thumb.setBounds(left, top, left + thumbWidth, top + thumbHeight);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        boolean changed = false;
        if (mCurrentDrawable != null && mCurrentDrawable.isStateful()) {
            changed = mCurrentDrawable.setState(getDrawableState());
        }
        if (changed) {
            invalidate();
        }

        final Drawable thumb = mThumb;
        if (thumb != null && thumb.isStateful()
                && thumb.setState(getDrawableState())) {
            invalidateDrawable(thumb);
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return who == mThumb || who == mProgressDrawable || super.verifyDrawable(who);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrack(canvas);
        drawThumb(canvas);
    }

    public void setText(String text) {
        mText = text;
    }

    public String getText() {
        return mText;
    }

    private void drawThumb(Canvas canvas) {
        if (mThumb != null) {
            mThumb.draw(canvas);
        }
    }

    /**
     * Draws the progress bar track.
     */
    private void drawTrack(Canvas canvas) {
        final Drawable d = mCurrentDrawable;
        if (d != null) {
            d.draw(canvas);
        }
    }

    private void initProgressBar() {
        mMax = 100;
        mProgress = 0;
    }

    public void setMax(int max) {
        if (max < 0) {
            mMax = 0;
        }
        if (mMax != max) {
            mMax = max;
            postInvalidate();
            if (mProgress > max) {
                mProgress = max;
            }
            doRefreshProgress(android.R.id.progress, mProgress, false);
        }
    }

    public int getProgress() {
        return mProgress;
    }

    private static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    public void setProgress(int progress) {
        progress = constrain(progress, 0, mMax);
        if (progress == mProgress) {
            return;
        }
        mProgress = progress;
        doRefreshProgress(android.R.id.progress, mProgress, false);
    }

    private void doRefreshProgress(int id, int progress, boolean fromUser) {
        int range = mMax;
        final float scale = range > 0 ? progress / (float) range : 0;
        setVisualProgress(id, scale);
        onProgressRefresh(fromUser, progress);
    }

    private void setVisualProgress(int id, float progress) {
        Drawable d = mCurrentDrawable;

        if (d instanceof LayerDrawable) {
            d = ((LayerDrawable) d).findDrawableByLayerId(id);
            if (d == null) {
                // If we can't find the requested layer, fall back to setting
                // the level of the entire drawable. This will break if
                // progress is set on multiple elements, but the theme-default
                // drawable will always have all layer IDs present.
                d = mCurrentDrawable;
            }
        }

        if (d != null) {
            final int level = (int) (progress * MAX_LEVEL);
            d.setLevel(level);
        } else {
            invalidate();
        }
        setThumbPos(mThumb, progress);
        invalidate();
    }

    public Drawable getProgressDrawable() {
        return mProgressDrawable;
    }

    public void setProgressDrawable(Drawable d) {
        if (mProgressDrawable != d) {
            if (mProgressDrawable != null) {
                mProgressDrawable.setCallback(null);
                unscheduleDrawable(mProgressDrawable);
            }

            mProgressDrawable = d;

            if (d != null) {
                d.setCallback(this);
                if (d.isStateful()) {
                    d.setState(getDrawableState());
                }
            }
            swapCurrentDrawable(d);
            postInvalidate();
            updateDrawableBounds(getWidth(), getHeight());

            doRefreshProgress(android.R.id.progress, mProgress, false);
        }
    }

    private void swapCurrentDrawable(Drawable newDrawable) {
        final Drawable oldDrawable = mCurrentDrawable;
        mCurrentDrawable = newDrawable;

        if (oldDrawable != mCurrentDrawable) {
            if (oldDrawable != null) {
                oldDrawable.setVisible(false, false);
            }
            if (mCurrentDrawable != null) {
                mCurrentDrawable.setVisible(getWindowVisibility() == VISIBLE && isShown(), false);
            }
        }
    }

    private boolean isInScrollingContainer() {
        ViewParent p = getParent();
        while (p != null && p instanceof ViewGroup) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInScrollingContainer()) {
                    mTouchDownX = event.getY();
                } else {
                    startDrag(event);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsDragging) {
                    trackTouchEvent(event);
                } else {
                    final float x = event.getY();
                    if (Math.abs(x - mTouchDownX) > mScaledTouchSlop) {
                        startDrag(event);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold should
                    // be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }
                // ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private void startDrag(MotionEvent event) {
        setPressed(true);

        if (mThumb != null) {
            // This may be within the padding region.
            invalidate(mThumb.getBounds());
        }

        onStartTrackingTouch();
        trackTouchEvent(event);
        attemptClaimDrag();
    }

    private void trackTouchEvent(MotionEvent event) {
        final int x = Math.round(event.getY());
        final int width = getHeight();
        final int availableWidth = width - getPaddingTop() - getPaddingBottom();

        final float scale;
        float progress = 0.0f;
        if (x < getPaddingTop()) {
            scale = 1f;
        } else if (x > width - getPaddingBottom()) {
            scale = 0.0f;
        } else {
            scale = (width - x - getPaddingBottom()) / (float) availableWidth;
        }

        final int range = getMax();
        progress += scale * range;

        setProgressInternal(Math.round(progress));
    }

    private void setProgressInternal(int progress) {
        if (progress == mProgress) {
            // No change from current.
            return;
        }

        mProgress = progress;
        doRefreshProgress(android.R.id.progress, mProgress, true);
    }

    public int getMax() {
        return mMax;
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateDrawableBounds(w, h);
        updateThumbAndTrackPos(w, h);
    }

    private void updateDrawableBounds(int w, int h) {
        int right = w - getPaddingRight();
        int bottom = h - getPaddingBottom();
        if (mProgressDrawable != null) {
            mProgressDrawable.setBounds(getPaddingLeft(), getPaddingTop(), right, bottom);
        }
    }


    private void onProgressRefresh(boolean fromUser, int progress) {
        dispatchOnProgressRefresh(fromUser, progress);
    }

    private void onStartTrackingTouch() {
        mIsDragging = true;
        dispatchOnStartTrackingTouch();
    }

    private void onStopTrackingTouch() {
        mIsDragging = false;
        dispatchOnStopTrackingTouch();
    }

    private void dispatchOnProgressRefresh(boolean fromUser, int progress) {
        for (OnSeekBarChangeListener l : mOnSeekBarChangeListeners) {
            if (l == null) {
                continue;
            }
            l.onProgressChanged(this, progress, fromUser);
        }
    }

    private void dispatchOnStartTrackingTouch() {
        for (OnSeekBarChangeListener l : mOnSeekBarChangeListeners) {
            if (l == null) {
                continue;
            }
            l.onStartTrackingTouch(this);
        }
    }

    private void dispatchOnStopTrackingTouch() {
        for (OnSeekBarChangeListener l : mOnSeekBarChangeListeners) {
            if (l == null) {
                continue;
            }
            l.onStopTrackingTouch(this);
        }
    }
}
