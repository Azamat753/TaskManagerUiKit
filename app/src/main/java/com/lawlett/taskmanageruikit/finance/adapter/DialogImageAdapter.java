package com.lawlett.taskmanageruikit.finance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;

import java.util.ArrayList;

public class DialogImageAdapter extends RecyclerView.Adapter<DialogImageAdapter.ViewHolder> {
    private final ArrayList<Integer> list = new ArrayList<>();
    private Animation animation ;
    private IIdeaOnClickListener listener;

    public DialogImageAdapter() {
        int[] image = new int[]{R.drawable.ic_01, R.drawable.ic_baseline_coffee_24, R.drawable.ic_03, R.drawable.ic_04,
                R.drawable.ic_taxi, R.drawable.ic_06, R.drawable.ic_07, R.drawable.ic_08,
                R.drawable.ic_09, R.drawable.ic_10, R.drawable.ic_11, R.drawable.ic_12,
                R.drawable.ic_smoking, R.drawable.ic_alcoghol, R.drawable.ic_15,R.drawable.ic_16,
                R.drawable.ic_alcoghol, R.drawable.ic_18, R.drawable.ic_19,R.drawable.ic_20,
                R.drawable.ic_21,R.drawable.ic_22, R.drawable.ic_wifi,R.drawable.ic_fastfood};
        for (int i : image) {
            list.add(i);
        }
        notifyItemInserted(list.size() - 1);
    }

    public int getItem(int position) {
        return list.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        animation = AnimationUtils.loadAnimation(parent.getContext(), R.anim.rotate);
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_image_rv, parent, false));
    }

    private int row_index = -1;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(list.get(position));
        holder.imageView.setOnClickListener(v -> {
            if (listener != null) {
                row_index = position;
                notifyDataSetChanged();
                listener.onItemClick(position);
            }
        });
        if (row_index == position) {

            holder.imageView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.iconBackground));
            holder.imageView.setAnimation(animation);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
        }

        public void onBind(Integer dialogImage) {
            imageView.setImageResource(dialogImage);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(getAdapterPosition());
            }
        }
    }

    public void setAdapterOnClick(IIdeaOnClickListener itemClickListener) {
        this.listener = itemClickListener;
    }
    public interface IIdeaOnClickListener {
        void onItemClick(int position);
    }
}