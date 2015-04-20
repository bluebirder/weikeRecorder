/*
 * @(#)FileUtils.java    Created on 2015年3月2日
 * Copyright (c) 2015 Guomi. All rights reserved.
 */
package com.gm.weikerecorder.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * @author Robin
 */
public class FileUtils {
    public static String getPath(Context context, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }

        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}
