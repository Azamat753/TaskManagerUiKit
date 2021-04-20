package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class AddDoneSizePreference {

    public static volatile AddDoneSizePreference instance;
    private SharedPreferences preferences;

    public AddDoneSizePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("doneSize", Context.MODE_PRIVATE);
    }

    public static AddDoneSizePreference getInstance(Context context) {
        if (instance == null) new AddDoneSizePreference(context);
        return instance;
    }

    public int getDataSize() {
        return preferences.getInt("done", 0);
    }

    public void saveDataSize(int size) {
        preferences.edit().putInt("done", size).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
