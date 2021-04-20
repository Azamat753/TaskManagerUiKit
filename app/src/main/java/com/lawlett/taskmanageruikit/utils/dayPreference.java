package com.lawlett.taskmanageruikit.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class dayPreference {

    public static volatile dayPreference instance;
    private SharedPreferences preferences;

    public dayPreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("currentDay", Context.MODE_PRIVATE);
    }

    public static dayPreference getInstance(Context context) {
        if (instance == null) new dayPreference(context);
        return instance;
    }

    public String returntDay() {
        return preferences.getString("currentDayKey", "");
    }



    public void saveCurrentDay(String a) {
        preferences.edit().putString("currentDayKey", a).apply();
    }

    public void clearDay() {
        preferences.edit().clear().apply();
    }
}
