package com.lawlett.taskmanageruikit.finance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.finance.model.FrequentSpendingModel;

import java.util.List;

public class FrequentSpendingAdapter extends RecyclerView.Adapter<FrequentSpendingAdapter.ViewHolder> {
    public List<FrequentSpendingModel> list;
    final DialogImageAdapter.IIdeaOnClickListener listener;

    public FrequentSpendingAdapter(List<FrequentSpendingModel> list, DialogImageAdapter.IIdeaOnClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frequent_spending, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void update(List<FrequentSpendingModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount;
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
            tvName = itemView.findViewById(R.id.expenses_item_text_tv);
            tvAmount = itemView.findViewById(R.id.expenses_item_amount_tv);
            img = itemView.findViewById(R.id.expenses_item_iv);
        }

        public void onBind(FrequentSpendingModel model) {
            img.setImageResource(model.getImage());
            tvName.setText(model.getName());
            tvAmount.setText(model.getAmount());
        }
    }
}
