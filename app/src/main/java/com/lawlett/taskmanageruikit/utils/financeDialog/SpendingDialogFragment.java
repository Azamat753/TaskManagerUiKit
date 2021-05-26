package com.lawlett.taskmanageruikit.utils.financeDialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.finance.adapter.SpendingDialogAdapter;

public class SpendingDialogFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spending_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.spending_recycler);
        SpendingDialogAdapter adapter = new SpendingDialogAdapter();
        recyclerView.setAdapter(adapter);
        view.findViewById(R.id.dialog_spending_cancelTv).setOnClickListener(v -> dismiss());
    }
}