package com.lawlett.taskmanageruikit.settings;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.achievement.AchievementActivity;
import com.lawlett.taskmanageruikit.auth.GoogleSignInActivity;
import com.lawlett.taskmanageruikit.splash.SplashActivity;
import com.lawlett.taskmanageruikit.utils.LanguagePreference;
import com.lawlett.taskmanageruikit.utils.PassCodeActivity;
import com.lawlett.taskmanageruikit.utils.PasswordDonePreference;
import com.lawlett.taskmanageruikit.utils.PasswordPreference;
import com.lawlett.taskmanageruikit.utils.ThemePreference;
import com.lawlett.taskmanageruikit.utils.TimingSizePreference;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout language_tv, clear_password_layout, clearMinutes_layout, share_layout, achievement_layout, reviews,sign_in;
    ImageView magick;
    ListView listView;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    ImageView back, imageSettings, imageTheme;
    ConstraintLayout theme_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_settings);

        clear_password_layout = findViewById(R.id.first_layout);
        clearMinutes_layout = findViewById(R.id.second_layout);
        theme_layout = findViewById(R.id.third_layout);
        back = findViewById(R.id.back_view);
        imageTheme = findViewById(R.id.image_day_night);
        language_tv = findViewById(R.id.four_layout);
        share_layout = findViewById(R.id.five_layout);
        imageSettings = findViewById(R.id.image_settings);
        reviews = findViewById(R.id.six_layout);
        sign_in=findViewById(R.id.seven_layout);

        achievement_layout = findViewById(R.id.achievement_layout);
        magick = findViewById(R.id.btn_magick);
        listView = findViewById(R.id.listView);


        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(getResources().getColor(R.color.statusBarC));

        if (ThemePreference.getInstance(SettingsActivity.this).isTheme()) {
            imageTheme.setImageResource(R.drawable.ic_day);
        } else {
            imageTheme.setImageResource(R.drawable.ic_nights);
        }

        theme_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ThemePreference.getInstance(SettingsActivity.this).isTheme()) {
                    ThemePreference.getInstance(SettingsActivity.this).saveThemeTrue();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    ThemePreference.getInstance(SettingsActivity.this).saveThemeFalse();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, GoogleSignInActivity.class));
            }
        });

        magick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        share_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Planner");
                    String shareMessage = "\nPlanner\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=com.lawlett.taskmanageruikit";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_app)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        achievement_layout.setOnClickListener(v -> startActivity(new Intent(this, AchievementActivity.class)));

        clear_password_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sPref = getSharedPreferences("qst", 0);
                String qst = sPref.getString(PassCodeActivity.SAVED_QST, null);
                String answer = sPref.getString(PassCodeActivity.SAVED_ANSWER, null);
                EditText answerInput = new EditText(SettingsActivity.this);
                AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this);
                String pass = PasswordPreference.getInstance(SettingsActivity.this).returnPassword();
                if (pass != "" && answer == null) {
                    dialog.setTitle(R.string.are_you_sure).setMessage(R.string.clear_password)
                            .setNegativeButton(R.string.no, (dialog1, which) ->
                                    dialog1.cancel())
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PasswordPreference.getInstance(SettingsActivity.this).clearPassword();
                                    PasswordDonePreference.getInstance(SettingsActivity.this).clearSettings();
                                    Toast.makeText(SettingsActivity.this, R.string.data_of_password_delete, Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                } else if (answer != null && pass != null) {
                    dialog.setView(answerInput);
                    dialog.setTitle(R.string.enter_secret_word)
                            .setNegativeButton(R.string.no, (dialog1, which) ->
                                    dialog1.cancel())
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (answerInput.getText().toString().equals(answer)) {
                                        sPref.edit().clear().apply();
                                        PasswordPreference.getInstance(SettingsActivity.this).clearPassword();
                                        PasswordDonePreference.getInstance(SettingsActivity.this).clearSettings();
                                        Toast.makeText(SettingsActivity.this, R.string.data_of_password_delete, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SettingsActivity.this, R.string.invalid_entered, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).show();
                } else if (pass == "") {
                    Toast.makeText(SettingsActivity.this, R.string.add_password, Toast.LENGTH_SHORT).show();
                }
            }
        });
        clearMinutes_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this);
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.clear_minute)
                        .setNegativeButton(R.string.no, (dialog1, which) ->
                                dialog1.cancel())
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TimingSizePreference.getInstance(SettingsActivity.this).clearSettings();
                                Toast.makeText(SettingsActivity.this, R.string.data_about_minutes_clear, Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });

        language_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });

        reviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mailIntent = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:?subject=" + getString(R.string.review_on_app) + "&body=" + getString(R.string.hello) + "&to=" + "azamat.nazar99@gmail.com");
                mailIntent.setData(data);
                startActivity(Intent.createChooser(mailIntent, "Send mail..."));
            }
        });

    }

    private void showChangeLanguageDialog() {
        final String[] listItems = {"English", "Русский", "Кыргызча", "Português", "한국어", "Український"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SettingsActivity.this);
        mBuilder.setTitle(R.string.choose_language);
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (i == 0) {
                    setLocale("en");
                    startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
                } else if (i == 1) {
                    setLocale("ru");
                    startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
                } else if (i == 2) {
                    setLocale("ky");
                    startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
                } else if (i == 3) {
                    setLocale("pt");
                    startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
                } else if (i == 4) {
                    setLocale("ko");
                    startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
                } else if (i == 5) {
                    setLocale("uk");
                    startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
                }
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        LanguagePreference.getInstance(SettingsActivity.this).saveLanguage(lang);
    }


    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speech recognition demo");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {

            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            listView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, matches));

            if (matches.contains(getString(R.string.patronum))) {
                Random random = new Random();
                String animals[] = {getString(R.string.fox), getString(R.string.deer), getString(R.string.bull), getString(R.string.dog), getString(R.string.cat), getString(R.string.rat), getString(R.string.crane), getString(R.string.hippo), getString(R.string.giraffe), getString(R.string.lion), getString(R.string.zebra)};
                int a = random.nextInt(animals.length);
                Toast.makeText(this, getString(R.string.your_patronus) + animals[a], Toast.LENGTH_SHORT).show();
            }

            if (matches.contains(getString(R.string.lumos))) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                        try {
                            cameraManager.setTorchMode("0", true);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(this, "FailureCamera", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show();
            }

            if (matches.contains("Nox")) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                        try {
                            cameraManager.setTorchMode("0", false);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadLocale() {
        String language = LanguagePreference.getInstance(this).getLanguage();
        setLocale(language);
    }
}
