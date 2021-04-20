package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class HomeDoneSizePreference {

    public static volatile HomeDoneSizePreference instance;
    private SharedPreferences preferences;

    public HomeDoneSizePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("homeSize", Context.MODE_PRIVATE);
    }

    public static HomeDoneSizePreference getInstance(Context context) {
        if (instance == null) new HomeDoneSizePreference(context);
        return instance;
    }

    public int getDataSize() {
        return preferences.getInt("home", 0);
    }

    public void saveDataSize(int size) {
        preferences.edit().putInt("home", size).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
