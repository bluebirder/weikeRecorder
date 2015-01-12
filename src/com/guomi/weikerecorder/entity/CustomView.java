/*
 * @(#)CustomView.java    Created on 2015年1月9日
 * Copyright (c) 2015 Guomi. All rights reserved.
 */
package com.guomi.weikerecorder.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Robin
 */
public class CustomView extends View {
    private int mLastX, mLastY; //上次触屏的位置
    private int mCurrX, mCurrY; //当前触屏的位置

    private Bitmap mBitmap; //保存每次绘画的结果
    private Paint mPaint;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStrokeWidth(6);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        //先将结果画到Bitmap上
        Canvas tmpCanvas = new Canvas(mBitmap);
        tmpCanvas.drawLine(mLastX, mLastY, mCurrX, mCurrY, mPaint);

        //再把Bitmap画到canvas上
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mLastX = mCurrX;
        mLastY = mCurrY;
        mCurrX = (int) event.getX();
        mCurrY = (int) event.getY();

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mLastX = mCurrX;
            mLastY = mCurrY;
            break;
        default:
            break;
        }

        invalidate();

        return true; //必须返回true
    }
}
