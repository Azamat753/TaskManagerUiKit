package com.lawlett.taskmanageruikit.tasksPage.workTask.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.tasksPage.data.model.WorkModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.WorkViewHolder> {
    List<WorkModel> list = new ArrayList<>();
    IWCheckedListener listener;

    public WorkAdapter(IWCheckedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WorkViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.work_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WorkViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<WorkModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public class WorkViewHolder extends RecyclerView.ViewHolder {
        CheckBox workTaskCheck;
        TextView workTaskTv;

        public WorkViewHolder(@NonNull View itemView) {
            super(itemView);
            workTaskCheck = itemView.findViewById(R.id.work_task_check);
            workTaskTv = itemView.findViewById(R.id.work_task_tv);
        }

        public void onBind(WorkModel workModel) {
            workTaskTv.setText(workModel.getWorkTask());
            workTaskCheck.setChecked(workModel.isDone);
            workTaskCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemCheckClick(getAdapterPosition());
                }
            });
            workTaskTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemLongClick(getAdapterPosition());
                    return false;
                }
            });
        }
    }

    public interface IWCheckedListener {
        void onItemCheckClick(int id);
        void onItemLongClick(int id);
    }
}
