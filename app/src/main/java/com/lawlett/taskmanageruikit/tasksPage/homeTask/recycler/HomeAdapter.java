package com.lawlett.taskmanageruikit.tasksPage.homeTask.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.tasksPage.data.model.HomeModel;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    List<HomeModel> list = new ArrayList<>();
    IHCheckedListener listener;

    public HomeAdapter(IHCheckedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<HomeModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder {
        CheckBox homeTaskCheck;
        TextView homeTaskTv;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            homeTaskCheck = itemView.findViewById(R.id.home_task_check);
            homeTaskTv = itemView.findViewById(R.id.home_task_tv);
        }

        public void onBind(HomeModel homeModel) {
            homeTaskTv.setText(homeModel.getHomeTask());
            homeTaskCheck.setChecked(homeModel.isDone);
            homeTaskCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemCheckClick(getAdapterPosition());
                }
            });
            homeTaskTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemLongClick(getAdapterPosition());
                    return false;
                }
            });

        }
    }

    public interface IHCheckedListener {
        void onItemCheckClick(int id);
        void onItemLongClick(int id);
    }
}

