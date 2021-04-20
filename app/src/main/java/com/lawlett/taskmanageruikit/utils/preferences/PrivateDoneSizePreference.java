package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PrivateDoneSizePreference {

    public static volatile PrivateDoneSizePreference instance;
    private SharedPreferences preferences;

    public PrivateDoneSizePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("privateSize", Context.MODE_PRIVATE);
    }

    public static PrivateDoneSizePreference getInstance(Context context) {
        if (instance == null) new PrivateDoneSizePreference(context);
        return instance;
    }

    public int getDataSize() {
        return preferences.getInt("private", 0);
    }

    public void saveDataSize(int size) {
        preferences.edit().putInt("private", size).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
