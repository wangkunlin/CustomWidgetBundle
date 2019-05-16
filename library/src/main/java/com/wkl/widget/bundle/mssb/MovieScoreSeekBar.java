package com.wkl.widget.bundle.mssb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.wkl.widget.bundle.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by <a href="mailto:wangkunlin1992@gmail.com">Wang kunlin</a>
 * <p>
 * On 2019-05-15
 */
public class MovieScoreSeekBar extends View {
    private static final int MAX_LEVEL = 10000;

    private int mSegmentCount = 10;
    private float mCornerRadius = 0;

    private Drawable mProgressDrawable;
    private int mSegmentPadding = 0;
    private boolean mShowText = true;
    private int mScore = 0;
    private Drawable mCurrentDrawable;

    private TextPaint mTextPaint;
    private Paint mPaint;

    private Rect mBounds = new Rect();
    private RectF mSegmentBounds = new RectF();

    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private boolean mIsDragging;
    private float mTouchDownX;
    private int mScaledTouchSlop;
    private PorterDuffXfermode mXfermode;

    public MovieScoreSeekBar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public MovieScoreSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MovieScoreSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MovieScoreSeekBar, defStyleAttr, 0);

        mSegmentCount = a.getInt(R.styleable.MovieScoreSeekBar_segmentCount, mSegmentCount);
        mCornerRadius = a.getDimension(R.styleable.MovieScoreSeekBar_cornerRadius, 0);

        if (isInEditMode()) {
            setScore(1);
        }

        setProgressDrawable(a.getDrawable(R.styleable.MovieScoreSeekBar_android_progressDrawable));
        float textSize1 = a.getDimension(R.styleable.MovieScoreSeekBar_android_textSize, textSize);
        int textColor = a.getColor(R.styleable.MovieScoreSeekBar_android_textColor, Color.WHITE);
        int textStyle = a.getInt(R.styleable.MovieScoreSeekBar_android_textStyle, -1);
        mSegmentPadding = a.getDimensionPixelSize(R.styleable.MovieScoreSeekBar_segmentPadding, 3);
        mShowText = a.getBoolean(R.styleable.MovieScoreSeekBar_showText, true);
        int maskColor = a.getColor(R.styleable.MovieScoreSeekBar_maskColor, Color.WHITE);

        a.recycle();

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTypeface(Typeface.DEFAULT);

        mTextPaint.setTextSize(textSize1);
        mTextPaint.setColor(textColor);

        setTextStyleInner(textStyle);
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(maskColor);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        mPaint.setStyle(Paint.Style.FILL);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
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

            doRefreshProgress(mScore, false);
        }
    }

    public void setScore(int score) {
        score = constrain(score, mSegmentCount);
        if (score == mScore) {
            return;
        }
        mScore = score;
        doRefreshProgress(mScore, true);
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (mProgressDrawable != null) {
            mProgressDrawable.setVisible(isVisible, false);
        }
    }

    private static int constrain(int amount, int high) {
        return amount < 0 ? 0 : (amount > high ? high : amount);
    }

    private void updateDrawableBounds(int w, int h) {
        w -= getPaddingRight() + getPaddingLeft();
        h -= getPaddingTop() + getPaddingBottom();

        int right = w;
        int bottom = h;
        if (mProgressDrawable != null) {
            mProgressDrawable.setBounds(0, 0, right, bottom);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        boolean changed = false;
        if (mProgressDrawable != null) {
            changed = mProgressDrawable.setState(getDrawableState());
        }
        if (changed) {
            invalidate();
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable dr) {
        return dr == mProgressDrawable || super.verifyDrawable(dr);
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

    @Retention(RetentionPolicy.CLASS)
    @IntDef({Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC, Typeface.BOLD_ITALIC})
    @interface TextStyle {
    }

    public void setTextStyle(@TextStyle int style) {
        setTextStyleInner(style);
    }

    private void setTextStyleInner(int style) {
        if (style > 0) {
            Typeface tf = mTextPaint.getTypeface();

            // now compute what (if any) algorithmic styling is needed
            int typefaceStyle = tf != null ? tf.getStyle() : 0;
            int need = style & ~typefaceStyle;
            mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
            mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
        } else {
            mTextPaint.setFakeBoldText(false);
            mTextPaint.setTextSkewX(0);
        }
        invalidate();
    }

    public void setTextColor(@ColorInt int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    /**
     * sp 为单位
     *
     * @param size sp
     */
    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setTextSize(int unit, float size) {
        setTextSizeInternal(unit, size);
    }

    private void setTextSizeInternal(int unit, float size) {
        Context c = getContext();
        Resources r = c.getResources();
        mTextPaint.setTextSize(TypedValue.applyDimension(unit, size, r.getDisplayMetrics()));
    }

    public void setShowText(boolean showText) {
        if (mShowText != showText) {
            mShowText = showText;
            invalidate();
        }
    }

    public void setSegmentCount(int segmentCount) {
        if (segmentCount <= 0) {
            segmentCount = 10;
        }
        if (mSegmentCount != segmentCount) {
            mSegmentCount = segmentCount;
            invalidate();
        }
    }

    public void setSegmentPadding(int segmentPadding) {
        if (segmentPadding < 0) {
            segmentPadding = 0;
        }
        if (mSegmentPadding != segmentPadding) {
            mSegmentPadding = segmentPadding;
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBounds.set(0, 0, w, h);
        updateDrawableBounds(w, h);
        updateThumbAndTrackPos(w, h);
    }

    private void updateThumbAndTrackPos(int w, int h) {
        final int paddedHeight = h - getPaddingTop() - getPaddingBottom();
        final Drawable track = mCurrentDrawable;


        // Apply offset to whichever item is taller.
        final int trackOffset = getPaddingTop();

        if (track != null) {
            final int trackWidth = w - getPaddingLeft() - getPaddingRight();
            track.setBounds(0, trackOffset, trackWidth, trackOffset + paddedHeight);
        }
    }

    private void doRefreshProgress(int progress, boolean callBackToApp) {
        int range = mSegmentCount;
        final float scale = range > 0 ? progress / (float) range : 0;
        setVisualProgress(scale);
        if (callBackToApp) {
            onProgressRefresh(progress);
        }
    }

    private void onProgressRefresh(int progress) {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, progress);
        }
    }

    private void setVisualProgress(float scale) {
        Drawable d = mCurrentDrawable;

        if (d instanceof LayerDrawable) {
            d = ((LayerDrawable) d).findDrawableByLayerId(android.R.id.progress);
            if (d == null) {
                // If we can't find the requested layer, fall back to setting
                // the level of the entire drawable. This will break if
                // progress is set on multiple elements, but the theme-default
                // drawable will always have all layer IDs present.
                d = mCurrentDrawable;
            }
        }

        if (d != null) {
            final int level = (int) (scale * MAX_LEVEL);
            d.setLevel(level);
            invalidate();
        } else {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawTrack(canvas);

        int segmentWidth = (mBounds.width() - getPaddingRight() - getPaddingLeft() -
                (mSegmentCount - 1) * mSegmentPadding) / mSegmentCount;

        drawMask(canvas, segmentWidth);
        drawText(canvas, segmentWidth);
    }

    private void drawText(Canvas canvas, int segmentWidth) {
        if (!mShowText) {
            return;
        }
        int score = mScore;
        if (score <= 0) {
            return;
        }
        int left = getPaddingLeft() + (segmentWidth + mSegmentPadding) * (score - 1);
        int top = getPaddingTop();
        int right = left + segmentWidth;
        int bottom = mBounds.height() - getPaddingBottom();

        float centerY = (top + bottom) >> 1;
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float baseLine = centerY - (fm.bottom - fm.top) / 2 - fm.top;
        String text = String.valueOf(score);
        float textWidth = mTextPaint.measureText(text);
        float x = ((left + right) >> 1) - textWidth / 2;
        canvas.drawText(text, x, baseLine, mTextPaint);
    }

    private void drawMask(Canvas canvas, int segmentWidth) {
        int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);

        canvas.drawRect(getPaddingLeft(),
                getPaddingTop(),
                mBounds.width() - getPaddingRight(),
                mBounds.height() - getPaddingBottom(), mPaint);

        mSegmentBounds.set(getPaddingLeft(),
                getPaddingTop(),
                getPaddingLeft() + segmentWidth,
                mBounds.height() - getPaddingBottom());
        mPaint.setXfermode(mXfermode);

        for (int i = 0; i < mSegmentCount; i++) {
            if (i > 0) {
                mSegmentBounds.offset(segmentWidth + mSegmentPadding, 0);
            }
            canvas.drawRoundRect(
                    mSegmentBounds, mCornerRadius, mCornerRadius, mPaint
            );
        }
        mPaint.setXfermode(null);
        canvas.restoreToCount(sc);
    }

    /**
     * Draws the progress bar track.
     */
    private void drawTrack(Canvas canvas) {
        final Drawable d = mCurrentDrawable;
        if (d != null) {
            final int saveCount = canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            d.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }


    private boolean isInScrollingContainer() {
        ViewParent p = getParent();
        while (p instanceof ViewGroup) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInScrollingContainer()) {
                    mTouchDownX = event.getX();
                } else {
                    startDrag(event);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsDragging) {
                    trackTouchEvent(event);
                } else {
                    final float x = event.getX();
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

    void onStopTrackingTouch() {
        mIsDragging = false;
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    private void startDrag(MotionEvent event) {
        setPressed(true);

        onStartTrackingTouch();
        trackTouchEvent(event);
        attemptClaimDrag();
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }


    void onStartTrackingTouch() {
        mIsDragging = true;
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        final int x = (int) Math.ceil(event.getX());
        final int width = getWidth();
        final int availableWidth = width - getPaddingLeft() - getPaddingRight();

        final float scale;
        float progress = 0.0f;
        if (x < getPaddingLeft()) {
            scale = 0.0f;
        } else if (x > width - getPaddingRight()) {
            scale = 1.0f;
        } else {
            scale = (x - getPaddingLeft()) / (float) availableWidth;
        }

        final int range = mSegmentCount;
        progress += scale * range;

        setScore((int) Math.ceil(progress));
    }


    public interface OnSeekBarChangeListener {
        void onProgressChanged(MovieScoreSeekBar seekBar, int progress);

        void onStartTrackingTouch(MovieScoreSeekBar seekBar);

        void onStopTrackingTouch(MovieScoreSeekBar seekBar);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
    }
}
