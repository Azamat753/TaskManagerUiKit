package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.lawlett.taskmanageruikit.R;

public class TaskDialogPreference {
    private static SharedPreferences sharedPreferences;
    private final static String PREF_NAME = "pref name";
    private final static String TITLE = "title";
    private final static String IMAGE = "image";

    private final static String TITLE_HOME = "title home";
    private final static String IMAGE_HOME = "image home";

    private final static String TITLE_MEET = "title meet";
    private final static String IMAGE_MEET = "image meet";

    private final static String TITLE_PERS = "title person";
    private final static String IMAGE_PERS = "image person";

    private final static String TITLE_WORK = "title work";
    private final static String IMAGE_WORK = "image work";

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveTitle(String title) {
        sharedPreferences.edit().putString(TITLE, title).apply();
    }

    public static void saveHomeTitle(String title) {
        sharedPreferences.edit().putString(TITLE_HOME, title).apply();
    }

    public static void saveMeetTitle(String title) {
        sharedPreferences.edit().putString(TITLE_MEET, title).apply();
    }

    public static void savePersonTitle(String title) {
        sharedPreferences.edit().putString(TITLE_PERS, title).apply();
    }

    public static void saveWorkTitle(String title) {
        sharedPreferences.edit().putString(TITLE_WORK, title).apply();
    }

    public static String getTitle() {
        return sharedPreferences.getString(TITLE, "");
    }

    public static String getHomeTitle() {
        return sharedPreferences.getString(TITLE_HOME, "");
    }

    public static String getMeetTitle() {
        return sharedPreferences.getString(TITLE_MEET, "");
    }

    public static String getPersonTitle() {
        return sharedPreferences.getString(TITLE_PERS, "");
    }

    public static String getWorkTitle() {
        return sharedPreferences.getString(TITLE_WORK, "");
    }

    public static void saveImage(int image) {
        sharedPreferences.edit().putInt(IMAGE, image).apply();
    }

    public static void saveHomeImage(int image) {
        sharedPreferences.edit().putInt(IMAGE_HOME, image).apply();
    }

    public static void saveMeetImage(int image) {
        sharedPreferences.edit().putInt(IMAGE_MEET, image).apply();
    }

    public static void savePersonImage(int image) {
        sharedPreferences.edit().putInt(IMAGE_PERS, image).apply();
    }

    public static void saveWorkImage(int image) {
        sharedPreferences.edit().putInt(IMAGE_WORK, image).apply();
    }

    public static int getImage() {
        return sharedPreferences.getInt(IMAGE, 0);
    }

    public static int getHomeImage() {
        return sharedPreferences.getInt(IMAGE_HOME, 0);
    }

    public static int getMeetImage() {
        return sharedPreferences.getInt(IMAGE_MEET, 0);
    }

    public static int getPersonImage() {
        return sharedPreferences.getInt(IMAGE_PERS, 0);
    }

    public static int getWorkImage() {
        return sharedPreferences.getInt(IMAGE_WORK, 0);
    }


    public static void remove() {
        sharedPreferences.edit().remove(TITLE).apply();
    }

    public static void removeHome() {
        sharedPreferences.edit().remove(TITLE_HOME).apply();
    }

    public static void removeMeet() {
        sharedPreferences.edit().remove(TITLE_MEET).apply();
    }

    public static void removePerson() {
        sharedPreferences.edit().remove(TITLE_PERS).apply();
    }

    public static void removeWork() {
        sharedPreferences.edit().remove(TITLE_WORK).apply();
    }

    public static boolean isShown() {
        return sharedPreferences.getBoolean("isShown", false);
    }

    public static void saveShown() {
        sharedPreferences.edit().putBoolean("isShown", true).apply();
    }
}
