package com.lawlett.taskmanageruikit.timing.activity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.timing.model.TimingModel;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.TimingSizePreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.lawlett.taskmanageruikit.utils.App.CHANNEL_ID;

public class TimerActivity extends AppCompatActivity {
    private TextView countdownText;
    private Button countdownButton, exitButton, timerTaskApply, applyDone;
    MediaPlayer mp;
    ImageView icanchor, xButton, phoneImage;
    EditText timerTaskEdit;
    String timeLeftText;
    Animation roundingalone, atg, btgone, btgtwo;
    TimingModel timingModel;
    String myTask;
    EditText editText;
    ConstraintLayout imageConst, timerConst;
    private Integer timeLeftInMilliseconds = 0;//600.000  10min ||1000//1 second
    CountDownTimer countDownTimer;
    private NotificationManagerCompat notificationManager;

    @SuppressLint({"ResourceAsColor", "Range"})
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mp = MediaPlayer.create(getApplicationContext(), notification);

        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(R.color.timing_color);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.timing_color));

        notificationManager = NotificationManagerCompat.from(this);


        phoneImage = findViewById(R.id.image_timerPhone);
        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        btgone = AnimationUtils.loadAnimation(this, R.anim.btgone);
        btgtwo = AnimationUtils.loadAnimation(this, R.anim.btgtwo);
        xButton = findViewById(R.id.close_button);
        timerTaskEdit = findViewById(R.id.timer_task_edit);
        editText = findViewById(R.id.editText);
        timerTaskApply = findViewById(R.id.timer_task_apply);
        applyDone = findViewById(R.id.apply_button);
        icanchor = findViewById(R.id.icanchor);
        countdownText = findViewById(R.id.countdown_text);
        countdownButton = findViewById(R.id.countdown_button);
        exitButton = findViewById(R.id.exit_button);
        roundingalone = AnimationUtils.loadAnimation(this, R.anim.roundingalone);
        imageConst = findViewById(R.id.image_const);
        timerConst = findViewById(R.id.timerConst);

        phoneImage.startAnimation(atg);
        countdownButton.startAnimation(btgone);
        timerTaskApply.startAnimation(btgtwo);
        timerTaskEdit.startAnimation(btgone);

        Typeface medium = Typeface.createFromAsset(getAssets(), "MMedium.ttf");
        Typeface mLight = Typeface.createFromAsset(getAssets(), "MLight.ttf");
        Typeface mRegular = Typeface.createFromAsset(getAssets(), "MRegular.ttf");

        countdownButton.setTypeface(medium);
        timerTaskApply.setTypeface(mLight);//try
        timerTaskEdit.setTypeface(mRegular);


        xButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.reset();
                notificationManager.cancel(1);
                if (countDownTimer!=null)
                countDownTimer.cancel();
                finish();
                //Крестик
            }
        });
        timerTaskApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerTaskEdit.getText().toString().isEmpty()) {
                    Toast.makeText(TimerActivity.this, R.string.empty, Toast.LENGTH_SHORT).show();

                } else {
                    myTask = timerTaskEdit.getText().toString();
                    imageConst.setVisibility(View.GONE);
                    timerConst.setVisibility(View.VISIBLE);
                }
            }
        });

        applyDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().equals("") || Integer.parseInt(editText.getText().toString()) < 1) {
                    Toast.makeText(TimerActivity.this, R.string.zero_minutes_pass, Toast.LENGTH_SHORT).show();
                } else {
                    timeLeftInMilliseconds = Integer.parseInt(editText.getText().toString()) * 60000;
                    timeLeftText = timeLeftInMilliseconds.toString();
                    applyDone.setVisibility(View.GONE);
                    editText.setVisibility(View.GONE);
                    countdownText.setText(R.string.ready);
                    countdownButton.setVisibility(View.VISIBLE);
                    countdownText.setVisibility(View.VISIBLE);
                }
            }
        });
        countdownButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                startTimer();
                showNotification();
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myTime = countdownText.getText().toString();
                if (myTime.equals("0:00") || myTime.equals("0:01") || myTime.equals("0:02")) {
                    dataRoom();
                    if (mp != null)
                        mp.stop();
                    countDownTimer.cancel();
                    finish();
                } else {
                    Toast.makeText(TimerActivity.this, R.string.timer_dont_end, Toast.LENGTH_SHORT).show();
                }
                notificationManager.cancel(1);
            }
        });
    }

    private void startTimer() {
        icanchor.startAnimation(roundingalone);
        countdownButton.animate().alpha(0).setDuration(300).start();

        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) millisUntilFinished / 60000;
                int seconds = (int) millisUntilFinished % 60000 / 1000;

                timeLeftText = "" + minutes;
                timeLeftText += ":";
                if (seconds < 10) timeLeftText += "0";
                timeLeftText += seconds;

                countdownText.setText(timeLeftText);
                countdownButton.setVisibility(View.GONE);
                exitButton.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFinish() {
                if (mp != null) {
                    mp.start();
                } else {
                    Toast.makeText(TimerActivity.this, R.string.timer_end, Toast.LENGTH_SHORT).show();
                }
                icanchor.clearAnimation();
                countdownButton.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
    }

    public void dataRoom() {
        Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        String[] monthName = {getString(R.string.january), getString(R.string.february), getString(R.string.march), getString(R.string.april), getString(R.string.may), getString(R.string.june), getString(R.string.july),
                getString(R.string.august), getString(R.string.september), getString(R.string.october), getString(R.string.november), getString(R.string.december)};

        final String month = monthName[c.get(Calendar.MONTH)];
        String currentDate = new SimpleDateFormat("dd ", Locale.getDefault()).format(new Date());
        int previousTime = TimingSizePreference.getInstance(this).getTimingSize();
        int a = Integer.parseInt(editText.getText().toString());
        int timerTime = a;
        TimingSizePreference.getInstance(this).saveTimingSize(timerTime + previousTime);
        timingModel = new TimingModel(myTask, timerTime, currentDate + " " + month + " " + year, null, null, null);
        App.getDataBase().timingDao().insert(timingModel);
        finish();
    }

   @RequiresApi(api = Build.VERSION_CODES.N)
    public void showNotification() {

        RemoteViews expandedView = new RemoteViews(getPackageName(),
                R.layout.notification_expanded_timer);

        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) millisUntilFinished / 60000;
                int seconds = (int) millisUntilFinished % 60000 / 1000;

                timeLeftText = "" + minutes;
                timeLeftText += ":";
                if (seconds < 10) timeLeftText += "0";
                timeLeftText += seconds;

                expandedView.setTextViewText(R.id.timer_expanded, timeLeftText);

                Notification notification = new NotificationCompat.Builder(TimerActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.app_logo_foreground)
                        .setCustomBigContentView(expandedView)
                        .setContentTitle(getString(R.string.timer))
                        .setColor(getColor(R.color.myWhite))
                        .setContentText(getString(R.string.go_count))
                        .setOnlyAlertOnce(true)
                        .build();


                notificationManager.notify(1, notification);
            }

            @Override
            public void onFinish() {
                Toast.makeText(TimerActivity.this, "00:00", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }


}