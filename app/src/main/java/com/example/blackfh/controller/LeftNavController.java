package com.example.blackfh.controller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by BlackFH on 2017/2/6.
 */

public class LeftNavController extends View {
    private int innerColor;
    private int outerColor;
    private final static int INNER_COLOR_DEFAULT = Color.parseColor("#d32f2f");
    private final static int OUTER_COLOR_DEFAULT = Color.parseColor("#f44336");
    private int OUTER_WIDTH_SIZE;
    private int OUTER_HEIGHT_SIZE;
    private int realWidth;//绘图使用的宽
    private int realHeight;//绘图使用的高
    private float innerCenterX;
    private float innerCenterY;
    private float outRadius;
    private float innerRedius;
    private Paint outerPaint;
    private Paint innerPaint;
    private OnNavAndSpeedListener mCallBack = null;
    private float unitValue;
    private TextView thrNum;
    private TextView yawNum;
    private int i = 0;

    public interface OnNavAndSpeedListener {
        public void onNavAndSpeed(float nav, float speed);
    }

    public LeftNavController(Context context) {
        this(context, null);
    }

    public LeftNavController(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = getResources().obtainAttributes(attrs, R.styleable.RightNavController);
        innerColor = ta.getColor(R.styleable.RightNavController_InnerColor, INNER_COLOR_DEFAULT);
        outerColor = ta.getColor(R.styleable.RightNavController_OuterColor, OUTER_COLOR_DEFAULT);
        thrNum = (TextView) findViewById(R.id.thrNum);
        yawNum = (TextView) findViewById(R.id.yawNum);
//        yawNum.setText(String.valueOf(1500));
//        thrNum.setText(String.valueOf(0));
        ta.recycle();
        OUTER_WIDTH_SIZE = dip2px(context, 125.0f);
        OUTER_HEIGHT_SIZE = dip2px(context, 125.0f);
        outerPaint = new Paint();
        innerPaint = new Paint();
        outerPaint.setColor(outerColor);
        outerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        innerPaint.setColor(innerColor);
        innerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int widthMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthVal = MeasureSpec.getSize(widthMeasureSpec);
        //处理三种模式
        if (widthMode == MeasureSpec.EXACTLY) {
            return widthVal + getPaddingLeft() + getPaddingRight();
        } else if (widthMode == MeasureSpec.UNSPECIFIED) {
            return OUTER_WIDTH_SIZE;
        } else {
            return Math.min(OUTER_WIDTH_SIZE, widthVal);
        }
    }

    private int measureHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightVal = MeasureSpec.getSize(heightMeasureSpec);
        //处理三种模式
        if (heightMode == MeasureSpec.EXACTLY) {
            return heightVal + getPaddingTop() + getPaddingBottom();
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            return OUTER_HEIGHT_SIZE;
        } else {
            return Math.min(OUTER_HEIGHT_SIZE, heightVal);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        realWidth = w;
        realHeight = h;
        innerCenterX = realWidth / 2;
        innerCenterY = realHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        outRadius = Math.min(Math.min(realWidth / 2 - getPaddingLeft(), realWidth / 2 - getPaddingRight()), Math.min(realHeight / 2 - getPaddingTop(), realHeight / 2 - getPaddingBottom()));
        //画外部圆
        canvas.drawRoundRect(realWidth / 2 - outRadius, realHeight / 2 - outRadius, realWidth / 2 + outRadius, realHeight / 2 + outRadius, outRadius / 4, outRadius / 4, outerPaint);
        //内部圆
        innerRedius = outRadius * 0.4f;
//        innerCenterY = innerCenterY+innerRedius/2;
        canvas.drawCircle(innerCenterX, innerCenterY, innerRedius, innerPaint);
        if (i == 0) {
            i++;
            innerCenterY = realHeight / 2 + outRadius;
            invalidate();
        }
        //计算单个像素对应的数值pit/rol=0~3000，中值1500
        unitValue = 3000 / outRadius / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            changeInnerCirclePosition(event);

        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            changeInnerCirclePosition(event);
            float yaw = (event.getX() - realWidth / 2 + outRadius) * unitValue;
            float thr = 1000 - ((event.getY() - realHeight / 2 + outRadius) * (unitValue / 3));
//            Log.i("TAG",event.getX()-realWidth/2+"----11111---"+(innerCenterY)+"-----"+unitValue);
//            Log.i("TAG",outRadius+"MOVED"+event.getY());
            if (yaw <= 0) {
                yaw = 0;
            } else if (yaw >= 3000) {
                yaw = 3000;
            }
            if (thr <= 0) {
                thr = 0;
            } else if (thr >= 1000) {
                thr = 1000;
            }
//            yawNum.setText(String.valueOf(yaw));
//            thrNum.setText(String.valueOf(thr));
            //在此处调用yaw，thr值
            Log.i("TAG", yaw + "MOVED" + thr);

        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            innerCenterX = realWidth / 2;
//            innerCenterY = realHeight / 2 + outRadius / 2;
            invalidate();

        }
        return true;
    }

    private void changeInnerCirclePosition(MotionEvent e) {
        //圆的方程：（x-realWidth/2）^2 +（y - realHeight/2）^2 <= outRadius^2
        //第一步，确定有效的触摸点集
        float X = e.getX();
        float Y = e.getY();
        if (mCallBack != null) {
            mCallBack.onNavAndSpeed(X, Y);
        }
//        boolean isPointInOutCircle = Math.pow(X-realWidth/2,2) +Math.pow(Y-realHeight/2,2)<=Math.pow(outRadius,2);
        boolean isPointInOutCircle = true;
        if (isPointInOutCircle) {
            Log.i("TAG", "inCircle");
            innerCenterX = X;
            innerCenterY = Y;
            if (Y >= realHeight / 2 + outRadius) {
                innerCenterY = realHeight / 2 + outRadius;
            } else if (Y < realHeight / 2 - outRadius) {
                innerCenterY = realHeight / 2 - outRadius;
            }
            if (X <= realWidth / 2 - outRadius) {
                innerCenterX = realWidth / 2 - outRadius;
            } else if (X >= realWidth / 2 + outRadius) {
                innerCenterX = realWidth / 2 + outRadius;
            }
            invalidate();
        } else {
            Log.i("TAG", "notInCircle");
        }
    }

    public void setOnNavAndSpeedListener(OnNavAndSpeedListener listener) {
        mCallBack = listener;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}