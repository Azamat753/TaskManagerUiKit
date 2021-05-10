package com.lawlett.taskmanageruikit.finance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.finance.model.MainRecyclerModel;
import com.lawlett.taskmanageruikit.utils.IIdeaOnClickListener;

import java.util.ArrayList;

public class FinanceMainAdapter extends RecyclerView.Adapter<FinanceMainAdapter.MainViewHolder> {
    ArrayList<MainRecyclerModel> list;
    IIdeaOnClickListener listener;
    public FinanceMainAdapter(IIdeaOnClickListener listener) {
        this.listener = listener;
        list = new ArrayList<>();
        list.add(new MainRecyclerModel("СОВЕТЫ", R.drawable.ic_advice));
        list.add(new MainRecyclerModel("РАСХОДЫ", R.drawable.ic_currency_usd));
        list.add(new MainRecyclerModel("ПОМОЩЬ", R.drawable.ic_baseline_help_outline_24));
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MainViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_finance_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class MainViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemTextView;
        private final ImageView itemImageView;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
            itemTextView = itemView.findViewById(R.id.item_textView);
            itemImageView = itemView.findViewById(R.id.item_imageView);
        }

        public void onBind(MainRecyclerModel model) {
            itemTextView.setText(model.getTitle());
            itemImageView.setImageResource(model.getImage());
        }
    }
}
