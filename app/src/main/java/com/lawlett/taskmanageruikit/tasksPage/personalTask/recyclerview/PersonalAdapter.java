package com.lawlett.taskmanageruikit.tasksPage.personalTask.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.tasksPage.data.model.PersonalModel;

import java.util.ArrayList;
import java.util.List;

public class PersonalAdapter extends RecyclerView.Adapter<PersonalAdapter.PersonalViewHolder> {
    List<PersonalModel> list = new ArrayList<>();
    ICheckedListener listener;

    public PersonalAdapter(ICheckedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PersonalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PersonalViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.personal_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<PersonalModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public class PersonalViewHolder extends RecyclerView.ViewHolder {
        CheckBox personalTaskCheck;
        TextView personalTaskTv;

        public PersonalViewHolder(@NonNull View itemView) {
            super(itemView);
            personalTaskCheck = itemView.findViewById(R.id.personal_task_check);
            personalTaskTv = itemView.findViewById(R.id.personal_task_tv);

        }

        public void onBind(PersonalModel personalModel) {
            personalTaskTv.setText(personalModel.getPersonalTask());
            personalTaskCheck.setChecked(personalModel.isDone);
            personalTaskCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemCheckClick(getAdapterPosition());
                }
            });
            personalTaskTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemLongClick(getAdapterPosition());
                    return false;
                }
            });
        }

    }
    public interface ICheckedListener {
        void onItemCheckClick(int id);
        void onItemLongClick(int pos);
    }
}