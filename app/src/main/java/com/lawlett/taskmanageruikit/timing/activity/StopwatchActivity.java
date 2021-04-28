package com.lawlett.taskmanageruikit.timing.activity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.timing.model.TimingModel;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.Constants;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.preferences.TimingSizePreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.lawlett.taskmanageruikit.utils.App.CHANNEL_ID;

public class StopwatchActivity extends AppCompatActivity {
    Button btnstart, btnstop, applyClick;
    ImageView icanchor, phoneImage;
    Animation roundingalone, atg, btgone, btgtwo;
    Chronometer timerHere;
    EditText taskEdit;
    long elapsedMillis;
    TimingModel timingModel;
    String myTask;
    ConstraintLayout imageConstrain, stopwatchConstraint;
    String stopwatchTime;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String collectionName;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private NotificationManagerCompat notificationManager;

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.timing_color));
        initViews();
        initAnimations();
        initTypeses();
        initClickers();

    }

    private void initClickers() {
        applyClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myTask = taskEdit.getText().toString();
                imageConstrain.setVisibility(View.GONE);
                stopwatchConstraint.setVisibility(View.VISIBLE);
            }
        });

        btnstart.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                showCustomNotification();
                playAnimation();
            }
            private void playAnimation() {
                icanchor.startAnimation(roundingalone);
                btnstop.animate().alpha(1).translationY(-80).setDuration(300).start();
                btnstart.animate().alpha(0).setDuration(300).start();
                btnstop.setVisibility(View.VISIBLE);
                btnstart.setVisibility(View.GONE);
                timerHere.setBase(SystemClock.elapsedRealtime());
                timerHere.start();
            }
        });

        btnstop.setOnClickListener(v -> {
            showElapsedTime();
            dataRoom();
            notificationManager.cancel(1);
        });
    }

    private void initTypeses() {
        Typeface MMedium = Typeface.createFromAsset(getAssets(), "MMedium.ttf");
        Typeface MLight = Typeface.createFromAsset(getAssets(), "MLight.ttf");
        Typeface MRegular = Typeface.createFromAsset(getAssets(), "MRegular.ttf");
        btnstart.setTypeface(MMedium);
        applyClick.setTypeface(MLight);
        taskEdit.setTypeface(MRegular);
    }
    private void initAnimations() {
        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        btgone = AnimationUtils.loadAnimation(this, R.anim.btgone);
        btgtwo = AnimationUtils.loadAnimation(this, R.anim.btgtwo);
        phoneImage.startAnimation(atg);
        btnstart.startAnimation(btgone);
        applyClick.startAnimation(btgtwo);
        taskEdit.startAnimation(btgone);
        btnstop.setAlpha(0);
        roundingalone = AnimationUtils.loadAnimation(this, R.anim.roundingalone);
    }

    private void initViews() {
        notificationManager = NotificationManagerCompat.from(this);
        phoneImage = findViewById(R.id.image_phone);
        btnstart = findViewById(R.id.btnstart);
        applyClick = findViewById(R.id.stopwatch_task_apply);
        btnstop = findViewById(R.id.btnstop);
        icanchor = findViewById(R.id.icanchor);
        taskEdit = findViewById(R.id.stopwatch_task_edit);
        timerHere = findViewById(R.id.timerHere);
        imageConstrain = findViewById(R.id.imageconst);
        stopwatchConstraint = findViewById(R.id.stopWatchConst);
    }

    private void showElapsedTime() {
        elapsedMillis = SystemClock.elapsedRealtime() - timerHere.getBase();
        stopwatchTime = String.valueOf(elapsedMillis / 60000);
    }

    public void dataRoom() {
        Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        String[] monthName = {getString(R.string.january), getString(R.string.february), getString(R.string.march), getString(R.string.april), getString(R.string.may), getString(R.string.june), getString(R.string.july),
                getString(R.string.august), getString(R.string.september), getString(R.string.october), getString(R.string.november), getString(R.string.december)};
        final String month = monthName[c.get(Calendar.MONTH)];
        String currentDate = new SimpleDateFormat("dd ", Locale.getDefault()).format(new Date());
        int stopwatchTimePref = Integer.parseInt(stopwatchTime);
        int previousTimePref = TimingSizePreference.getInstance(this).getTimingSize();
        TimingSizePreference.getInstance(this).saveTimingSize(stopwatchTimePref + previousTimePref);
        timingModel = new TimingModel(null, null, null, myTask, Integer.valueOf(stopwatchTime), currentDate + " " + month + " " + year);
        if (user!=null){
            FireStoreTools.writeOrUpdateDataByFireStore(myTask, Constants.TIMING_COLLECTION,db,timingModel);
        }
        App.getDataBase().timingDao().insert(timingModel);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showCustomNotification() {
        RemoteViews expandedView = new RemoteViews(getPackageName(),
                R.layout.notification_expanded_stopwatch);
        expandedView.setChronometer(R.id.timerHere_expanded, SystemClock.elapsedRealtime(), null, true);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.app_foreground)
                .setCustomBigContentView(expandedView)
                .setContentTitle(getString(R.string.stopwatch))
                .setContentText(getString(R.string.go_count))
                .setColor(getColor(R.color.myWhite))
                .build();
        notificationManager.notify(1, notification);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showElapsedTime();
        notificationManager.cancel(1);
    }
}
