/*
 * @(#)FileChooserDialogFragment.java    Created on 2015年3月13日
 * Copyright (c) 2015 Guomi. All rights reserved.
 */
package com.gm.weikerecorder.dialog;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.gm.weikerecorder.entity.CallbackBundle;
import com.gm.weikerecorder.entity.OpenFileDialog;
import com.gm.weikerecorder.util.DialogIds;
import com.guomi.weikerecorder.R;

/**
 * @author Robin
 */
public class FileChooserDialogFragment extends DialogFragment {

    public static FileChooserDialogFragment newInstance(String title) {
        FileChooserDialogFragment frag = new FileChooserDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        final Activity activity = getActivity();

        Map<String, Integer> images = new HashMap<String, Integer>();
        // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
        images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root); // 根目录图标
        images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up); //返回上一层的图标
        images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder); //文件夹图标
        images.put("ppt", R.drawable.filedialog_pptfile); //wav文件图标
        images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
        Dialog dialog = OpenFileDialog.createDialog(DialogIds.openfileDialogId, activity, title, new CallbackBundle() {
            @Override
            public void callback(Bundle bundle) {
                String filepath = bundle.getString("path");
                activity.setTitle(filepath); // 把文件路径显示在标题上
                // PPTUtils.doPPTtoImage(new File(filepath)); // 暂时无法实现
            }
        }, ".ppt;", images);
        return dialog;
    }
}
