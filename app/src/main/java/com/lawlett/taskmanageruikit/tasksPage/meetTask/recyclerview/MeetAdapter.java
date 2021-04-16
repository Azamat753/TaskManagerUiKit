package com.lawlett.taskmanageruikit.tasksPage.meetTask.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.tasksPage.data.model.MeetModel;

import java.util.ArrayList;
import java.util.List;

public class MeetAdapter extends RecyclerView.Adapter<MeetAdapter.MeetViewHolder> {
    List<MeetModel> list = new ArrayList<>();
    IMCheckedListener listener;

    public MeetAdapter(IMCheckedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MeetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MeetViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.meet_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MeetViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<MeetModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public class MeetViewHolder extends RecyclerView.ViewHolder {
        CheckBox meetTaskCheck;
        TextView meetTaskTv;

        public MeetViewHolder(@NonNull View itemView) {
            super(itemView);
            meetTaskCheck = itemView.findViewById(R.id.meet_task_check);
            meetTaskTv = itemView.findViewById(R.id.meet_task_tv);
        }

        public void onBind(MeetModel meetModel) {
            meetTaskTv.setText(meetModel.getMeetTask());
            meetTaskCheck.setChecked(meetModel.isDone);
            meetTaskCheck.setOnClickListener(v -> listener.onItemCheckClick(getAdapterPosition()));
            meetTaskTv.setOnLongClickListener(view -> {
                listener.onItemLongClick(getAdapterPosition());
                return false;
            });
        }
    }

    public interface IMCheckedListener {
        void onItemCheckClick(int id);

        void onItemLongClick(int pos);
    }
}
