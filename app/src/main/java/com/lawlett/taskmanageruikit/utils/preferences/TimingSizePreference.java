package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class TimingSizePreference {

    public static volatile TimingSizePreference instance;
    private SharedPreferences preferences;

    public TimingSizePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("timingSize", Context.MODE_PRIVATE);
    }

    public static TimingSizePreference getInstance(Context context) {
        if (instance == null) new TimingSizePreference(context);
        return instance;
    }

    public int getTimingSize() {
        return preferences.getInt("timing", 0);
    }

    public void saveTimingSize(int size) {
        preferences.edit().putInt("timing", size).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
