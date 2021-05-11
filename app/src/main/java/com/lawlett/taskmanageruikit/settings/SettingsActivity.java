package com.lawlett.taskmanageruikit.settings;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.auth.GoogleSignInActivity;
import com.lawlett.taskmanageruikit.splash.SplashActivity;
import com.lawlett.taskmanageruikit.utils.Constants;
import com.lawlett.taskmanageruikit.utils.PassCodeActivity;
import com.lawlett.taskmanageruikit.utils.dialoglanguage.BaseRadioAdapter;
import com.lawlett.taskmanageruikit.utils.dialoglanguage.GridSpacingItemDecoration;
import com.lawlett.taskmanageruikit.utils.dialoglanguage.LanguageAdapter;
import com.lawlett.taskmanageruikit.utils.preferences.LanguagePreference;
import com.lawlett.taskmanageruikit.utils.preferences.PasswordDonePreference;
import com.lawlett.taskmanageruikit.utils.preferences.PasswordPreference;
import com.lawlett.taskmanageruikit.utils.preferences.ThemePreference;
import com.lawlett.taskmanageruikit.utils.preferences.TimingSizePreference;

import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity implements BaseRadioAdapter.LanguageChooseListener {
    private LinearLayout language_tv, clear_password_layout, clearMinutes_layout, share_layout, rate_layout, reviews, sign_in;
    private ImageView back;
    private ImageView imageTheme;
    private ConstraintLayout theme_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_settings);
        initViews();
        initClickers();
        checkUser();
        setThemeImage();
    }
    private void initClickers() {
        theme_layout.setOnClickListener(v -> {
            if (!ThemePreference.getInstance(SettingsActivity.this).getTheme()) {
                ThemePreference.getInstance(SettingsActivity.this).saveNightTheme();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                ThemePreference.getInstance(SettingsActivity.this).saveLightTheme();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
        sign_in.setOnClickListener(view -> startActivity(new Intent(SettingsActivity.this, GoogleSignInActivity.class)));

        back.setOnClickListener(v -> onBackPressed());

        share_layout.setOnClickListener(v -> {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Planner");
                String shareMessage = "\nPlanner\n";
                shareMessage = shareMessage + Constants.APP_LINK;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_app)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        rate_layout.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(Constants.APP_LINK));
            startActivity(i);
        });

        clear_password_layout.setOnClickListener(v -> {
            SharedPreferences sPref = getSharedPreferences("qst", 0);
            String answer = sPref.getString(PassCodeActivity.SAVED_ANSWER, null);
            EditText answerInput = new EditText(SettingsActivity.this);
            AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this);
            String pass = PasswordPreference.getInstance(SettingsActivity.this).returnPassword();
            if (!pass.equals("") && answer == null) {
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.clear_password)
                        .setNegativeButton(R.string.no, (dialog1, which) ->
                                dialog1.cancel())
                        .setPositiveButton(R.string.yes, (dialog12, which) -> {
                            PasswordPreference.getInstance(SettingsActivity.this).clearPassword();
                            PasswordDonePreference.getInstance(SettingsActivity.this).clearSettings();
                            Toast.makeText(SettingsActivity.this, R.string.data_of_password_delete, Toast.LENGTH_SHORT).show();
                        }).show();
            } else if (answer != null && pass != null) {
                dialog.setView(answerInput);
                dialog.setTitle(R.string.enter_secret_word)
                        .setNegativeButton(R.string.no, (dialog1, which) ->
                                dialog1.cancel())
                        .setPositiveButton(R.string.yes, (dialog14, which) -> {
                            if (answerInput.getText().toString().equals(answer)) {
                                sPref.edit().clear().apply();
                                PasswordPreference.getInstance(SettingsActivity.this).clearPassword();
                                PasswordDonePreference.getInstance(SettingsActivity.this).clearSettings();
                                Toast.makeText(SettingsActivity.this, R.string.data_of_password_delete, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SettingsActivity.this, R.string.invalid_entered, Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            } else if (pass.equals("")) {
                Toast.makeText(SettingsActivity.this, R.string.add_password, Toast.LENGTH_SHORT).show();
            }
        });
        clearMinutes_layout.setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this);
            dialog.setTitle(R.string.are_you_sure).setMessage(R.string.clear_minute)
                    .setNegativeButton(R.string.no, (dialog1, which) ->
                            dialog1.cancel())
                    .setPositiveButton(R.string.yes, (dialog13, which) -> {
                        TimingSizePreference.getInstance(SettingsActivity.this).clearSettings();
                        Toast.makeText(SettingsActivity.this, R.string.data_about_minutes_clear, Toast.LENGTH_SHORT).show();
                    }).show();
        });

        language_tv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View view) {
                languageAlert();
            }
        });

        reviews.setOnClickListener(view -> {
            Intent mailIntent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.parse("mailto:?subject=" + getString(R.string.review_on_app) + "&body=" + getString(R.string.hello) + "&to=" + "azamat.nazar99@gmail.com");
            mailIntent.setData(data);
            startActivity(Intent.createChooser(mailIntent, "Send mail..."));
        });
    }

    private void setThemeImage() {
        if (ThemePreference.getInstance(SettingsActivity.this).getTheme()) {
            imageTheme.setImageResource(R.drawable.ic_day);
        } else {
            imageTheme.setImageResource(R.drawable.ic_nights);
        }
    }

    private void initViews() {
        clear_password_layout = findViewById(R.id.first_layout);
        clearMinutes_layout = findViewById(R.id.second_layout);
        theme_layout = findViewById(R.id.third_layout);
        back = findViewById(R.id.back_view);
        imageTheme = findViewById(R.id.image_day_night);
        language_tv = findViewById(R.id.four_layout);
        share_layout = findViewById(R.id.five_layout);
        reviews = findViewById(R.id.six_layout);
        sign_in = findViewById(R.id.seven_layout);
        rate_layout = findViewById(R.id.rate_layout);
    }

    private void checkUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            sign_in.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void languageAlert() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.language_layout, null);

        Dialog alertDialog = new Dialog(this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        RecyclerView recyclerView;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        gridLayoutManager.generateDefaultLayoutParams();
        List<String> languages = List.of(
                "English", "Русский", "Кыргызча", "Português", "한국어",
                "Український", "Deutsche", "हिंदी", "Қазақ тілі","Беларускі","Español","Italiano","Français","Türk","中文","日本語");
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setAdapter(new LanguageAdapter(this, languages, this));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(45));
        alertDialog.show();
    }

    private void setLanguage(int position) {
        switch (position) {
            case 0:
                setLocale("en");
                break;
            case 1:
                setLocale("ru");
                break;
            case 2:
                setLocale("ky");
                break;
            case 3:
                setLocale("pt");
                break;
            case 4:
                setLocale("ko");
                break;
            case 5:
                setLocale("uk");
                break;
            case 6:
                setLocale("de");
                break;
            case 7:
                setLocale("hi");
                break;
            case 8:
                setLocale("kk");
                break;
            case 9:
                setLocale("be");
                break;
            case 10:
                setLocale("es");
                break;
            case 11:
                setLocale("it");
                break;
            case 12:
                setLocale("fr");
                break;
            case 13:
                setLocale("tr");
                break;
            case 14:
                setLocale("zh");
                break;
            case 15:
                setLocale("ja");
                break;
        }
        startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        LanguagePreference.getInstance(SettingsActivity.this).saveLanguage(lang);
    }

    private void loadLocale() {
        String language = LanguagePreference.getInstance(this).getLanguage();
        setLocale(language);
    }

    @Override
    public void onClick(int position) {
        setLanguage(position);
    }
}
