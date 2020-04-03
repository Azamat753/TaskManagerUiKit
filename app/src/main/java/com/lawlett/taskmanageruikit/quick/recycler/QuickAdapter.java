package com.lawlett.taskmanageruikit.quick.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lawlett.taskmanageruikit.quick.data.model.QuickModel;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.utils.IOnClickListener;

import java.util.Calendar;
import java.util.List;

public class QuickAdapter extends RecyclerView.Adapter<QuickAdapter.QuickViewHolder> {
    List<QuickModel> list;
    Context context;
    IOnClickListener listener;

    public QuickAdapter(List<QuickModel> list,IOnClickListener listener, Context context) {
        this.context = context;
        this.list = list;
        this.listener=listener;
    }

    @NonNull
    @Override
    public QuickViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.quick_item, parent, false),listener);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

//    public void setOnClickListener(IOnClickListener listener) {
//        this.listener = listener;
//    }

    public class QuickViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener, View.OnLongClickListener {
        TextView title, desc, data_created;
        ImageView imageDesc;
        Calendar c = Calendar.getInstance();
        IOnClickListener listeneer;
        final int year = c.get(Calendar.YEAR);
        String[] monthName = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль",
                "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};


        final String month = monthName[c.get(Calendar.MONTH)];

        public QuickViewHolder(@NonNull View itemView,IOnClickListener listeneer) {
            super(itemView);
            title = itemView.findViewById(R.id.title_quick);
            desc = itemView.findViewById(R.id.desc_quick);
            data_created = itemView.findViewById(R.id.data_quick);
            imageDesc = itemView.findViewById(R.id.image_desc);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener( this);
            this.listeneer=listeneer;

        }
        public void onBind(QuickModel quickModel) {
            title.setText(quickModel.getTitle());
            desc.setText(quickModel.getDescription());
            data_created.setText(quickModel.getCreateData() + month + "  " + year);
            Glide.with(context).load(quickModel.getImage()).into(imageDesc);

        }
        @Override
        public void onClick(View v) {
            listeneer.onItemClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            listeneer.onItemLongClick(getAdapterPosition());
            return false;
        }
    }
}
