package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class DayPreference {

    public static volatile DayPreference instance;
    private SharedPreferences preferences;

    public DayPreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("currentDay", Context.MODE_PRIVATE);
    }
    public static DayPreference getInstance(Context context) {
        if (instance == null) new DayPreference(context);
        return instance;
    }
    public String returnDay() {
        return preferences.getString("currentDayKey", "");
    }
    public void saveCurrentDay(String a) {
        preferences.edit().putString("currentDayKey", a).apply();
    }
    public void clearDay() {
        preferences.edit().clear().apply();
    }
}
