package com.lawlett.taskmanageruikit.utils.financeDialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.finance.AdviceText;
import com.lawlett.taskmanageruikit.utils.preferences.AdvicePreference;

public class AdviceDialog extends DialogFragment {
    private TextView title,subTitle;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_advice_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setTvText();
        view.findViewById(R.id.ok_tv_advice).setOnClickListener(v -> dismiss());

    }

    private void setTvText() {
        AdvicePreference prefs = new AdvicePreference(requireContext());

        Log.e("TAG", "setTvText: " + AdviceText.getTitleAdvice(prefs.getDayPosition()));
        Log.e("TAG", "setTvText: " + AdviceText.getDescAdvice(prefs.getDayPosition()));

        title.setText(AdviceText.getTitleAdvice(prefs.getDayPosition()));
        subTitle.setText(AdviceText.getDescAdvice(prefs.getDayPosition()));

    }

    private void initViews(View view) {
        title = view.findViewById(R.id.title_advice_dialog_tv);
        subTitle = view.findViewById(R.id.subTitle_advice_dialog_tv);
    }
}