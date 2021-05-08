package com.lawlett.taskmanageruikit.utils;

import android.app.AlertDialog;
import android.content.Context;

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
}

