package com.lawlett.taskmanageruikit.timing.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.timing.model.TimingModel;

import java.util.List;

public class TimingAdapter extends RecyclerView.Adapter<TimingAdapter.TimingViewHolder> {
    List<TimingModel> list;

    public TimingAdapter(List<TimingModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public TimingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.timing_item, parent, false);
        return new TimingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TimingViewHolder holder, int position) {
        holder.onBind(list.get(position));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class TimingViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskMinute, taskDate;

        public TimingViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.timer_task_title);
            taskMinute = itemView.findViewById(R.id.minutes_amount);
            taskDate = itemView.findViewById(R.id.task_day);
        }

        @SuppressLint("SetTextI18n")
        public void onBind(TimingModel timingModel) {
            if (timingModel.getTimerTitle() == null) {
                taskTitle.setText(timingModel.getStopwatchTitle());
                taskMinute.setText(timingModel.getStopwatchMinutes() + " " + taskMinute.getContext().getString(R.string.minut));
                taskDate.setText(timingModel.getStopwatchDay());
            } else {
                taskTitle.setText(timingModel.getTimerTitle());
                taskMinute.setText(timingModel.getTimerMinutes() + " " + taskMinute.getContext().getString(R.string.minut));
                taskDate.setText(timingModel.getTimerDay());
            }
        }
    }
}
