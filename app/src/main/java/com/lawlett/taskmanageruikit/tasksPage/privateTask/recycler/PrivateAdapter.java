package com.lawlett.taskmanageruikit.tasksPage.privateTask.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.tasksPage.data.model.PrivateModel;

import java.util.ArrayList;
import java.util.List;

public class PrivateAdapter extends RecyclerView.Adapter<PrivateAdapter.PrivateViewHolder> {
    List<PrivateModel> list = new ArrayList<>();
    IPCheckedListener listener;

    public PrivateAdapter(IPCheckedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PrivateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PrivateViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.private_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PrivateViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<PrivateModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public class PrivateViewHolder extends RecyclerView.ViewHolder {
        CheckBox privateTaskCheck;
        TextView privateTaskTv;

        public PrivateViewHolder(@NonNull View itemView) {
            super(itemView);
            privateTaskCheck = itemView.findViewById(R.id.private_task_check);
            privateTaskTv = itemView.findViewById(R.id.private_task_tv);
        }

        public void onBind(PrivateModel privateModel) {
            privateTaskTv.setText(privateModel.getPrivateTask());
            privateTaskCheck.setChecked(privateModel.isDone);
            privateTaskCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemCheckClick(getAdapterPosition());
                }
            });
            privateTaskTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemLongClick(getAdapterPosition());
                    return false;
                }
            });
        }
    }

    public interface IPCheckedListener {
        void onItemCheckClick(int id);
        void onItemLongClick(int id);
    }
}

