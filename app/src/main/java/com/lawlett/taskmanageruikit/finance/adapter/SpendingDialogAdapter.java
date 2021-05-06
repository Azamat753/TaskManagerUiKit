package com.lawlett.taskmanageruikit.finance.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.finance.model.SpendingModel;
import com.lawlett.taskmanageruikit.utils.App;

import java.util.List;

public class SpendingDialogAdapter extends RecyclerView.Adapter<SpendingDialogAdapter.ViewHolder> {
    List<SpendingModel> list;
    public SpendingDialogAdapter(){
        list = App.getDataBase().spendingDao().getAll();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.spending_item,parent,false));
    }
    public List<SpendingModel> getList(){
        return list;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView amount;
        private final TextView desc;
        private final TextView date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            amount = itemView.findViewById(R.id.spending_item_tvAmount);
            desc = itemView.findViewById(R.id.spending_item_tvDesc);
            date = itemView.findViewById(R.id.spending_item_tvTime);
        }

        public void onBind(SpendingModel model) {
            amount.setText(model.getAmount());
            desc.setText(model.getDescription());
            date.setText(DateUtils.formatDateTime
                    (itemView.getContext(), model.getDate(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                            | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR));
        }
    }
}
