package com.lawlett.taskmanageruikit.progress;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.calendarEvents.CalendarEventsFragment;
import com.lawlett.taskmanageruikit.idea.IdeasFragment;
import com.lawlett.taskmanageruikit.tasks.TasksFragment;
import com.lawlett.taskmanageruikit.timing.model.TimingModel;
import com.lawlett.taskmanageruikit.utils.preferences.AddDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.preferences.HomeDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.preferences.MeetDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.preferences.PersonDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.preferences.PrivateDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.preferences.TaskDialogPreference;
import com.lawlett.taskmanageruikit.utils.preferences.ThemePreference;
import com.lawlett.taskmanageruikit.utils.preferences.TimingSizePreference;
import com.lawlett.taskmanageruikit.utils.preferences.WorkDoneSizePreference;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProgressFragment extends Fragment {
    private ImageView btnChange;
    private TextView toolbar_title;
    private TextView plans_amount, todo_amount, event_amount, allTask_amount,
            complete_task_amount, todo_percent, personalPercent, workPercent,
            meetPercent, homePercent, privatePercent, timing_task_amount, timing_minute_amount,
            addPercent, addTitle, homeTitle, personTitle, workTitle, meetTitle;
    private ProgressBar allTaskProgress, personalProgress, workProgress, meetProgress,
            homeProgress, privateProgress, addProgress;
    private int personalDoneAmount, workDoneAmount, meetDoneAmount, homeDoneAmount, privateDoneAmount,
            plansAmount, doneAmount, personalAmount, workAmount, meetAmount, homeAmount, privateAmount,
            todoAmount, eventAmount, allTaskAmount, addDoneAmount, addAmount;
    private int todoPercent, personalPercentAmount, workPercentAmount, meetPercentAmount, homePercentAmount,
            privatePercentAmount, timingTaskAmountInt, timingMinuteAmountInt, addPercentAmount;
    private List<TimingModel> list;
    private LinearLayout firstCon, secondCon, thirdCon;
    private BottomNavigationView bottomNavigationView;
    Calendar c = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initClickers();
        initRoom();
        getDataFromBD();
        getAllTasks();
        getTasksDoneAmount();
        countUpPercent();
        setShow();
        checkNewCategory();

    }

    @SuppressLint("SetTextI18n")
    private void initClickers() {
        final int year = c.get(Calendar.YEAR);
        String[] monthName = {getString(R.string.january), getString(R.string.february),
                getString(R.string.march), getString(R.string.april), getString(R.string.may),
                getString(R.string.june), getString(R.string.july),
                getString(R.string.august), getString(R.string.september), getString(R.string.october),
                getString(R.string.november), getString(R.string.december)};
        final String month = monthName[c.get(Calendar.MONTH)];

        firstCon.setOnClickListener(v -> {
            changeFragment(new CalendarEventsFragment());
            btnChange.setVisibility(View.GONE);
            toolbar_title.setText(month + " " + year);
            bottomNavigationView.selectTab(3);
        });

        secondCon.setOnClickListener(v -> {
            changeFragment(new TasksFragment());
            btnChange.setVisibility(View.GONE);
            toolbar_title.setText(R.string.tasks);
            bottomNavigationView.selectTab(1);
        });
        thirdCon.setOnClickListener(v -> {
            changeFragment(new IdeasFragment());
            btnChange.setVisibility(View.VISIBLE);
            toolbar_title.setText(R.string.ideas);
            bottomNavigationView.selectTab(4);
        });
    }

    private void initViews(View view) {
        timing_task_amount = view.findViewById(R.id.timing_task_amount);
        timing_minute_amount = view.findViewById(R.id.timing_minute_amount);
        plans_amount = view.findViewById(R.id.plans_amount);
        todo_amount = view.findViewById(R.id.todo_amount);
        event_amount = view.findViewById(R.id.events_amount);
        allTask_amount = view.findViewById(R.id.total_amount);
        complete_task_amount = view.findViewById(R.id.complete_task_amount);
        todo_percent = view.findViewById(R.id.allTask_percent);
        personalPercent = view.findViewById(R.id.personal_percent);
        workPercent = view.findViewById(R.id.work_percent);
        homePercent = view.findViewById(R.id.homeTask_percent);
        meetPercent = view.findViewById(R.id.meetTask_percent);
        privatePercent = view.findViewById(R.id.privateTask_percent);
        allTaskProgress = view.findViewById(R.id.allTask_progress);
        personalProgress = view.findViewById(R.id.personal_progress);
        workProgress = view.findViewById(R.id.work_progress);
        meetProgress = view.findViewById(R.id.meetTask_progress);
        homeProgress = view.findViewById(R.id.homeTask_progress);
        privateProgress = view.findViewById(R.id.private_progress);
        addPercent = view.findViewById(R.id.add_percent);
        addProgress = view.findViewById(R.id.add_progress);
        addTitle = view.findViewById(R.id.add_pr_title);
        homeTitle = view.findViewById(R.id.homeTask_pr_title);
        personTitle = view.findViewById(R.id.personal_pr_title);
        workTitle = view.findViewById(R.id.work_pr_title);
        meetTitle = view.findViewById(R.id.meetTask_pr_title);
        btnChange = view.findViewById(R.id.tool_btn_grid);
        toolbar_title = view.findViewById(R.id.toolbar_title);
        firstCon = view.findViewById(R.id.first_con);
        secondCon = view.findViewById(R.id.second_con);
        thirdCon = view.findViewById(R.id.third_con);
        bottomNavigationView = view.findViewById(R.id.bottomNavigation);
    }

    private void initRoom() {
        list = new ArrayList<>();
        App.getDataBase().timingDao().getAllLive().observe(this, timingModels -> {
            if (timingModels != null)
                list.clear();
            list.addAll(timingModels);
        });
    }

    public void countUpPercent() {
        try {
            todoPercent = doneAmount * 100 / todoAmount;
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
        try {
            personalPercentAmount = personalDoneAmount * 100 / personalAmount;
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
        try {
            meetPercentAmount = meetDoneAmount * 100 / meetAmount;

        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
        try {
            homePercentAmount = homeDoneAmount * 100 / homeAmount;

        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
        try {
            workPercentAmount = workDoneAmount * 100 / workAmount;
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
        try {
            privatePercentAmount = privateDoneAmount * 100 / privateAmount;
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
        try {
            addPercentAmount = addDoneAmount * 100 / addAmount;
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
    }

    public void getTasksDoneAmount() {
        personalDoneAmount = PersonDoneSizePreference.getInstance(getContext()).getPersonalSize();//Персональные выполненные
        workDoneAmount = WorkDoneSizePreference.getInstance(getContext()).getDataSize(); //Работа выполненные
        meetDoneAmount = MeetDoneSizePreference.getInstance(getContext()).getDataSize(); //Встречи выполненные
        homeDoneAmount = HomeDoneSizePreference.getInstance(getContext()).getDataSize(); //Дом выполненные
        addDoneAmount = AddDoneSizePreference.getInstance(getContext()).getDataSize();
        privateDoneAmount = PrivateDoneSizePreference.getInstance(getContext()).getDataSize(); //Приватные выполненные
        doneAmount = personalDoneAmount + workDoneAmount + meetDoneAmount + homeDoneAmount + privateDoneAmount + addDoneAmount;//Выполненные задачи
    }

    public void getDataFromBD() {
        timingTaskAmountInt = App.getDataBase().timingDao().getAll().size();//тайминг кол-во задач
        timingMinuteAmountInt = TimingSizePreference.getInstance(getContext()).getTimingSize();
        plansAmount = App.getDataBase().taskDao().getAll().size(); //идеи кол=во
        personalAmount = App.getDataBase().personalDao().getAll().size();//Персональные задачи кол-во
        workAmount = App.getDataBase().workDao().getAll().size();//Работа задачи кол-во
        meetAmount = App.getDataBase().meetDao().getAll().size();//Встречи задачи кол-во
        homeAmount = App.getDataBase().homeDao().getAll().size();//Дом задачи кол-во
        addAmount = App.getDataBase().doneDao().getAll().size();
        privateAmount = App.getDataBase().privateDao().getAll().size();//Приватные задачи кол-во
        eventAmount = App.getDataBase().dataDao().getAll().size();//События задачи кол-во
    }


    public void getAllTasks() {
        todoAmount = personalAmount + workAmount + meetAmount + homeAmount + privateAmount + addAmount; //Все задачи
        allTaskAmount = todoAmount + eventAmount + plansAmount;//Всего записей
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    public void setShow() {
        timing_task_amount.setText(String.valueOf(timingTaskAmountInt));
        timing_minute_amount.setText(String.valueOf(timingMinuteAmountInt));
        plans_amount.setText(String.valueOf(plansAmount)); //Отображеие кол-во идей
        todo_amount.setText(String.valueOf(todoAmount));  //Отображение кол-во Задач
        event_amount.setText(String.valueOf(eventAmount)); //Отображение кол-во Событий
        complete_task_amount.setText(String.valueOf(doneAmount)); //Отображение кол-во Завершённых Задач
        allTask_amount.setText(String.valueOf(allTaskAmount)); //Отображение кол-во Всех Задач
        todo_percent.setText(todoPercent + "%");
        personalPercent.setText(personalPercentAmount + "%");
        workPercent.setText(workPercentAmount + "%");
        meetPercent.setText(meetPercentAmount + "%");
        homePercent.setText(homePercentAmount + "%");
        addPercent.setText(addPercentAmount + "%");
        privatePercent.setText(privatePercentAmount + "%");
        boolean booleanValue = ThemePreference.getInstance(getContext()).getTheme();
        allTaskProgress.setProgress(doneAmount);
        if (!booleanValue) {
            allTaskProgress.setMax(todoAmount);
            personalProgress.setProgress(personalDoneAmount);
            personalProgress.setMax(personalAmount);
            workProgress.setProgress(workDoneAmount);
            workProgress.setMax(workAmount);
            meetProgress.setProgress(meetDoneAmount);
            meetProgress.setMax(meetAmount);
            homeProgress.setProgress(homeDoneAmount);
            homeProgress.setMax(homeAmount);
            addProgress.setProgress(addDoneAmount);
            addProgress.setMax(addAmount);
            privateProgress.setProgress(privateDoneAmount);
            privateProgress.setMax(privateAmount);
        } else {
            personalProgress.setProgress(personalDoneAmount);
            workProgress.setProgress(workDoneAmount);
            meetProgress.setProgress(meetDoneAmount);
            homeProgress.setProgress(homeDoneAmount);
            addProgress.setProgress(addDoneAmount);
            privateProgress.setProgress(privateDoneAmount);
            allTaskProgress.setMax(todoAmount);
            personalProgress.setMax(personalAmount);
            workProgress.setMax(workAmount);
            meetProgress.setMax(meetAmount);
            homeProgress.setMax(homeAmount);
            addProgress.setMax(addAmount);
            privateProgress.setMax(privateAmount);
            allTaskProgress.getProgressDrawable().setColorFilter(Color.parseColor("#0365C4"), PorterDuff.Mode.SRC_IN);
            personalProgress.getProgressDrawable().setColorFilter(Color.parseColor("#0365C4"), PorterDuff.Mode.SRC_IN);
            workProgress.getProgressDrawable().setColorFilter(Color.parseColor("#0365C4"), PorterDuff.Mode.SRC_IN);
            meetProgress.getProgressDrawable().setColorFilter(Color.parseColor("#0365C4"), PorterDuff.Mode.SRC_IN);
            homeProgress.getProgressDrawable().setColorFilter(Color.parseColor("#0365C4"), PorterDuff.Mode.SRC_IN);
            addProgress.getProgressDrawable().setColorFilter(Color.parseColor("#0365C4"), PorterDuff.Mode.SRC_IN);
            privateProgress.getProgressDrawable().setColorFilter(Color.parseColor("#0365C4"), PorterDuff.Mode.SRC_IN);
        }
    }

    public void changeFragment(Fragment fragment) {
        FragmentManager manager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public void checkNewCategory() {
        TaskDialogPreference.init(getContext());
        if (!TaskDialogPreference.getTitle().isEmpty()) {
            addTitle.setVisibility(View.VISIBLE);
            addProgress.setVisibility(View.VISIBLE);
            addPercent.setVisibility(View.VISIBLE);
            addTitle.setText(TaskDialogPreference.getTitle());
        } else {
            addTitle.setVisibility(View.GONE);
            addProgress.setVisibility(View.GONE);
            addPercent.setVisibility(View.GONE);
        }
        if (!TaskDialogPreference.getHomeTitle().isEmpty()) {
            homeTitle.setText(TaskDialogPreference.getHomeTitle());
        }
        if (!TaskDialogPreference.getPersonTitle().isEmpty()) {
            personTitle.setText(TaskDialogPreference.getPersonTitle());
        }
        if (!TaskDialogPreference.getWorkTitle().isEmpty()) {
            workTitle.setText(TaskDialogPreference.getWorkTitle());
        }
        if (!TaskDialogPreference.getMeetTitle().isEmpty()) {
            meetTitle.setText(TaskDialogPreference.getMeetTitle());
        }
    }
}
