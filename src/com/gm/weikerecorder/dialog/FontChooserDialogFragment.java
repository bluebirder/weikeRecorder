/*
 * @(#)FontChooserDialogFragment.java    Created on 2015年4月28日
 * Copyright (c) 2015 Guomi. All rights reserved.
 */
package com.gm.weikerecorder.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.gm.weikerecorder.entity.CallbackBundle;
import com.gm.weikerecorder.entity.FontChooserDialog;
import com.gm.weikerecorder.util.DialogIds;

/**
 * @author Robin
 */
public class FontChooserDialogFragment extends DialogFragment {

    public static FontChooserDialogFragment newInstance(String title) {
        FontChooserDialogFragment frag = new FontChooserDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        final Activity activity = getActivity();

        Dialog dialog = FontChooserDialog.createDialog(DialogIds.fontDialogId, activity, title, new CallbackBundle() {
            @Override
            public void callback(Bundle bundle) {
            }
        });
        return dialog;
    }

}
