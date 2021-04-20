package com.lawlett.taskmanageruikit.splash;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.main.MainActivity;
import com.lawlett.taskmanageruikit.onboard.BoardActivity;
import com.lawlett.taskmanageruikit.utils.preferences.LanguagePreference;
import com.lawlett.taskmanageruikit.utils.preferences.IntroPreference;
import com.lawlett.taskmanageruikit.utils.preferences.ThemePreference;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkedTheme();
        loadLocale();
        setContentView(R.layout.activity_splash);
            new Handler().postDelayed(() -> {
                boolean isShown = IntroPreference.getInstance(getApplication()).isShown();
                if (isShown) {
                    startActivity(new Intent(getApplication(), MainActivity.class));
                } else {
                    startActivity(new Intent(getApplication(), BoardActivity.class));
                }
                finish();

            }, 1000);
    }

    private void checkedTheme() {
        boolean booleanValue = ThemePreference.getInstance(this).getTheme();
        if (booleanValue) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
    public void loadLocale(){
        String language= LanguagePreference.getInstance(this).getLanguage();
        setLocale(language);
    }
}