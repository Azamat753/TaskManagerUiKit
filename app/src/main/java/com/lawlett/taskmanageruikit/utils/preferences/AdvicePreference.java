package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

public class AdvicePreference {
    private final SharedPreferences preferences;
    Calendar calendar = Calendar.getInstance();

    public AdvicePreference(Context context) {
        this.preferences = context.getSharedPreferences("CalendarOfDays",Context.MODE_PRIVATE);
        setDayPosition(calendar.get(Calendar.DAY_OF_MONTH));
    }

    public void setDayPosition(int pos){
        if ( pos <=25) preferences.edit().putInt("CurrentDay",pos).apply();
        else  preferences.edit().putInt("CurrentDay",31-pos).apply();
    }
    public int getDayPosition(){
        return preferences.getInt("CurrentDay",0);
    }

}
