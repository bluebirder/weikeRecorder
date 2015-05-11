/*
 * @(#)PaintUtils.java    Created on 2015年1月16日
 * Copyright (c) 2015 Guomi. All rights reserved.
 */
package com.gm.weikerecorder.util;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * @author Robin
 */
public class PaintUtils {

    public static final int PENCIL_WIDTH = 3;
    public static final int ERASER_WIDTH = 6;
    public static final int BROWSER_WIDTH = 10;
    public static final int FONT_SIZE = 12;

    public static void changeTools(Paint paint, Character tool) {
        switch (tool) {
        case 'p':
            paint.setStrokeWidth(PENCIL_WIDTH);
            paint.setColor(Color.BLACK);
            break;
        case 'e':
            paint.setStrokeWidth(ERASER_WIDTH);
            paint.setColor(Color.WHITE);
            break;
        case 'b':
            paint.setStrokeWidth(BROWSER_WIDTH);
            paint.setColor(Color.BLACK);
            break;
        }
    }
}
