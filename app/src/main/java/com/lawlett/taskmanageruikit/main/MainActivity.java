package com.lawlett.taskmanageruikit.main;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.achievement.AchievementActivity;
import com.lawlett.taskmanageruikit.auth.GoogleSignInActivity;
import com.lawlett.taskmanageruikit.calendarEvents.CalendarEventsFragment;
import com.lawlett.taskmanageruikit.finance.FinanceFragment;
import com.lawlett.taskmanageruikit.habit.fragment.HabitFragment;
import com.lawlett.taskmanageruikit.help.HelpActivity;
import com.lawlett.taskmanageruikit.idea.IdeasFragment;
import com.lawlett.taskmanageruikit.progress.ProgressFragment;
import com.lawlett.taskmanageruikit.service.MessageService;
import com.lawlett.taskmanageruikit.settings.SettingsActivity;
import com.lawlett.taskmanageruikit.tasks.TasksFragment;
import com.lawlett.taskmanageruikit.timing.fragment.TimingFragment;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.Constants;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;
import com.lawlett.taskmanageruikit.utils.preferences.HeaderImagePreference;
import com.lawlett.taskmanageruikit.utils.preferences.HeaderNamePreference;
import com.lawlett.taskmanageruikit.utils.preferences.LanguagePreference;
import com.lawlett.taskmanageruikit.utils.preferences.PasswordPassDonePreference;
import com.lawlett.taskmanageruikit.utils.preferences.TaskDialogPreference;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationItem;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TextView toolbar_title, nav_header_name;
    private ImageView btnGrid, btnHelp, nav_header_profile;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private BottomNavigationView bottomNavigationView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initDrawerNavigation(savedInstanceState);
        initBottomNavigation();
        checkInstance();
        initClickers();
        googleSyncAlert();
    }

    private void googleSyncAlert() {
        if (user == null) {
            SharedPreferences sharedPreferences = getSharedPreferences("googleSync", Context.MODE_PRIVATE);
            boolean isShow = sharedPreferences.getBoolean("isShow", false);
            if (!isShow) {
                sharedPreferences.edit().putBoolean("isShow", true).apply();
                PlannerDialog.showPlannerDialog(this, getString(R.string.planner),getString(R.string.want_save_data_in_google), () -> {
                    startActivity(new Intent(this, GoogleSignInActivity.class));
                });
            }
        }
    }

    private void initDrawerNavigation(Bundle savedInstanceState) {
        Toolbar toolbar = findViewById(R.id.toolbar_b);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        nav_header_profile = hView.findViewById(R.id.nav_header_iv);
        nav_header_name = hView.findViewById(R.id.nav_header_tv);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.settings_text_color));
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        setUserNameAndProfile();
        if (savedInstanceState == null) {
            checkInstance();
        }
    }

    private void setUserNameAndProfile() {
        String imageFromPreference = HeaderImagePreference.getInstance(MainActivity.this).returnImage();
        String nameFromPreference = HeaderNamePreference.getInstance(MainActivity.this).returnName();
        if (!nameFromPreference.isEmpty()) {
            nav_header_name.setText(nameFromPreference);
        } else {
            if (user != null) {
                nav_header_name.setText(user.getDisplayName());
            } else {
                nav_header_name.setText(R.string.you_name);
            }
        }
        if (!imageFromPreference.isEmpty()) {
            Glide.with(MainActivity.this).load(imageFromPreference).placeholder(R.drawable.ic_person).circleCrop().into(nav_header_profile);
        } else {
            if (user != null) {
                Glide.with(MainActivity.this).load(user.getPhotoUrl()).placeholder(R.drawable.ic_person).circleCrop().into(nav_header_profile);
            } else {
                Glide.with(MainActivity.this).load(R.drawable.ic_person).into(nav_header_profile);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            PlannerDialog.showPlannerDialog(this, getString(R.string.planner),getString(R.string.are_you_sure), this::finishAffinity);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_finance:
                changeFragment(new FinanceFragment());
                toolbar_title.setText(R.string.finance);
                btnGrid.setVisibility(View.GONE);
                btnHelp.setVisibility(View.GONE);
                bottomNavigationView.setVisibility(View.GONE);
                break;
            case R.id.nav_home:
                changeFragment(new ProgressFragment());
                toolbar_title.setText(R.string.progress);
                btnGrid.setVisibility(View.GONE);
                btnHelp.setVisibility(View.VISIBLE);
                bottomNavigationView.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.nav_achievements:
                startActivity(new Intent(MainActivity.this, AchievementActivity.class));
                break;
            case R.id.nav_timing:
                changeFragment(new TimingFragment());
                toolbar_title.setText(R.string.timing);
                btnGrid.setVisibility(View.GONE);
                btnHelp.setVisibility(View.GONE);
                bottomNavigationView.setVisibility(View.GONE);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private void initClickers() {
        btnHelp.setOnClickListener(v -> {
            TaskDialogPreference.saveShown();
            startActivity(new Intent(MainActivity.this, HelpActivity.class));
            finish();
        });
        nav_header_profile.setOnClickListener(view -> openGallery());
        nav_header_name.setOnClickListener(view -> showCreateNameDialog());
    }

    private void openGallery() {
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
        startActivityForResult(chooserIntent, Constants.YOUR_SELECT_PICTURE_REQUEST_CODE);
    }

    private void showCreateNameDialog() {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.create_user_name, null);

        Dialog alertDialog = new Dialog(MainActivity.this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextInputLayout textInputLayout = alertDialog.findViewById(R.id.editText_wrapper);
        EditText editText = alertDialog.findViewById(R.id.editText_create_name);
        textInputLayout.setHint(R.string.you_name);
        if(!nav_header_name.getText().toString().equals(getString(R.string.you_name))){
            editText.setText(nav_header_name.getText().toString());
        }
        alertDialog.findViewById(R.id.apply_btn).setOnClickListener(v -> {
            if (editText.getText().toString().isEmpty()) {
                App.showToast(MainActivity.this, getString(R.string.empty));
            } else {
                String name = editText.getText().toString();
                nav_header_name.setText(name);
                HeaderNamePreference.getInstance(MainActivity.this).saveName(name);
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.YOUR_SELECT_PICTURE_REQUEST_CODE) {
                Uri selectedImageUri = data == null ? null : data.getData();
                Glide.with(MainActivity.this).load(selectedImageUri).placeholder(R.drawable.ic_person).circleCrop().into(nav_header_profile);
                HeaderImagePreference.getInstance(MainActivity.this).saveImage(selectedImageUri.toString());
            }
        }
    }

    private void initViews() {
        toolbar_title = findViewById(R.id.toolbar_title);
        btnGrid = findViewById(R.id.tool_btn_grid);
        btnHelp = findViewById(R.id.tool_btn_help);
    }

    private void checkInstance() {
        if (getIntent().getStringExtra("setting") == null) {
            changeFragment(new ProgressFragment());
        }
        if (getIntent().getStringExtra("help") != null) {
            changeFragment(new TasksFragment());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNotification();
    }

    private void setNotification() {
        Intent i = new Intent(getBaseContext(), MessageService.class);
        i.putExtra("displayText", "sample text");
        i.putExtra(MessageService.TITLE, "Planner");
        i.putExtra(MessageService.TEXT, getString(R.string.new_aim));
        PendingIntent pi = PendingIntent.getBroadcast(this.getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mAlarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.WEEK_OF_MONTH, 1);
        long time = calendar.getTimeInMillis();
        mAlarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pi);
    }

    public void changeFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @SuppressLint("SetTextI18n")
    public void initBottomNavigation() {
        Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        String[] monthName = {getString(R.string.january), getString(R.string.february), getString(R.string.march), getString(R.string.april), getString(R.string.may), getString(R.string.june), getString(R.string.july),
                getString(R.string.august), getString(R.string.september), getString(R.string.october), getString(R.string.november), getString(R.string.december)};

        final String month = monthName[c.get(Calendar.MONTH)];

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        BottomNavigationItem bottomNavigationItem = new BottomNavigationItem
                (getString(R.string.progress), ContextCompat.getColor(this, R.color.navigation_background), R.drawable.ic_progress);
        BottomNavigationItem bottomNavigationItem1 = new BottomNavigationItem
                (getString(R.string.tasks), ContextCompat.getColor(this, R.color.navigation_background), R.drawable.ic_checklist);
        BottomNavigationItem bottomNavigationItem4 = new BottomNavigationItem
                ((getString(R.string.habit)), ContextCompat.getColor(this, R.color.navigation_background), R.drawable.ic_habit);
        BottomNavigationItem bottomNavigationItem2 = new BottomNavigationItem
                (getString(R.string.events), ContextCompat.getColor(this, R.color.navigation_background), R.drawable.ic_date_white);
        BottomNavigationItem bottomNavigationItem3 = new BottomNavigationItem
                (getString(R.string.ideas), ContextCompat.getColor(this, R.color.navigation_background), R.drawable.ic_idea);

        bottomNavigationView.addTab(bottomNavigationItem);
        bottomNavigationView.addTab(bottomNavigationItem1);
        bottomNavigationView.addTab(bottomNavigationItem4);
        bottomNavigationView.addTab(bottomNavigationItem2);
        bottomNavigationView.addTab(bottomNavigationItem3);

        bottomNavigationView.setOnBottomNavigationItemClickListener(index -> {
            switch (index) {
                case 0:
                    changeFragment(new ProgressFragment());
                    toolbar_title.setText(R.string.progress);
                    btnGrid.setVisibility(View.GONE);
                    btnHelp.setVisibility(View.GONE);
                    break;
                case 1:
                    changeFragment(new TasksFragment());
                    toolbar_title.setText(R.string.tasks);
                    btnGrid.setVisibility(View.GONE);
                    btnHelp.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    changeFragment(new HabitFragment());
                    toolbar_title.setText(R.string.habit);
                    btnGrid.setVisibility(View.GONE);
                    btnHelp.setVisibility(View.GONE);
                    break;
                case 3:
                    changeFragment(new CalendarEventsFragment());
                    toolbar_title.setText(month + " " + year);
                    btnGrid.setVisibility(View.GONE);
                    btnHelp.setVisibility(View.GONE);
                    break;
                case 4:
                    changeFragment(new IdeasFragment());
                    toolbar_title.setText(R.string.ideas);
                    btnGrid.setVisibility(View.VISIBLE);
                    btnHelp.setVisibility(View.GONE);
                    break;
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PasswordPassDonePreference.getInstance(MainActivity.this).clearSettings();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        LanguagePreference.getInstance(MainActivity.this).saveLanguage(lang);
    }

    private void loadLocale() {
        String language = LanguagePreference.getInstance(this).getLanguage();
        setLocale(language);
    }

}
