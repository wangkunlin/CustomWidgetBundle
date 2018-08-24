package com.wkl.widget.bundle.cdv;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.wkl.widget.bundle.R;


/**
 * Created by <a href="mailto:kunlin.wang@mtime.com">Wang kunlin</a>
 * <p>
 * On 2018-03-06
 */

public class CountDownView extends View {

    public CountDownView(Context context) {
        super(context);
        init(context, null);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private SweepGradient mSweepGradient;
    private int mMax;
    private int mProgress;
    private int[] mColors;
    /**
     * 画笔
     */
    private TextPaint mTextPaint;
    private Paint mShaderPaint;
    private Paint mBgPaint;
    /**
     * 数值文字颜色
     */
    private int mTextColor;

    /**
     * 数值文字大小
     */
    private float mTextSize;

    private RectF mBounds;
    private RectF mRectF;
    private int mStrokeWidth = 10;
    private float mAnimatedProgress = 1;
    private ValueAnimator mAnimator;
    private TimeInterpolator mInterpolator = new LinearInterpolator();
    private int mProgressBgColor;
    private int mProgressPadding;

    @SuppressLint("ResourceType")
    private void init(Context context, AttributeSet attrs) {
        initProgressBar();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CountDownView);
        mTextColor = ta.getColor(R.styleable.CountDownView_cdTextColor, Color.BLACK);
        mTextSize = ta.getDimensionPixelSize(R.styleable.CountDownView_cdTextSize, 10);
        mStrokeWidth = ta.getDimensionPixelSize(R.styleable.CountDownView_cdProgressWidth, mStrokeWidth);
        mProgressBgColor = ta.getColor(R.styleable.CountDownView_cdProgressBackgroundColor, Color.WHITE);
        mProgressPadding = ta.getDimensionPixelSize(R.styleable.CountDownView_cdProgressPadding, 0);
        int centerColor = ta.getColor(R.styleable.CountDownView_cdProgressCenterColor, 0);
        int sideColor = ta.getColor(R.styleable.CountDownView_cdProgressSideColor, 0);
        setMax(ta.getInt(R.styleable.CountDownView_android_max, 0));
        setProgress(ta.getInt(R.styleable.CountDownView_android_progress, 0));

        ta.recycle();
        mColors = new int[]{
                sideColor,
                centerColor,
                sideColor
        };

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        mShaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShaderPaint.setStyle(Paint.Style.STROKE);
        mShaderPaint.setStrokeWidth(mStrokeWidth);
        // 设置线帽为圆形
        mShaderPaint.setStrokeCap(Paint.Cap.ROUND);

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(mProgressBgColor);
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setStrokeWidth(mStrokeWidth + mProgressPadding * 2);

        mBounds = new RectF();
        mRectF = new RectF();
        if (!isInEditMode()) {
            mProgress = mMax;
        }
        mAnimatedProgress = mProgress * 1.0f / mMax;
    }

    private CountDownListener mDownListener = new CountDownListener();

    /**
     * 从指定的 progress 开始倒计时
     *
     * @param progress progress
     */
    public void startCountDown(int progress) {
        progress = constrain(progress, 0, mMax);
        if (progress <= 0) return;
        innerStop();
        mAnimatedProgress = progress * 1f / mMax;
        mProgress = progress;

        mAnimator = ValueAnimator.ofFloat(mAnimatedProgress, 0);
        mAnimator.setDuration(progress * 1000);
        mAnimator.setInterpolator(mInterpolator);
        mAnimator.addUpdateListener(mDownListener);
        mAnimator.addListener(mDownListener);
        mAnimator.start();
        postDelayed(mTcd, 1000);
    }

    /**
     * 从指定的 max 开始倒计时
     */
    public void startCountDown() {
        startCountDown(mMax);
    }

    /**
     * 停止倒计时
     */
    public void stop() {
        innerStop();
        invalidate();
    }

    /**
     * 停止倒计时,并置 0
     */
    public void stopToZero() {
        innerStop();
        mAnimatedProgress = 0;
        mProgress = 0;
        invalidate();
    }

    private void innerStop() {
        if (isAnimatorRunning()) {
            removeCallbacks(mTcd);
            mAnimator.cancel();
        }
    }

    public int getProgress() {
        return mProgress;
    }

    private TextCountDown mTcd = new TextCountDown();

    private class TextCountDown implements Runnable {

        @Override
        public void run() {
            mProgress--;
            if (mProgress > 0) {
                postDelayed(this, 1000);
            }
            invalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        innerStop();
    }

    private class CountDownListener extends AnimatorListenerAdapter
            implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mAnimatedProgress = (float) animation.getAnimatedValue();
            invalidate();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            removeAllListeners(animation);
            if (mCdc != null) {
                mCdc.onCountDownComplete();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            removeAllListeners(animation);
        }

        private void removeAllListeners(Animator animation) {
            animation.removeAllListeners();
            ((ValueAnimator) animation).removeAllUpdateListeners();
        }
    }

    private boolean isAnimatorRunning() {
        return mAnimator != null && (mAnimator.isRunning() || mAnimator.isStarted());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBounds.set(0, 0, w, h);
        // 环形渐变
        mSweepGradient = new SweepGradient(w / 2, h / 2, mColors, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawProgressBg(canvas);
        drawProgress(canvas);
        drawText(canvas);
    }

    private void drawProgressBg(Canvas canvas) {
        float centerX = mBounds.centerX();
        float centerY = mBounds.centerY();
        float radius = Math.min(centerX, centerY) - mProgressPadding;
        float border = mStrokeWidth / 2;
        mRectF.set(
                centerX - radius + border,
                centerY - radius + border,
                centerX + radius - border,
                centerY + radius - border
        );
        canvas.drawArc(mRectF, 0, 360, false, mBgPaint);
    }

    private void drawProgress(Canvas canvas) {
        float centerX = mBounds.centerX();
        float centerY = mBounds.centerY();
        float radius = Math.min(centerX, centerY) - mProgressPadding;
        float border = mStrokeWidth / 2;
        mRectF.set(
                centerX - radius + border,
                centerY - radius + border,
                centerX + radius - border,
                centerY + radius - border
        );
        mShaderPaint.setShader(mSweepGradient);
        float sweepAngle = mAnimatedProgress * 360;
        canvas.drawArc(mRectF, -90, -sweepAngle, false, mShaderPaint);
    }

    private void drawText(Canvas canvas) {
        float centerX = mBounds.centerX();
        float centerY = mBounds.centerY();
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float baseLine = centerY - (fm.bottom - fm.top) / 2 - fm.top;
        String text = String.valueOf(mProgress);
        float textWidth = mTextPaint.measureText(text);
        float x = centerX - textWidth / 2;
        canvas.drawText(text, x, baseLine, mTextPaint);
    }

    public void setMax(int max) {
        if (max < 0) {
            mMax = 0;
        }
        if (mMax != max) {
            mMax = max;
            if (mProgress > max) {
                mProgress = max;
            }
        }
    }

    /**
     * 准备倒计时
     */
    public void prepareCountDown() {
        innerStop();
        mProgress = mMax;
        mAnimatedProgress = 1;
        invalidate();
    }

    private void setProgress(int progress) {
        progress = constrain(progress, 0, mMax);
        if (progress == mProgress) {
            return;
        }
        mProgress = progress;
    }

    private static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    private void initProgressBar() {
        mMax = 100;
        mProgress = 0;
    }

    private CountDownComplete mCdc;

    public void setCountDownCompleteListener(CountDownComplete cdc) {
        mCdc = cdc;
    }

    public interface CountDownComplete {
        void onCountDownComplete();
    }
}
