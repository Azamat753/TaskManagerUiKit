package com.lawlett.taskmanageruikit.calendarEvents;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.calendarEvents.data.model.CalendarTaskModel;
import com.lawlett.taskmanageruikit.service.MessageService;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DatePickerFragment;
import com.lawlett.taskmanageruikit.utils.preferences.LanguagePreference;
import com.lawlett.taskmanageruikit.utils.TimePickerFragment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private TextView startData, startTime, endTime, startDataText, startTimeNumber, endTimeNumber;
    private EditText title_ed;
    private CalendarTaskModel calendarTaskModel;
    private ImageView backView, doneView;
    private String currentDataString;
    private String titleT;
    private int choosedColor;
    private long time;
    private final Calendar baseCalendar = Calendar.getInstance();
    private String startHour, endingHour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_add_event);
        initViews();
        initClickers();
        getIncomingIntent();
        RequestPermission();
    }

    private void initClickers() {
        startDataText.setOnClickListener(v -> {
            DialogFragment dataPicker = new DatePickerFragment();
            dataPicker.show(getSupportFragmentManager(), "data picker");
        });

        startTime.setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "timePicker");
        });

        endTime.setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(AddEventActivity.this, (timePicker, selectedHour, selectedMinute) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(0, 0, 0, selectedHour, selectedMinute);
                endTimeNumber.setText(android.text.format.DateFormat.format("HH:mm", calendar));
                endingHour = String.valueOf(android.text.format.DateFormat.format("HH:mm", calendar));
            }, hour, minute, true);
            mTimePicker.setTitle(getString(R.string.select_time));
            mTimePicker.show();

        });

        doneView.setOnClickListener(v -> {
            titleT = title_ed.getText().toString();
            setNotification();
            recordDataRoom();
        });

        backView.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        baseCalendar.set(Calendar.YEAR, year);
        baseCalendar.set(Calendar.MONTH, month);
        baseCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        currentDataString = DateFormat.getDateInstance().format(c.getTime());
        startData.setText(currentDataString);
    }

    private void RequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, 1);
            }  //Permission Granted-System will work

        }
    }

    private void setNotification() {
        Intent i = new Intent(getBaseContext(), MessageService.class);
        i.putExtra("displayText", "sample text");
        i.putExtra(MessageService.TITLE, "Planner");
        i.putExtra(MessageService.TEXT, titleT);
        List<CalendarTaskModel> listA = App.getDataBase().eventsDao().getAll();
        int idOfP = listA.size();
        PendingIntent pi = PendingIntent.getBroadcast(this.getApplicationContext(), idOfP, i, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mAlarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mAlarm.set(AlarmManager.RTC_WAKEUP, time, pi);
    }

    @SuppressLint({"LogNotTimber", "SetTextI18n"})
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(0, 0, 0, hourOfDay, minute);
        baseCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        baseCalendar.set(Calendar.MINUTE, minute);
        time = baseCalendar.getTimeInMillis();
        startTimeNumber.setText(android.text.format.DateFormat.format("HH:mm", calendar));
        startHour = String.valueOf(android.text.format.DateFormat.format("HH:mm", calendar));
    }

    public void initViews() {
        title_ed = findViewById(R.id.edit_title);
        startData = findViewById(R.id.start_date_number);
        startTime = findViewById(R.id.start_time);
        startTimeNumber = findViewById(R.id.start_time_number);
        endTimeNumber = findViewById(R.id.end_time_number);
        endTime = findViewById(R.id.end_time);
        backView = findViewById(R.id.back_view_event);
        doneView = findViewById(R.id.done_view_event);
        startDataText = findViewById(R.id.start_date_button);

    }

    public void recordDataRoom() {
        String myTitle = title_ed.getText().toString();
        String myStartData = startData.getText().toString();
        String myStartTime = startTimeNumber.getText().toString();
        String myEndTime = endTimeNumber.getText().toString();
        int myColor = choosedColor;
        if (calendarTaskModel != null) {
            calendarTaskModel.setTitle(myTitle);
            calendarTaskModel.setDataTime(myStartData);
            calendarTaskModel.setStartTime(myStartTime);
            calendarTaskModel.setEndTime(myEndTime);
            calendarTaskModel.setChooseColor(myColor);
            App.getDataBase().eventsDao().update(calendarTaskModel);
            finish();
        } else {
            titleT = title_ed.getText().toString();
            if (currentDataString != null && startHour != null && endingHour != null) {
                calendarTaskModel = new CalendarTaskModel(currentDataString, title_ed.getText().toString().trim(),
                        startHour, endingHour, choosedColor);
                App.getDataBase().eventsDao().insert(calendarTaskModel);
                finish();
            } else {
                Toast.makeText(this, R.string.need_all_fields, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getIncomingIntent() {
        Intent intent = getIntent();
        calendarTaskModel = (CalendarTaskModel) intent.getSerializableExtra("calendar");
        if (calendarTaskModel != null) {
            titleT = calendarTaskModel.getTitle();
            title_ed.setText(titleT);
            String getStart = calendarTaskModel.getStartTime();
            startTimeNumber.setText(getStart);
            String getEndTime = calendarTaskModel.getEndTime();
            endTimeNumber.setText(getEndTime);
            choosedColor = calendarTaskModel.getChooseColor();
            String getDataTime = calendarTaskModel.getDataTime();
            startData.setText(getDataTime);
        }
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        LanguagePreference.getInstance(AddEventActivity.this).saveLanguage(lang);
    }

    public void loadLocale() {
        String language = LanguagePreference.getInstance(this).getLanguage();
        setLocale(language);
    }
}

