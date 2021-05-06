package com.lawlett.taskmanageruikit.utils;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.lawlett.taskmanageruikit.R;

public class AlertDialogFragment extends DialogFragment {
    private final OkButtonClickListener listener;
    private TextView okTv, cancelTv;
    private EditText amountEdt;

    public AlertDialogFragment(OkButtonClickListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.fragment_alert_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initClickers();
    }


    private void initViews(View v) {
        okTv = v.findViewById(R.id.ok_tv_qt1);
        cancelTv = v.findViewById(R.id.cancel_tv_qt1);
        amountEdt = v.findViewById(R.id.income_amount_edt);
    }

    private void initClickers() {
        okTv.setOnClickListener(v -> save());
        cancelTv.setOnClickListener(v -> cancel());
    }

    private void cancel() {
        getDialog().cancel();
    }

    private void save() {
        String amount = amountEdt.getText().toString();
        if (!amount.equals("")) {
            listener.onClick(amount);
            getDialog().cancel();
        } else amountEdt.setHintTextColor(Color.RED);

    }
}