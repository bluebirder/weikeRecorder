/*
 * @(#)ColorChooserDialogFragment.java    Created on 2015年3月23日
 * Copyright (c) 2015 Guomi. All rights reserved.
 */
package com.gm.weikerecorder.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.gm.weikerecorder.entity.CallbackBundle;
import com.gm.weikerecorder.entity.ColorChooserDialog;
import com.gm.weikerecorder.util.DialogIds;

/**
 * @author Robin
 */
public class ColorChooserDialogFragment extends DialogFragment {

    public static ColorChooserDialogFragment newInstance(String title) {
        ColorChooserDialogFragment frag = new ColorChooserDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        final Activity activity = getActivity();

        Dialog dialog = ColorChooserDialog.createDialog(DialogIds.colorDialogId, activity, title, new CallbackBundle() {
            @Override
            public void callback(Bundle bundle) {
            }
        });
        return dialog;
    }

}
