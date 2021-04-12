package com.lawlett.taskmanageruikit.help.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;

import java.util.List;

public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.HelpViewHolder> {
    Context context;
    List<HelpModel> helpModels;
    MyOnItemClickListener onItemClickListener;

    public HelpAdapter(Context context, List<HelpModel> helpModels) {
        this.context = context;
        this.helpModels = helpModels;
    }

    public void setOnItemClickListener(MyOnItemClickListener myOnItemClickListener){
        onItemClickListener = myOnItemClickListener;

    }

    @NonNull
    @Override
    public HelpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HelpViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_help_recycler, parent, false), onItemClickListener);

    }

    @Override
    public void onBindViewHolder(@NonNull HelpViewHolder holder, int position) {
        holder.helpTitle.setText(helpModels.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return helpModels.size();
    }


    public class HelpViewHolder extends RecyclerView.ViewHolder {
        TextView helpTitle;
        LinearLayout itemHelp;

        public HelpViewHolder(@NonNull View itemView, MyOnItemClickListener listener) {
            super(itemView);
            helpTitle = itemView.findViewById(R.id.item_help_title);
            itemHelp = itemView.findViewById(R.id.item_help_recycler);
            itemView.setOnClickListener(view -> listener.onItemClick(getAdapterPosition()));
        }
    }

    public interface MyOnItemClickListener {
        void onItemClick(int id);
    }
}
