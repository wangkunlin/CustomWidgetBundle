package com.wkl.widget.bundle.vsb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.wkl.widget.bundle.util.SizeUtil;


/**
 * Created by <a href="mailto:wangkunlin1992@gmail.com">Wang kunlin</a>
 * <p>
 * On 2018-08-14
 */
public class EqualizerSeekGroup extends LinearLayout implements VerticalSeekBar.OnSeekBarChangeListener {

    public EqualizerSeekGroup(Context context) {
        super(context);
        init();
    }

    public EqualizerSeekGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EqualizerSeekGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private int mLineLength;
    private int mLineWidth;
    private int mSideOffset;
    private int mBridgeWidth;
    private Paint mPaint;
    private static final int LineColor = 0xff5B5E6D;
    private static final int BridgeColor = 0xff323759;
    private static final int LINE_COUNT = 7;
    private int[] mLastXy = new int[2];

    /**
     * 画笔
     */
    private TextPaint mTextPaint;

    private void init() {
        setOrientation(HORIZONTAL);
        SizeUtil.init(getContext());

        mLineLength = (int) SizeUtil.dp2px(12);
        mLineWidth = (int) SizeUtil.dp2px(1);
        mBridgeWidth = (int) SizeUtil.dp2px(6);
        mSideOffset = (int) SizeUtil.dp2px(10);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        /*
      文字大小
     */
        float textSize = SizeUtil.sp2px(16);
        /*
      文字颜色
     */
        int textColor = 0x89FFFFFF;
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);

    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof VerticalSeekBar)) {
            throw new IllegalArgumentException("");
        }
        VerticalSeekBar seekBar = (VerticalSeekBar) child;
        seekBar.addOnSeekBarChangeListener(this);
        super.addView(child, index, params);
    }

    void drawText(Canvas canvas, float cx, String text) {
        if (TextUtils.isEmpty(text)) return;
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        int baseline = getPaddingTop() - fontMetrics.top;
        float textWidth = mTextPaint.measureText(text);
        float x = cx - textWidth / 2;
        canvas.drawText(text, x, baseline, mTextPaint);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int N = getChildCount();
        for (int i = 0; i < N; ++i) {
            View child = getChildAt(i);
            VerticalSeekBar seekBar = (VerticalSeekBar) child;

            drawLines(canvas, seekBar);

            int left = child.getLeft();
            int top = child.getTop();
            int width = child.getWidth();
            int childPaddingTop = child.getPaddingTop();

            Drawable drawable = seekBar.getProgressDrawable();
            Rect bounds = drawable.getBounds();
            drawText(canvas, left + width / 2, seekBar.getText());

            int progressHeight = bounds.height();
            float progress = 1 - seekBar.getProgress() * 1f / seekBar.getMax();
            int thumbX = left + child.getWidth() / 2;
            int thumbY = (int) (top + childPaddingTop + progressHeight * progress);

            if (i > 0) {
                Paint paint = mPaint;
                paint.setStrokeWidth(mBridgeWidth);
                paint.setColor(BridgeColor);
                canvas.drawLine(mLastXy[0], mLastXy[1], thumbX, thumbY, paint);
            }

            mLastXy[0] = thumbX;
            mLastXy[1] = thumbY;
        }
        super.dispatchDraw(canvas);
    }

    private void drawLines(Canvas canvas, VerticalSeekBar seekBar) {
        int left = seekBar.getLeft();
        int top = seekBar.getTop();
        int right = seekBar.getRight();
        int width = seekBar.getWidth();
        int childPaddingTop = seekBar.getPaddingTop();

        Drawable drawable = seekBar.getProgressDrawable();
        Rect bounds = drawable.getBounds();
        drawText(canvas, left + width / 2, seekBar.getText());

        int contentHeight = bounds.height() - mSideOffset * 2;
        int perHeight = contentHeight / (LINE_COUNT - 1);
        Paint paint = mPaint;
        paint.setStrokeWidth(mLineWidth);
        paint.setColor(LineColor);
        int lineLength = mLineLength;

        int y = top + childPaddingTop + mSideOffset;

        for (int i = 0; i < LINE_COUNT; ++i) {
            if (i > 0) {
                y += perHeight;
            }
            int x = left;
            canvas.drawLine(x, y, x + lineLength, y, paint);
            x = right - lineLength;
            canvas.drawLine(x, y, x + lineLength, y, paint);
        }
    }

    @Override
    public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
        invalidate();
    }

    @Override
    public void onStartTrackingTouch(VerticalSeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(VerticalSeekBar seekBar) {
    }
}
