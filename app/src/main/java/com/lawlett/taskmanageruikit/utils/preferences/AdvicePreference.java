package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Random;

public class AdvicePreference {
    private final SharedPreferences preferences;
    Calendar calendar = Calendar.getInstance();

    public AdvicePreference(Context context) {
        this.preferences = context.getSharedPreferences("CalendarOfDays", Context.MODE_PRIVATE);
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        if (!(today == getDayPosition())) {
            Random random = new Random();
            int advicePosition = random.nextInt(47);
            setDayPosition(advicePosition);
        }
        preferences.edit().clear().apply();
        setDayPosition(today);

    }

    public void setDayPosition(int pos) {
        preferences.edit().putInt("CurrentDay", pos).apply();
    }

    public int getDayPosition() {
        return preferences.getInt("CurrentDay", 0);
    }

}
