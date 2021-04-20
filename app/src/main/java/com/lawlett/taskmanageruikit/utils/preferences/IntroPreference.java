package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class IntroPreference {

    public static volatile IntroPreference instance;
    private SharedPreferences preferences;

    public IntroPreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public static IntroPreference getInstance(Context context) {
        if (instance == null) new IntroPreference(context);
        return instance;
    }

    public boolean isShown() {
        return preferences.getBoolean("isShown", false);
    }

    public void saveShown() {
        preferences.edit().putBoolean("isShown", true).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
