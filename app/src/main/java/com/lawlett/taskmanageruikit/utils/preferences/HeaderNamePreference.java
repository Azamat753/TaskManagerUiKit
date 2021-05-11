package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class HeaderNamePreference {

    public static volatile HeaderNamePreference instance;
    private SharedPreferences preferences;

    public HeaderNamePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("headerName", Context.MODE_PRIVATE);
    }

    public static HeaderNamePreference getInstance(Context context) {
        if (instance == null) new HeaderNamePreference(context);
        return instance;
    }

    public String returnName() {
        return preferences.getString("name", "");
    }

    public void saveName(String a) {
        preferences.edit().putString("name", a).apply();
    }

    public void clearName() {
        preferences.edit().clear().apply();
    }
}
