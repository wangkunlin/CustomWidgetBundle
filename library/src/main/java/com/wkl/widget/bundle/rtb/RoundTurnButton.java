package com.wkl.widget.bundle.rtb;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wkl.widget.bundle.R;
import com.wkl.widget.bundle.util.SizeUtil;


/**
 * Created by <a href="mailto:wangkunlin1992@gmail.com">Wang kunlin</a>
 * <p>
 * On 2018-08-14
 */
public class RoundTurnButton extends View {

    public interface OnTurnButtonChangeListener {

        void onProgressChanged(RoundTurnButton turnButton, int progress, boolean fromUser);

        void onStartTrackingTouch(RoundTurnButton turnButton);

        void onStopTrackingTouch(RoundTurnButton turnButton);
    }

    private OnTurnButtonChangeListener mOnTurnButtonChangeListener;

    private Drawable mThumb;
    private int mMax = 100;
    private int mProgress = 0;
    private float mProgressWidth;
    private float mProgressBackgroundWidth;
    private int mProgressColor;
    private int mProgressBackgroundColor;
    private int mStartAngle;
    private int mSweepAngle;

    private float mProgressSweep;

    private RectF mArcRect = new RectF();
    private Paint mPaint;

    private float mSideRoundWidth;

    private static final int SIDE_ROUND_COLOR = 0xff4C5584;
    private static final int MAIN_COLOR = 0xff2B304E;

    private static final int SIDE_POINT_COLOR = 0xff3C446C;
    private static final int SIDE_POINT_SELECTED_COLOR = 0xffE6AD0F;
    private static final int POINT_COUNT = 10;
    // 偏移 90度，使 12 点方向为 0 度
    private static final int ANGLE_OFFSET = -90;

    private float mProgressInside;
    private Rect mBounds = new Rect();
    private float mProgressRadius;
    private float mRadius;
    private float mPointRadius;

    private Path mPath = new Path();
    private PathMeasure mPathMeasure = new PathMeasure();
    private float[] mPos = new float[2];
    private float[] mThumbTrans = new float[2];


    public RoundTurnButton(Context context) {
        super(context);
        init(context, null);
    }

    public RoundTurnButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundTurnButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        SizeUtil.init(context);

        mSideRoundWidth = SizeUtil.dp2px(10);
        mProgressInside = SizeUtil.dp2px(8);
        mPointRadius = SizeUtil.dp2px(2);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundTurnButton,
                0, R.style.RoundTurnButtonDefStyle);
        int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            if (i == R.styleable.RoundTurnButton_android_thumb) {
                mThumb = a.getDrawable(i);
            } else if (i == R.styleable.RoundTurnButton_android_max) {
                mMax = a.getInt(i, 0);
            } else if (i == R.styleable.RoundTurnButton_android_progress) {
                mProgress = a.getInt(i, 0);
            } else if (i == R.styleable.RoundTurnButton_progressWidth) {
                mProgressWidth = a.getDimension(i, 1);
            } else if (i == R.styleable.RoundTurnButton_progressBackgroundWidth) {
                mProgressBackgroundWidth = a.getDimension(i, 1);
            } else if (i == R.styleable.RoundTurnButton_progressColor) {
                mProgressColor = a.getColor(i, 0);
            } else if (i == R.styleable.RoundTurnButton_progressBackgroundColor) {
                mProgressBackgroundColor = a.getColor(i, 0);
            } else if (i == R.styleable.RoundTurnButton_startAngle) {
                mStartAngle = a.getInt(i, 0);
            } else if (i == R.styleable.RoundTurnButton_sweepAngle) {
                mSweepAngle = a.getInt(i, 360);
            }
        }
        a.recycle();

        mSweepAngle = mSweepAngle % 360;
        mStartAngle = mStartAngle % 360;

        mMax = mMax <= 0 ? 100 : mMax;
        mProgress = constrain(mProgress, mMax);

        mProgressSweep = mProgress * 1f / mMax * mSweepAngle;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStartAngle += ANGLE_OFFSET;
        if (mThumb != null) {
            int thumbHalfheight = mThumb.getIntrinsicHeight() / 2;
            int thumbHalfWidth = mThumb.getIntrinsicWidth() / 2;
            mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
                    thumbHalfheight);
        }
    }

    public void setOnTurnButtonChangeListener(OnTurnButtonChangeListener l) {
        mOnTurnButtonChangeListener = l;
    }

    private static int constrain(int amount, int high) {
        return amount < 0 ? 0 : (amount > high ? high : amount);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBounds.set(0, 0, w, h);
        mRadius = w < h ? w / 2 : h / 2;

        mProgressRadius = mRadius - mSideRoundWidth - mProgressInside;
        calculatProgressSweep();
    }

    private void calculatProgressSweep() {
        int centerX = mBounds.centerX();
        int centerY = mBounds.centerY();
        float left = centerX - mProgressRadius;
        float top = centerY - mProgressRadius;
        mArcRect.set(left, top, mBounds.width() - left, mBounds.height() - top);

        mPath.reset();
        mPath.addArc(mArcRect, mStartAngle, mSweepAngle);
        mPathMeasure.setPath(mPath, false);
        float len = mPathMeasure.getLength() * (mProgress * 1f / mMax);
        mPathMeasure.getPosTan(len, mThumbTrans, null);

        mProgressSweep = mProgress * 1f / mMax * mSweepAngle;
    }

    public int getProgress() {
        return mProgress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawSideRound(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(MAIN_COLOR);
        int centerX = mBounds.centerX();
        int centerY = mBounds.centerY();
        canvas.drawCircle(centerX, centerY, mRadius - mSideRoundWidth, mPaint);

        float left = centerX - mProgressRadius;
        float top = centerY - mProgressRadius;
        mArcRect.set(left, top, mBounds.width() - left, mBounds.height() - top);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mProgressBackgroundColor);
        mPaint.setStrokeWidth(mProgressBackgroundWidth);
        canvas.drawArc(mArcRect, mStartAngle, mSweepAngle, false, mPaint);

        if (mProgressSweep > 0) {
            mPaint.setColor(mProgressColor);
            mPaint.setStrokeWidth(mProgressWidth);
            canvas.drawArc(mArcRect, mStartAngle, mProgressSweep, false, mPaint);
        }

        if (mThumb != null) {
            canvas.save();
            canvas.translate(mThumbTrans[0], mThumbTrans[1]);
            mThumb.draw(canvas);
            canvas.restore();
        }
    }

    private void drawSideRound(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mSideRoundWidth);
        mPaint.setColor(SIDE_ROUND_COLOR);
        int centerX = mBounds.centerX();
        int centerY = mBounds.centerY();
        float strokeWidth = mSideRoundWidth / 2;
        canvas.drawCircle(centerX, centerY, mRadius - strokeWidth, mPaint);
        float left = centerX - mRadius + strokeWidth;
        float top = centerY - mRadius + strokeWidth;
        mArcRect.set(left, top, mBounds.width() - left, mBounds.height() - top);
        mPath.reset();
        mPath.addArc(mArcRect, mStartAngle, mSweepAngle);
        mPathMeasure.setPath(mPath, false);
        float length = mPathMeasure.getLength();
        float perLen = length / (POINT_COUNT - 1);
        float percent = mProgress * 1f / mMax;
        float pice = 1f / (POINT_COUNT - 1);
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < POINT_COUNT; ++i) {
            mPathMeasure.getPosTan(i * perLen, mPos, null);
            if (percent >= i * pice) {
                mPaint.setColor(SIDE_POINT_SELECTED_COLOR);
            } else {
                mPaint.setColor(SIDE_POINT_COLOR);
            }
            canvas.drawCircle(mPos[0], mPos[1], mPointRadius, mPaint);
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return mThumb == who || super.verifyDrawable(who);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mThumb != null && mThumb.isStateful()) {
            int[] state = getDrawableState();
            mThumb.setState(state);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                setPressed(true);
                onStartTrackingTouch();
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_MOVE:
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                onStopTrackingTouch();
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    public void setProgress(int progress) {
        updateProgress(progress, false);
    }

    private void updateProgress(int progress, boolean fromUser) {
        progress = constrain(progress, mMax);

        mProgress = progress;

        if (mOnTurnButtonChangeListener != null) {
            mOnTurnButtonChangeListener
                    .onProgressChanged(this, progress, fromUser);
        }

        updateThumbPosition();

        invalidate();
    }

    private void updateThumbPosition() {
        calculatProgressSweep();
    }

    private void updateOnTouch(MotionEvent event) {
        double touchAngle = getTouchDegrees(event.getX(), event.getY());
        int progress = getProgressForAngle(touchAngle);
        updateProgress(progress, true);
    }

    private int getProgressForAngle(double angle) {
        int touchProgress = (int) Math.round(mMax * 1f / mSweepAngle * angle);
        return constrain(touchProgress, mMax);
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mBounds.centerX();
        float y = yPos - mBounds.centerY();
        double angle = Math.toDegrees(Math.atan2(y, x));
//        if (x >= 0 && y >= 0) {
//            // 第一象限
//            angle += 360;
//        } /*else if (x < 0 && y > 0) {
//            // 第二象限
//        } */ else if (x < 0 && y < 0) {
//            // 第三象限
//            angle += 360;
//        } else if (x > 0 && y < 0) {
//            // 第四象限
//            angle += 360;
//        }
        if (!(x < 0 && y > 0)) { // 根据上面
            angle += 360;
        }
        angle = constraintAngle(angle);

        return angle - mStartAngle;
    }

    private double constraintAngle(double amount) {
        return amount < mStartAngle ? mStartAngle :
                (amount > (mStartAngle + mSweepAngle) ? (mStartAngle + mSweepAngle) : amount);
    }

    private void onStartTrackingTouch() {
        if (mOnTurnButtonChangeListener != null) {
            mOnTurnButtonChangeListener.onStartTrackingTouch(this);
        }
    }

    private void onStopTrackingTouch() {
        if (mOnTurnButtonChangeListener != null) {
            mOnTurnButtonChangeListener.onStopTrackingTouch(this);
        }
    }
}
