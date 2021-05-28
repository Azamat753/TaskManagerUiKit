package com.lawlett.taskmanageruikit.habit.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.room.HabitModel;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    public List<HabitModel> list;
    Context context;
    public   HabitAdapter.IMClickListener listener;

    public HabitAdapter(List<HabitModel> list, Context context, IMClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HabitViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class HabitViewHolder extends RecyclerView.ViewHolder {
        ImageView habitImg;
        ContentLoadingProgressBar habitProgress;
        TextView habitTitle, habitAmount, habitMenu;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            habitImg = itemView.findViewById(R.id.habit_image);
            habitProgress = itemView.findViewById(R.id.habit_progress);
            habitTitle = itemView.findViewById(R.id.habit_title);
            habitAmount = itemView.findViewById(R.id.habit_count);
            habitMenu = itemView.findViewById(R.id.habit_options);

        }

        @SuppressLint("SetTextI18n")
        public void onBind(HabitModel habitModel){
            habitTitle.setText(habitModel.getTitle());
            habitAmount.setText(habitModel.getCurrentDay() + " / " + habitModel.getAllDays());
            habitImg.setImageResource(habitModel.getImage());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(habitModel);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemLongClick(habitModel);
                    return false;
                }
            });

            habitMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onMenuItemClick(habitModel, habitMenu);
                }
            });

            habitProgress.setMax(Integer.parseInt(habitModel.getAllDays()));
            habitProgress.setProgress(habitModel.getCurrentDay());
        }
    }

    public interface IMClickListener {
        void onItemClick(HabitModel habitModel);
        void onItemLongClick(HabitModel habitModel);
        void onMenuItemClick(HabitModel habitModel, TextView textView);
    }
}