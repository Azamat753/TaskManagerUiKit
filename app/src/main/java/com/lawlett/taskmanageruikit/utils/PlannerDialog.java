package com.lawlett.taskmanageruikit.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.lawlett.taskmanageruikit.R;

public class PlannerDialog extends App {
    public static PlannerDialogClick listener;

    public PlannerDialog(PlannerDialogClick listener) {
        this.listener = listener;
    }

    public static void showPlannerDialog(Activity activity,String alertTitle, PlannerDialogClick plannerDialog) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.delete_alert_layout, null);
        TextView alertTitleView =activity.findViewById(R.id.alert_title);
        alertTitleView.setText(alertTitle);
        Dialog alertDialog = new Dialog(activity);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.findViewById(R.id.yesBtn).setOnClickListener(v -> {
            plannerDialog.clickOnYes();
            alertDialog.cancel();
        });

        alertDialog.findViewById(R.id.noBtn).setOnClickListener(v -> {
            alertDialog.cancel();
        });
        alertDialog.show();
    }

    public interface PlannerDialogClick {
        void clickOnYes();
    }
}
