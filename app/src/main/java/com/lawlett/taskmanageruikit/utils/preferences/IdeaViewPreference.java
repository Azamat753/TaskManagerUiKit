package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class IdeaViewPreference {

    public static volatile IdeaViewPreference instance;
    private SharedPreferences preferences;

    public IdeaViewPreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("IdeaView", Context.MODE_PRIVATE);
    }

    public static IdeaViewPreference getInstance(Context context) {
        if (instance == null) new IdeaViewPreference(context);
        return instance;
    }

    public boolean getView() {
        return preferences.getBoolean("mView",false);
    }

    public void saveView(Boolean lang) {
        preferences.edit().putBoolean("mView", lang).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}