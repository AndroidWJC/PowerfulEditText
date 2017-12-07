package com.wang.powerfuledittext;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;


public class PowerfulEditText extends EditText {

    private static final String TAG = "PowerfulEditText.java";

    private static final int DEFAULT_CLEAR_RES = R.drawable.clear_all;
    private static final int DEFAULT_VISIBLE_RES = R.drawable.visible;
    private static final int DEFAULT_INVISIBLE_RES = R.drawable.invisible;

    private final int DEFAULT_BUTTON_PADDING =
            getResources().getDimensionPixelSize(R.dimen.btn_edittext_padding);
    private final int DEFAULT_BUTTON_WIDTH =
            getResources().getDimensionPixelSize(R.dimen.btn_edittext_width);

    private static final int ANIMATOR_TIME = 200;

    //按钮间隔
    private int mBtnPadding = 0;
    //按钮宽度
    private int mBtnWidth = 0;
    //右内边距
    private int mTextPaddingRight;

    private int mClearResId = 0;
    private int mVisibleResId = 0;
    private int mInvisibleResId = 0;
    private Bitmap mBitmapClear;
    private Bitmap mBitmapVisible;
    private Bitmap mBitmapInvisible;

    //出现和消失动画
    private ValueAnimator mGoneAnimator;
    private ValueAnimator mVisibleAnimator;
    //状态值
    private boolean isBtnVisible = false;
    private boolean isPassword = false;
    private boolean isPasswordVisible = false;
    private boolean isFocused = false;

    private Paint mPaint;

    public PowerfulEditText(Context context) {
        this(context, null);
    }

    public PowerfulEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PowerfulEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        //抗锯齿和位图滤波
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        //读取xml文件中的配置
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PowerfulEditText);
            for (int i = 0; i < array.getIndexCount(); i++) {
                int attr = array.getIndex(i);

                switch (attr) {
                    case R.styleable.PowerfulEditText_clearDrawable:
                        mClearResId = array.getResourceId(attr, DEFAULT_CLEAR_RES);
                        break;

                    case R.styleable.PowerfulEditText_visibleDrawable:
                        mVisibleResId = array.getResourceId(attr, DEFAULT_VISIBLE_RES);
                        break;

                    case R.styleable.PowerfulEditText_invisibleDrawable:
                        mInvisibleResId = array.getResourceId(attr, DEFAULT_INVISIBLE_RES);
                        break;

                    case R.styleable.PowerfulEditText_BtnWidth:
                        mBtnWidth = array.getDimensionPixelSize(attr, DEFAULT_BUTTON_WIDTH);
                        break;

                    case R.styleable.PowerfulEditText_BtnSpacing:
                        mBtnPadding = array.getDimensionPixelSize(attr, DEFAULT_BUTTON_PADDING);
                        break;
                }
            }
            array.recycle();
        }

        mBitmapClear = createBitmap(context, mClearResId, DEFAULT_CLEAR_RES);
        mBitmapVisible = createBitmap(context, mVisibleResId, DEFAULT_VISIBLE_RES);
        mBitmapInvisible = createBitmap(context, mInvisibleResId, DEFAULT_INVISIBLE_RES);
        if (mBtnPadding == 0) {
            mBtnPadding = DEFAULT_BUTTON_PADDING;
        }
        if (mBtnWidth == 0) {
            mBtnWidth = DEFAULT_BUTTON_WIDTH;
        }
        mTextPaddingRight = mBtnPadding * 4 + mBtnWidth * 2;

        //按钮出现和消失的动画
        mGoneAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(ANIMATOR_TIME);
        mVisibleAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(ANIMATOR_TIME);

        //是否是密码样式
        isPassword =
                getInputType() == (InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //设置右内边距, 防止清除按钮和文字重叠
        setPadding(getPaddingLeft(), getPaddingTop(), mTextPaddingRight, getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isBtnVisible) {
            if (mVisibleAnimator.isRunning()) {
                float scale = (float) mVisibleAnimator.getAnimatedValue();
                drawClearButton(scale, canvas);
                if (isPassword) {
                    drawVisibleButton(scale, canvas, isPasswordVisible);
                }
                invalidate();
            } else {
                drawClearButton(1, canvas);
                if (isPassword) {
                    drawVisibleButton(1, canvas, isPasswordVisible);
                }
            }
        } else {
            if (mGoneAnimator.isRunning()) {
                float scale = (float) mGoneAnimator.getAnimatedValue();
                drawClearButton(scale, canvas);
                if (isPassword) {
                    drawVisibleButton(scale, canvas, isPasswordVisible);
                }
                invalidate();
            }
        }
    }

    /**
     * 绘制清除按钮出现的图案
     * @param scale 缩放比例
     * @param canvas
     */
    private void drawClearButton(float scale, Canvas canvas) {

        int right = (int) (getWidth() + getScrollX() - mBtnPadding - mBtnWidth * (1f - scale) / 2f);
        int left = (int) (getWidth() + getScrollX() - mBtnPadding - mBtnWidth * (scale + (1f - scale) / 2f));
        int top = (int) ((getHeight() - mBtnWidth * scale) / 2);
        int bottom = (int) (top + mBtnWidth * scale);
        Rect rect = new Rect(left, top, right, bottom);
        canvas.drawBitmap(mBitmapClear, null, rect, mPaint);
    }

    private void drawVisibleButton(float scale, Canvas canvas, boolean isVisible) {

        int right = (int) (getWidth() + getScrollX() - mBtnPadding * 3 - mBtnWidth - mBtnWidth * (1f - scale) / 2f);
        int left = (int) (getWidth() + getScrollX() - mBtnPadding * 3 - mBtnWidth - mBtnWidth * (scale + (1f - scale) / 2f));
        int top = (int) ((getHeight() - mBtnWidth * scale) / 2);
        int bottom = (int) (top + mBtnWidth * scale);
        Rect rect = new Rect(left, top, right, bottom);
        if (isVisible) {
            canvas.drawBitmap(mBitmapVisible, null, rect, mPaint);
        } else {
            canvas.drawBitmap(mBitmapInvisible, null, rect, mPaint);
        }

    }

    // 清除按钮出现时的动画效果
    private void startVisibleAnimator() {
        endAllAnimator();
        mVisibleAnimator.start();
        invalidate();
    }

    // 清除按钮消失时的动画效果
    private void startGoneAnimator() {
        endAllAnimator();
        mGoneAnimator.start();
        invalidate();
    }

    // 结束所有动画
    private void endAllAnimator(){
        mGoneAnimator.end();
        mVisibleAnimator.end();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        isFocused = focused;
        if (focused && getText().length() > 0) {
            if (!isBtnVisible) {
                isBtnVisible = true;
                startVisibleAnimator();
            }
        } else {
            if (isBtnVisible) {
                isBtnVisible = false;
                startGoneAnimator();
            }
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        if (text.length() > 0 && isFocused()) {
            if (!isBtnVisible) {
                isBtnVisible = true;
                startVisibleAnimator();
            }
        } else {
            if (isBtnVisible) {
                isBtnVisible = false;
                startGoneAnimator();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            boolean clearTouched =
                    ( getWidth() - mBtnPadding - mBtnWidth < event.getX() )
                            && (event.getX() < getWidth() - mBtnPadding)
                            && isFocused;
            boolean visibleTouched =
                    (getWidth() - mBtnPadding * 3 - mBtnWidth * 2 < event.getX())
                            && (event.getX() < getWidth() - mBtnPadding * 3 - mBtnWidth)
                            && isPassword && isFocused;

            if (clearTouched) {
                setError(null);
                setText("");
            } else if (visibleTouched) {
                if (isPasswordVisible) {
                    isPasswordVisible = false;
                    setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    setSelection(getText().length());
                    invalidate();
                } else {
                    isPasswordVisible = true;
                    setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    setSelection(getText().length());
                    invalidate();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    // 开始晃动的入口
    public void startShakeAnimation(){
        if(getAnimation() == null){
            setAnimation(shakeAnimation(4));
        }
        startAnimation(getAnimation());
    }

    /**
     * 晃动动画
     * @param counts 0.5秒钟晃动多少下
     * @return
     */
    private Animation shakeAnimation(int counts){
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(counts));
        translateAnimation.setDuration(500);
        return translateAnimation;
    }

    private Bitmap createBitmap(Context context, int resId, int defResId) {
        if (resId != 0) {
            return BitmapFactory.decodeResource(context.getResources(), resId);
        } else {
            return BitmapFactory.decodeResource(context.getResources(), defResId);
        }
    }
}

