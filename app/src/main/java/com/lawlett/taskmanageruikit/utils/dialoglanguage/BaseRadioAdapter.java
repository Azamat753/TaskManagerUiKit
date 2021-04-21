package com.lawlett.taskmanageruikit.utils.dialoglanguage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;

import java.util.List;

public abstract class BaseRadioAdapter<T> extends RecyclerView.Adapter<BaseRadioAdapter.ViewHolder> {
    public int mSelectedItem = -1;
    public List<T> mItems;
    private Context mContext;
    LanguageChooseListener listener;

    public BaseRadioAdapter(Context context, List<T> items,LanguageChooseListener listener) {
        mContext = context;
        mItems = items;
        this.listener=listener;
    }

    @Override
    public void onBindViewHolder(BaseRadioAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.mRadio.setChecked(i == mSelectedItem);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.list_language, viewGroup, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public RadioButton mRadio;

        public ViewHolder(final View inflate) {
            super(inflate);
            mRadio = (RadioButton) inflate.findViewById(R.id.radioButton);
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(getAdapterPosition());
                    mSelectedItem = getAdapterPosition();
                    notifyDataSetChanged();
                }
            };
            itemView.setOnClickListener(clickListener);
            mRadio.setOnClickListener(clickListener);
        }
    }
    public interface LanguageChooseListener{
        void onClick(int position);
    }
}