package com.wkl.widget.bundle.vtv;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import com.wkl.widget.bundle.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:wangkunlin1992@gmail.com">Wang kunlin</a>
 * <p>
 * On 2018-06-15
 */
public class VerticalTextView extends View {

    private int mCurTextColor;

    public VerticalTextView(Context context) {
        super(context);
        init(context, null);
    }

    public VerticalTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VerticalTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private TextPaint mPaint;
    private ColorStateList mTextColor;
    private int mGravity = Gravity.TOP | Gravity.LEFT;
    private String mText;
    private float mVerticalSpacing;
    private float mHorizontalSpacing;
    private List<TextColumn> mColumns;
    private String mRotationLetters;
    private int mRotationDegrees = 0;

    private void init(Context c, AttributeSet attrs) {
        mColumns = new ArrayList<>(5);
        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.density = getResources().getDisplayMetrics().density;

        TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.VerticalTextView);
        mVerticalSpacing = a.getDimensionPixelSize(R.styleable.VerticalTextView_verticalSpacing, 0);
        mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.VerticalTextView_horizontalSpacing, 0);
        float textsize = a.getDimensionPixelSize(R.styleable.VerticalTextView_android_textSize, 15);
        mPaint.setTextSize(textsize);
        ColorStateList color = a.getColorStateList(R.styleable.VerticalTextView_android_textColor);
        setTextColor(color == null ? ColorStateList.valueOf(0xff000000) : color);
        mText = a.getString(R.styleable.VerticalTextView_android_text);
        mGravity = a.getInt(R.styleable.VerticalTextView_android_gravity, mGravity);
        mRotationLetters = a.getString(R.styleable.VerticalTextView_rotationLetters);
        mRotationDegrees = a.getInt(R.styleable.VerticalTextView_rotationDegrees, 0);
        constraintRotation();
        a.recycle();
    }

    private void constraintRotation() {
        if (mRotationDegrees > 360 || mRotationDegrees < -360) {
            mRotationDegrees %= 360;
        }
    }

    /**
     * 每个字符 垂直方向上的间距
     *
     * @param spacing spacing
     */
    public void setVerticalSpacing(float spacing) {
        if (spacing != mVerticalSpacing) {
            mVerticalSpacing = spacing;
            requestLayout();
            invalidate();
        }
    }

    public void setRotationDegrees(int rotationDegrees) {
        if (mRotationDegrees != rotationDegrees) {
            mRotationDegrees = rotationDegrees;
            invalidate();
        }
    }

    public void setRotationLetters(String rotationLetters) {
        mRotationLetters = rotationLetters;
        invalidate();
    }

    /**
     * 每一列的间距
     *
     * @param spacing spacing
     */
    public void setHorizontalSpacing(float spacing) {
        if (spacing != mHorizontalSpacing) {
            mHorizontalSpacing = spacing;
            invalidate();
        }
    }

    /**
     * 设置内容对其方式
     *
     * @param gravity gravity
     */
    public void setGravity(int gravity) {
        if (gravity != mGravity) {
            mGravity = gravity;
            invalidate();
        }
    }

    public final void setText(int resId) {
        setText(getResources().getString(resId));
    }

    public final void setText(String text) {
        if (text == null) text = "";
        mText = text;
        requestLayout();
        invalidate();
    }

    public final String getText() {
        return mText;
    }

    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public void setTextSize(int unit, float size) {
        Context c = getContext();
        Resources r;

        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }
        float textSize = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());

        if (textSize != mPaint.getTextSize()) {
            mPaint.setTextSize(textSize);
            requestLayout();
            invalidate();
        }
    }

    public void setTextColor(@ColorInt int color) {
        mTextColor = ColorStateList.valueOf(color);
        updateTextColors();
    }

    public void setTextColor(ColorStateList colors) {
        if (colors == null) {
            throw new NullPointerException();
        }
        mTextColor = colors;
        updateTextColors();
    }

    private void updateTextColors() {
        int color = mTextColor.getColorForState(getDrawableState(), 0);
        if (color != mCurTextColor) {
            mCurTextColor = color;
            invalidate();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mTextColor != null && mTextColor.isStateful()) {
            updateTextColors();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        int measuredWidth;
        int measuredHeight = makeColumn(heightSize, heightMode);
        if (measuredHeight > heightSize) {
            measuredHeight = heightSize;
        }

        if (widthMode == MeasureSpec.EXACTLY) {
            measuredWidth = widthSize;
        } else {
            measuredWidth = (int) (mColumnWidth + paddingLeft + paddingRight);
            if (measuredWidth > widthSize) {
                measuredWidth = widthSize;
            }
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private int makeColumn(int heightSize, int heightMode) {
        mColumns.clear();
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        // 一个字符个高度
        float letterHeight = metrics.bottom - metrics.top;
        // 一个汉字的宽度
        float letterWidth = mPaint.measureText("正");

        int index = 0;
        int lastIndex = 0;
        int maxHeight = 0;
        mColumnWidth = 0;
        if (TextUtils.isEmpty(mText)) {
            mColumnWidth = 15;
            return heightMode != MeasureSpec.EXACTLY ? 15 : heightSize;
        }

        // 分隔换行符
        while ((index = mText.indexOf('\n', index)) != -1) {
            String item = mText.substring(lastIndex, index);
            handleColumnText(item, letterHeight, letterWidth, heightSize, heightMode);
            index += 1;
            lastIndex = index;
            if (heightMode != MeasureSpec.EXACTLY) { // 如果不是确切的高度，取所有列的最大高度
                maxHeight = (int) Math.max(maxHeight, item.length() * letterHeight + (item.length() - 1) * mVerticalSpacing);
            }
        }
        String item = mText.substring(lastIndex);
        handleColumnText(item, letterHeight, letterWidth, heightSize, heightMode);
        if (heightMode != MeasureSpec.EXACTLY) {
            maxHeight = (int) Math.max(maxHeight, item.length() * letterHeight + (item.length() - 1) * mVerticalSpacing);
            return maxHeight + getPaddingTop() + getPaddingBottom();
        }
        return heightSize;
    }

    private float mColumnWidth = 0;

    /**
     * 处理 换行后的每一列，如果高度固定，可能还需要换列
     */
    private void handleColumnText(String item, float letterHeight, float letterWidth,
                                  int heightSize, int heightMode) {
        if (heightMode == MeasureSpec.EXACTLY) { // 高度固定
            float realHeight = heightSize - getPaddingTop() - getPaddingBottom() - letterHeight;
            // 根据高度计算每一列最多可以容纳的字符数
            int maxCharCount = (int) Math.floor(realHeight / (letterHeight + mVerticalSpacing)) + 1;
            int start = 0;
            int end = maxCharCount;
            int index = 1;
            while ((item.length() - maxCharCount * index) > 0) {
                // 生成一列
                TextColumn column = new TextColumn();
                // 添加字符
                column.mCharset.append(item, start, end);
                start = end;
                end += maxCharCount;
                addColumn(column, letterWidth, letterHeight);
                index++;
            }
            TextColumn column = new TextColumn();
            column.mCharset.append(item, start, item.length());
            addColumn(column, letterWidth, letterHeight);
        } else { // 高度不固定，直接生成一个列
            TextColumn column = new TextColumn();
            column.mCharset.append(item);
            addColumn(column, letterWidth, letterHeight);
        }
    }

    private void addColumn(TextColumn column, float letterWidth, float letterHeight) {
        column.mRotationDegrees = mRotationDegrees;
        column.mRotationLetters = mRotationLetters;
        column.mVerticalSpacing = mVerticalSpacing;
        List<TextColumn> columns = mColumns;
        // 是否是 靠右布局
        boolean right = (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT;
        column.mLetterWidth = letterWidth;
        column.mLetterHeight = letterHeight;
        // 宽度增加
        mColumnWidth += letterWidth;
        if (columns.isEmpty()) { // 靠边的 列 空间为0
            if (right) {
                column.mRightSpace = 0;
            } else {
                column.mLeftSpace = 0;
            }
        } else {
            mColumnWidth += mHorizontalSpacing;
            // 当前列的空间 赋值
            if (right) {
                column.mRightSpace = mHorizontalSpacing / 2;
            } else {
                column.mLeftSpace = mHorizontalSpacing / 2;
            }

            // 把上一个 的列的空间 赋值
            TextColumn last = columns.get(columns.size() - 1);
            if (right) {
                last.mLeftSpace = mHorizontalSpacing / 2;
            } else {
                last.mRightSpace = mHorizontalSpacing / 2;
            }
        }
        // 默认当前添加的为最后一列，靠边的 列 空间 为 0
        if (right) {
            column.mLeftSpace = 0;
        } else {
            column.mRightSpace = 0;
        }
        columns.add(column);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = mPaint;
        paint.setColor(mCurTextColor);
        paint.drawableState = getDrawableState();
        List<TextColumn> columns = mColumns;
        int N = columns.size();
        int gravity = mGravity;
        // 是否是 靠右布局
        boolean right = (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT;
        // 是否是 从下向上布局
        int majorGravity = (gravity & Gravity.VERTICAL_GRAVITY_MASK);
        boolean bottom = majorGravity == Gravity.BOTTOM;
        boolean centerVertical = majorGravity == Gravity.CENTER_VERTICAL;
        // 起始绘制的坐标
        float xAxis = right ? getMeasuredWidth() - getPaddingRight() : getPaddingLeft();
        float yAxis = bottom ? getMeasuredHeight() - getPaddingBottom() : getPaddingTop();
        for (int i = 0; i < N; i++) {
            // 绘制每一列
            TextColumn column = columns.get(i);
            yAxis = centerVertical ? (getHeight() - column.getHeight()) / 2 : yAxis;
            xAxis = column.draw(canvas, paint, gravity, xAxis, yAxis);
        }
    }

    private static class TextColumn {
        private float mLeftSpace;
        private float mRightSpace;
        private StringBuilder mCharset;
        private float mLetterWidth;
        private float mLetterHeight;
        private float mVerticalSpacing;
        private int mRotationDegrees;
        private String mRotationLetters;
        private RectF mRotation;

        private float getHeight() {
            int length = mCharset.length();
            return length * mLetterHeight + (length - 1) * mVerticalSpacing;
        }

        TextColumn() {
            mCharset = new StringBuilder();
            mRotation = new RectF();
        }

        private char[] todraw = new char[1]; // 为了不频繁的 创建对象

        float draw(Canvas canvas, TextPaint paint, int gravity, float xAxis, float yAxis) {
            float vSpace = mVerticalSpacing;
            boolean right = (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT;
            boolean bottom = (gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM;
            float textX;
            // 计算文字绘制的 起始 x 坐标 和 下一列的 x 坐标
            if (right) {
                textX = xAxis - mRightSpace - mLetterWidth;
                xAxis = xAxis - mLetterWidth - mLeftSpace - mRightSpace;
            } else {
                textX = xAxis + mLeftSpace;
                xAxis = xAxis + mLetterWidth + mLeftSpace + mRightSpace;
            }
            Paint.FontMetrics metrics = paint.getFontMetrics();
            float textY;
            if (bottom) { // 计算 文字绘制的 y 坐标 并绘制每一个字符，如果是 从下向上布局，需要反向绘制
                textY = yAxis - metrics.bottom;
                for (int i = mCharset.length() - 1; i >= 0; i--) {
                    doDraw(canvas, paint, i, textX, textY);
                    textY = textY - vSpace - mLetterHeight;
                }
            } else {
                textY = yAxis - metrics.top;
                for (int i = 0; i < mCharset.length(); i++) {
                    doDraw(canvas, paint, i, textX, textY);
                    textY = textY + vSpace + mLetterHeight;
                }
            }

            return xAxis;
        }

        private void doDraw(Canvas canvas, TextPaint paint, int index, float x, float y) {
            char letter = mCharset.charAt(index);
            todraw[0] = letter;
            boolean needRotation = false;
            String rl = mRotationLetters;
            if (rl != null && !rl.isEmpty()) {
                if (rl.indexOf(letter) != -1) {
                    needRotation = true;
                }
            }
            int sc = -1;
            if (needRotation) { // 旋转字符
                Paint.FontMetrics ms = paint.getFontMetrics();
                RectF rectF = mRotation;
                rectF.left = x;
                rectF.top = y + ms.top;
                rectF.right = x + mLetterWidth;
                rectF.bottom = rectF.top + mLetterHeight;
                sc = canvas.save();

                canvas.clipRect(rectF);
                canvas.rotate(mRotationDegrees, rectF.centerX(), rectF.centerY());
            }
            canvas.drawText(todraw, 0, 1, x, y, paint);
            if (needRotation) {
                canvas.restoreToCount(sc);
            }
        }
    }
}
