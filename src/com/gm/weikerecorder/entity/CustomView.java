/*
 * @(#)CustomView.java    Created on 2015年1月9日
 * Copyright (c) 2015 Guomi. All rights reserved.
 */
package com.gm.weikerecorder.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.gm.weikerecorder.util.PaintUtils;

/**
 * @author Robin
 */
public class CustomView extends View {
    private int mLastX, mLastY; // 上次触屏的位置
    private int mCurrX, mCurrY; // 当前触屏的位置

    private Bitmap mBitmap; // 保存每次绘画的结果
    private Canvas mCanvas;
    private Paint mPaint;
    private boolean cut = false; // 是否断的

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas cvs) {
        super.onDraw(cvs);

        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        //先将结果画到Bitmap上
        mCanvas.drawLine(mLastX, mLastY, mCurrX, mCurrY, mPaint);

        //再把Bitmap画到canvas上
        cvs.drawBitmap(mBitmap, 0, 0, mPaint);
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
            cut = false;
            break;
        case MotionEvent.ACTION_UP:
            cut = true;
            break;
        default:
            break;
        }

        invalidate();

        return true; //必须返回true
    }

    public void clearCanvas() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        invalidate();
    }

    public void clearCanvas2() {
        Bitmap mp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Paint pt = new Paint();

        drawImg(mp, pt);
    }

    public void drawImg(Bitmap map, Paint pt) {
        mCanvas.drawBitmap(map, 0, 0, pt);

        invalidate();
    }

    public boolean isCut() {
        return cut;
    }

    public void changeTools(Character tool) {
        PaintUtils.changeTools(mPaint, tool);
    }
}
