package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PasswordDonePreference {

    public static volatile PasswordDonePreference instance;
    private SharedPreferences preferences;

    public PasswordDonePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("done", Context.MODE_PRIVATE);
    }

    public static PasswordDonePreference getInstance(Context context) {
        if (instance == null) new PasswordDonePreference(context);
        return instance;
    }

    public boolean isShown() {
        return preferences.getBoolean("isDone", false);
    }

    public void saveShown() {
        preferences.edit().putBoolean("isDone", true).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
