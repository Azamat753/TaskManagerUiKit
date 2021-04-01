package com.lawlett.taskmanageruikit.tasksPage.addTask.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.tasksPage.data.model.DoneModel;
import com.lawlett.taskmanageruikit.tasksPage.data.model.MeetModel;

import java.util.ArrayList;
import java.util.List;

public class DoneAdapter extends RecyclerView.Adapter<DoneAdapter.DoneViewHolder>  {
    List<DoneModel> list = new ArrayList<>();
    DoneAdapter.IMCheckedListener listener;

    public DoneAdapter(IMCheckedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DoneAdapter.DoneViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.done_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DoneViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<DoneModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    public class DoneViewHolder extends RecyclerView.ViewHolder {
        CheckBox doneTaskCheck;
        TextView doneTaskTv;

        public DoneViewHolder(@NonNull View itemView) {
            super(itemView);
            doneTaskCheck = itemView.findViewById(R.id.done_task_check);
            doneTaskTv = itemView.findViewById(R.id.done_task_tv);
        }

        public void onBind(DoneModel doneModel) {
            doneTaskTv.setText(doneModel.getDoneTask());
            doneTaskCheck.setChecked(doneModel.isDone);
            doneTaskCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemCheckClick(getAdapterPosition());
                }
            });
            doneTaskTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemLongClick(getAdapterPosition());
                    return false;
                }
            });
        }
    }
    public interface IMCheckedListener {
        void onItemCheckClick(int id);
        void onItemLongClick(int id);
    }
}
