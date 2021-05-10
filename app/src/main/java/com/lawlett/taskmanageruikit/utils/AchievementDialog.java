package com.lawlett.taskmanageruikit.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.lawlett.taskmanageruikit.R;

public class AchievementDialog extends App {

    public static void showAchievementDialog(Activity activity, String alertTitle) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.achievement_alert_layout, null);
        Dialog alertDialog = new Dialog(activity);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView alertTitleView = alertDialog.findViewById(R.id.alert_title);
        alertTitleView.setText(alertTitle);
        alertDialog.findViewById(R.id.yesBtn).setOnClickListener(v -> {
            alertDialog.cancel();
        });
        alertDialog.show();
    }
}
