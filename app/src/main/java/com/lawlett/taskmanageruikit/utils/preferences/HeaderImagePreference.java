package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class HeaderImagePreference {

    public static volatile HeaderImagePreference instance;
    private SharedPreferences preferences;

    public HeaderImagePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("headerImage", Context.MODE_PRIVATE);
    }
    public static HeaderImagePreference getInstance(Context context) {
        if (instance == null) new HeaderImagePreference(context);
        return instance;
    }
    public String returnImage() {
        return preferences.getString("image", "");
    }
    public void saveImage(String a) {
        preferences.edit().putString("image", a).apply();
    }
    public void clearImage() {
        preferences.edit().clear().apply();
    }
}
