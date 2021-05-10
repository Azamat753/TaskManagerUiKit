package com.lawlett.taskmanageruikit.idea.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.idea.data.model.QuickModel;
import com.lawlett.taskmanageruikit.utils.preferences.IdeaViewPreference;

import java.util.List;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.QuickViewHolder> {
    List<QuickModel> list;
    Context context;
    ItemOnClickListener listener;
    ShowImageInterface showImageInterface;
    public MutableLiveData<Boolean> isChange = new MutableLiveData<>();

    public IdeaAdapter(List<QuickModel> list, ItemOnClickListener listener, ShowImageInterface showImageInterface, Context context) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.showImageInterface = showImageInterface;
    }

    @NonNull
    @Override
    public QuickViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.idea_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull QuickViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class QuickViewHolder extends RecyclerView.ViewHolder {
        TextView desc, data_created;
        TextView desc2, data_created2;
        ImageView imageDesc;
        ImageView imageDesc2;
        FrameLayout leftView, leftView2;
        CardView secondConst;
        ConstraintLayout firstConst;
        Boolean isGrid;

        public QuickViewHolder(@NonNull View itemView) {
            super(itemView);
            desc = itemView.findViewById(R.id.desc_quick);
            data_created = itemView.findViewById(R.id.data_quick);
            imageDesc = itemView.findViewById(R.id.image_desc);
            desc2 = itemView.findViewById(R.id.desc_quick2);
            data_created2 = itemView.findViewById(R.id.data_quick2);
            imageDesc2 = itemView.findViewById(R.id.image_desc2);
            leftView = itemView.findViewById(R.id.quItem_left_view);
            leftView2 = itemView.findViewById(R.id.quItem_left_view2);
            firstConst = itemView.findViewById(R.id.card);
            secondConst = itemView.findViewById(R.id.card2);
            isChange.observeForever(aBoolean -> checkView());
            checkView();
        }

        public void onBind(QuickModel quickModel) {
            desc.setText(quickModel.getTitle());
            data_created.setText(quickModel.getCreateData());
            desc2.setText(quickModel.getTitle());
            data_created2.setText(quickModel.getCreateData());
            imageDesc2.setOnClickListener(view -> showImageInterface.show(quickModel));
            imageDesc.setOnClickListener(view -> showImageInterface.show(quickModel));
            itemView.setOnClickListener(view -> listener.onItemClick(quickModel));
            leftView.setBackgroundColor(quickModel.getColor());
            leftView2.setBackgroundColor(quickModel.getColor());
                Glide.with(context).load(quickModel.getImage()).into(imageDesc);
                Glide.with(context).load(quickModel.getImage()).into(imageDesc2);
        }

        public void checkView() {
            isGrid = IdeaViewPreference.getInstance(context).getView();
            if (isGrid) {
                secondConst.setVisibility(View.VISIBLE);
                firstConst.setVisibility(View.GONE);
            } else {
                secondConst.setVisibility(View.GONE);
                firstConst.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface ShowImageInterface {
        void show(QuickModel quickModel);
    }

    public interface ItemOnClickListener {
        void onItemClick(QuickModel quickModel);
    }
}
