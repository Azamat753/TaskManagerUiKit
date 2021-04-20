package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class LanguagePreference {

    public static volatile LanguagePreference instance;
    private SharedPreferences preferences;

    public LanguagePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("Language", Context.MODE_PRIVATE);
    }

    public static LanguagePreference getInstance(Context context) {
        if (instance == null) new LanguagePreference(context);
        return instance;
    }

    public String getLanguage() {
        return preferences.getString("My_Lang","");
    }

    public void saveLanguage(String lang) {
        preferences.edit().putString("My_Lang", lang).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
