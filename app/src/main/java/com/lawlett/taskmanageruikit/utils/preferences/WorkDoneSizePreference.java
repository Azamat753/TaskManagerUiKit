package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class WorkDoneSizePreference {

    public static volatile WorkDoneSizePreference instance;
    private SharedPreferences preferences;

    public WorkDoneSizePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("workSize", Context.MODE_PRIVATE);
    }

    public static WorkDoneSizePreference getInstance(Context context) {
        if (instance == null) new WorkDoneSizePreference(context);
        return instance;
    }

    public int getDataSize() {
        return preferences.getInt("work", 0);
    }

    public void saveDataSize(int size) {
        preferences.edit().putInt("work", size).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
