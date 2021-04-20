package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class MeetDoneSizePreference {

    public static volatile MeetDoneSizePreference instance;
    private SharedPreferences preferences;

    public MeetDoneSizePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("meetSize", Context.MODE_PRIVATE);
    }

    public static MeetDoneSizePreference getInstance(Context context) {
        if (instance == null) new MeetDoneSizePreference(context);
        return instance;
    }

    public int getDataSize() {
        return preferences.getInt("meet", 0);
    }

    public void saveDataSize(int size) {
        preferences.edit().putInt("meet", size).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
