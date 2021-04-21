package com.lawlett.taskmanageruikit.utils.dialoglanguage;

import android.content.Context;

import java.util.List;

public class LanguageAdapter extends BaseRadioAdapter<String> {
    public LanguageAdapter(Context context, List<String> items,LanguageChooseListener listener){
        super(context, items,listener);
    }

    @Override
    public void onBindViewHolder(BaseRadioAdapter.ViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        viewHolder.mRadio.setText(mItems.get(i));
    }
}