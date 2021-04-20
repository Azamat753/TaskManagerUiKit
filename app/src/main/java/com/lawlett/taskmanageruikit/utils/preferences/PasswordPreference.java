package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PasswordPreference {

    public static volatile PasswordPreference instance;
    private SharedPreferences preferences;

    public PasswordPreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("password", Context.MODE_PRIVATE);
    }

    public static PasswordPreference getInstance(Context context) {
        if (instance == null) new PasswordPreference(context);
        return instance;
    }

    public String returnPassword() {
        return preferences.getString("keyword", "");
    }



    public void savePassword(String a) {
        preferences.edit().putString("keyword", a).apply();
    }

    public void clearPassword() {
        preferences.edit().clear().apply();
    }
}
