package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PasswordPassDonePreference  {

    public static volatile PasswordPassDonePreference instance;
    private SharedPreferences preferences;

    public PasswordPassDonePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("pass", Context.MODE_PRIVATE);
    }

    public static PasswordPassDonePreference getInstance(Context context) {
        if (instance == null) new PasswordPassDonePreference(context);
        return instance;
    }

    public boolean isPass() {
        return preferences.getBoolean("isPass", false);
    }

    public void savePass() {
        preferences.edit().putBoolean("isPass", true).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
