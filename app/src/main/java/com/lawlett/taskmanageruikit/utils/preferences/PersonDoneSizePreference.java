package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PersonDoneSizePreference {

    public static volatile PersonDoneSizePreference instance;
    private SharedPreferences preferences;

    public PersonDoneSizePreference(Context context) {
        instance = this;
        preferences = context.getSharedPreferences("personalSize", Context.MODE_PRIVATE);
    }

    public static PersonDoneSizePreference getInstance(Context context) {
        if (instance == null) new PersonDoneSizePreference(context);
        return instance;
    }

    public int getPersonalSize() {
        return preferences.getInt("personal", 0);
    }

    public void savePersonalSize(int size) {
        preferences.edit().putInt("personal", size).apply();
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}
