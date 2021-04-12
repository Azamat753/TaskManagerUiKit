package com.lawlett.taskmanageruikit.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.lawlett.taskmanageruikit.R;

public class DialogHelper  {
    public void myDialog(Context context, ActionForDialog actionForDialog){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.attention)
                .setMessage(R.string.are_you_sure_delete_all)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        actionForDialog.pressOk();
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }
}

