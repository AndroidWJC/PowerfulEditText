package com.wang.powerfuledittext;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
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

    private static final int DEFAULT_STYLE_COLOR = Color.BLUE;

    private final int DEFAULT_BUTTON_PADDING =
            getResources().getDimensionPixelSize(R.dimen.btn_edittext_padding);
    private final int DEFAULT_BUTTON_WIDTH =
            getResources().getDimensionPixelSize(R.dimen.btn_edittext_width);

    private static final String STYLE_RECT = "rectangle";
    private static final String STYLE_ROUND_RECT = "roundRect";
    private static final String STYLE_HALF_RECT = "halfRect";
    private static final String STYLE_ANIMATOR = "animator";

    private static final int DEFAULT_ROUND_RADIUS = 20;
    private static final int ANIMATOR_TIME = 200;
    private static final int DEFAULT_FOCUSED_STROKE_WIDTH = 8;
    private static final int DEFAULT_UNFOCUSED_STROKE_WIDTH = 4;

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

    private String mBorderStyle = "animator";
    private int mStyleColor = -1;

    //出现和消失动画
    private ValueAnimator mGoneAnimator;
    private ValueAnimator mVisibleAnimator;
    //状态值
    private boolean isBtnVisible = false;
    private boolean isPassword = false;
    private boolean isPasswordVisible = false;

    private boolean isAnimatorRunning = false;
    private int mAnimatorProgress = 0;
    private ObjectAnimator mAnimator;

    //自定义属性动画
    private static final Property<PowerfulEditText, Integer> BORDER_PROGRESS
            = new Property<PowerfulEditText, Integer>(Integer.class, "borderProgress") {
        @Override
        public Integer get(PowerfulEditText powerfulEditText) {
            return powerfulEditText.getBorderProgress();
        }

        @Override
        public void set(PowerfulEditText powerfulEditText, Integer value) {
            powerfulEditText.setBorderProgress(value);
        }
    };

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
            mClearResId = array.getResourceId(R.styleable.PowerfulEditText_clearDrawable, DEFAULT_CLEAR_RES);
            mVisibleResId = array.getResourceId(R.styleable.PowerfulEditText_visibleDrawable, DEFAULT_VISIBLE_RES);
            mInvisibleResId = array.getResourceId(R.styleable.PowerfulEditText_invisibleDrawable, DEFAULT_INVISIBLE_RES);
            mBtnWidth = array.getDimensionPixelSize(R.styleable.PowerfulEditText_BtnWidth, DEFAULT_BUTTON_WIDTH);
            mBtnPadding = array.getDimensionPixelSize(R.styleable.PowerfulEditText_BtnSpacing, DEFAULT_BUTTON_PADDING);
            if (!TextUtils.isEmpty(array.getString(R.styleable.PowerfulEditText_borderStyle))){
                mBorderStyle = array.getString(R.styleable.PowerfulEditText_borderStyle);
            }
            mStyleColor = array.getColor(R.styleable.PowerfulEditText_styleColor, DEFAULT_STYLE_COLOR);
            array.recycle();
        }

        //初始化按钮显示的Bitmap
        mBitmapClear = createBitmap(context, mClearResId, DEFAULT_CLEAR_RES);
        mBitmapVisible = createBitmap(context, mVisibleResId, DEFAULT_VISIBLE_RES);
        mBitmapInvisible = createBitmap(context, mInvisibleResId, DEFAULT_INVISIBLE_RES);
        //如果自定义，则使用自定义的值，否则使用默认值
        if (mBtnPadding == 0) {
            mBtnPadding = DEFAULT_BUTTON_PADDING;
        }
        if (mBtnWidth == 0) {
            mBtnWidth = DEFAULT_BUTTON_WIDTH;
        }
        //给文字设置一个padding，避免文字和按钮重叠了
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

        mPaint.setStyle(Paint.Style.STROKE);

        //使用自定义颜色。如未定义，则使用默认颜色
        if (mStyleColor != -1) {
            mPaint.setColor(mStyleColor);
        } else {
            mPaint.setColor(DEFAULT_STYLE_COLOR);
        }

        //控件获取焦点时，加粗边框
        if (isFocused()) {
            mPaint.setStrokeWidth(DEFAULT_FOCUSED_STROKE_WIDTH);
        } else {
            mPaint.setStrokeWidth(DEFAULT_UNFOCUSED_STROKE_WIDTH);
        }

        //绘制清空和明文显示按钮
        drawBorder(canvas);

        //绘制边框
        drawButtons(canvas);
    }

    private void drawBorder(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        switch (mBorderStyle) {
            //矩形样式
            case STYLE_RECT:
                setBackground(null);
                canvas.drawRect(0, 0, width, height, mPaint);
                break;

            //圆角矩形样式
            case STYLE_ROUND_RECT:
                setBackground(null);
                float roundRectLineWidth = 0;
                if (isFocused()) {
                    roundRectLineWidth = DEFAULT_FOCUSED_STROKE_WIDTH / 2;
                } else {
                    roundRectLineWidth = DEFAULT_UNFOCUSED_STROKE_WIDTH / 2;
                }
                mPaint.setStrokeWidth(roundRectLineWidth);
                if (Build.VERSION.SDK_INT >= 21) {
                    canvas.drawRoundRect(
                            roundRectLineWidth/2, roundRectLineWidth/2, width - roundRectLineWidth/2, height - roundRectLineWidth/2,
                            DEFAULT_ROUND_RADIUS, DEFAULT_ROUND_RADIUS,
                            mPaint);
                } else {
                    canvas.drawRoundRect(
                            new RectF(roundRectLineWidth/2, roundRectLineWidth/2, width - roundRectLineWidth/2, height - roundRectLineWidth/2),
                            DEFAULT_ROUND_RADIUS, DEFAULT_ROUND_RADIUS,
                            mPaint);
                }
                break;

            //半矩形样式
            case STYLE_HALF_RECT:
                setBackground(null);
                canvas.drawLine(0, height, width, height, mPaint);
                canvas.drawLine(0, height / 2, 0, height, mPaint);
                canvas.drawLine(width, height / 2, width, height, mPaint);
                break;

            //动画特效样式
            case STYLE_ANIMATOR:
                setBackground(null);
                if (isAnimatorRunning) {
                    canvas.drawLine(width / 2 - mAnimatorProgress, height, width / 2 + mAnimatorProgress, height, mPaint);
                    if (mAnimatorProgress == width / 2) {
                        isAnimatorRunning = false;
                    }
                } else {
                    canvas.drawLine(0, height, width, height, mPaint);
                }
                break;
        }
    }

    private void drawButtons(Canvas canvas) {
        if (isBtnVisible) {
            //播放按钮出现的动画
            if (mVisibleAnimator.isRunning()) {
                float scale = (float) mVisibleAnimator.getAnimatedValue();
                drawClearButton(scale, canvas);
                if (isPassword) {
                    drawVisibleButton(scale, canvas, isPasswordVisible);
                }
                invalidate();
            //绘制静态的按钮
            } else {
                drawClearButton(1, canvas);
                if (isPassword) {
                    drawVisibleButton(1, canvas, isPasswordVisible);
                }
            }
        } else {
            //播放按钮消失的动画
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

        //播放按钮出现和消失动画
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

        //实现动画特效样式
        if (focused && mBorderStyle.equals(STYLE_ANIMATOR)) {
            isAnimatorRunning = true;
            mAnimator = ObjectAnimator.ofInt(this, BORDER_PROGRESS, 0, getWidth() / 2);
            mAnimator.setDuration(ANIMATOR_TIME);
            mAnimator.start();
        }
    }

    protected void setBorderProgress(int borderProgress) {
        mAnimatorProgress = borderProgress;
        postInvalidate();
    }

    protected int getBorderProgress() {
        return mAnimatorProgress;
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
                            && isFocused();
            boolean visibleTouched =
                    (getWidth() - mBtnPadding * 3 - mBtnWidth * 2 < event.getX())
                            && (event.getX() < getWidth() - mBtnPadding * 3 - mBtnWidth)
                            && isPassword && isFocused();

            if (clearTouched) {
                setError(null);
                setText("");
                return true;
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
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 开始晃动的入口
     *
     * @param counts 0.5秒钟晃动多少下
     */
    public void startShakeAnimation(int counts){
        if(getAnimation() == null){
            setAnimation(shakeAnimation(counts));
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
            return getBitmap(context, resId);
        } else {
            return getBitmap(context, defResId);
        }
    }

    private static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }
}

