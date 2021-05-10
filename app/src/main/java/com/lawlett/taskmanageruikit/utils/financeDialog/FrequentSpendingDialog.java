package com.lawlett.taskmanageruikit.utils.financeDialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.finance.adapter.DialogImageAdapter;
import com.lawlett.taskmanageruikit.finance.model.FrequentSpendingModel;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.IIdeaOnClickListener;
import com.lawlett.taskmanageruikit.utils.OkButtonClickListener;

import java.util.Objects;

public class FrequentSpendingDialog extends DialogFragment implements IIdeaOnClickListener {
    private int imgResId = 0;
    private TextView okTv,cancelTv,pickTheImageTv;
    private EditText spendingNameEdt;
    OkButtonClickListener listener;

    public FrequentSpendingDialog(OkButtonClickListener listener) {
        this.listener = listener;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog()).getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.frequent_spending_dialog, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setClickers();
    }

    private void initViews(View view) {
        DialogImageAdapter adapter = new DialogImageAdapter();
        RecyclerView recyclerView = view.findViewById(R.id.frequent_dialog_recycler);
        recyclerView.setAdapter(adapter);
        adapter.setAdapterOnClick(this);
        okTv = view.findViewById(R.id.ok_tv);
        cancelTv = view.findViewById(R.id.cancel_tv);
        pickTheImageTv = view.findViewById(R.id.pick_the_image);
        spendingNameEdt = view.findViewById(R.id.spending_name_edt);
    }

    private void setClickers() {
    okTv.setOnClickListener(v -> save());
    cancelTv.setOnClickListener(v -> cancel());
    }

    private void cancel() {
        Objects.requireNonNull(getDialog()).cancel();
    }

    private void save() {
        String name;
        name = spendingNameEdt.getText().toString();
        if (checkDataEntered(name)) {
            App.getDataBase().frequentSpendingDao().insert(new FrequentSpendingModel(name, imgResId, "0"));
            Objects.requireNonNull(getDialog()).cancel();
        }
    }

    private boolean checkDataEntered(String name) {
        if (!name.equals("") && imgResId != 0) return true;
        if (name.equals("")) {
            spendingNameEdt.setHintTextColor(Color.RED);
        }
        if (imgResId == 0) {
            pickTheImageTv.setTextColor(Color.RED);
        }
        return false;
    }

    @Override
    public void onItemClick(int position) {
        imgResId = new DialogImageAdapter().getItem(position);
    }
}