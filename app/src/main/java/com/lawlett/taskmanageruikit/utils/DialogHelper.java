package com.lawlett.taskmanageruikit.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.lawlett.taskmanageruikit.R;

public class DialogHelper {
    public void myDialog(Context context, ActionForDialog actionForDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.attention)
                .setMessage(R.string.are_you_sure_delete_all)
                .setPositiveButton(R.string.yes, (dialog, which) ->
                        actionForDialog.pressOk())
                .setNegativeButton(R.string.no, (dialog, which) ->
                        dialog.cancel()).show();
    }

    public void myDialog2(Context context, String title, String message,
                          String positiveButtonText, String negativeButtonText, ActionForDialog dialogAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        if (!positiveButtonText.equals("")) {
            builder.setPositiveButton(positiveButtonText, (dialog, which) ->
                    dialogAction.pressOk());
        }
        builder.setNegativeButton(negativeButtonText, (dialog, which) ->
                dialog.cancel());
        builder.show();
    }
    public void myDialogDeleteAll(Context context, ActionForDialog actionForDialog){
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
    public void myDialog(Context context, ActionForDialogSecond actionForDialogSecond, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        actionForDialogSecond.pressOkSecond();
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
    }
}

