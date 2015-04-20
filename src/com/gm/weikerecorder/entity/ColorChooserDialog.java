/*
 * @(#)ColorChooserDialog.java    Created on 2015年3月13日
 * Copyright (c) 2015 Guomi. All rights reserved.
 */
package com.gm.weikerecorder.entity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * @author Robin
 */
public class ColorChooserDialog {

    public static String tag = "ColorChooserDialog";

    private String color = "#000000";

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // 参数说明
    // context:上下文
    // dialogid:对话框ID
    // title:对话框标题
    // callback:一个传递Bundle参数的回调接口
    public static Dialog createDialog(int id, Context context, String title, CallbackBundle callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(new ColorSelectView(context, id, callback));
        Dialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle(title);
        return dialog;
    }

    static class ColorSelectView extends GridView implements android.widget.AdapterView.OnItemClickListener {

        private CallbackBundle callback = null;
        private int dialogid = 1;

        public ColorSelectView(Context context, int dialogid, CallbackBundle callback) {
            super(context);
            this.callback = callback;
            this.dialogid = dialogid;
            this.setOnItemClickListener(this);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            // 如果是文件
            ((Activity) getContext()).dismissDialog(this.dialogid); // 让文件夹对话框消失

            // 设置回调的返回值
            Bundle bundle = new Bundle();
            bundle.putString("color", "color");
            // 调用事先设置的回调函数
            this.callback.callback(bundle);
        }
    }
}
