package com.lawlett.taskmanageruikit.habit.fragment;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.habit.adapter.HabitAdapter;
import com.lawlett.taskmanageruikit.room.HabitModel;
import com.lawlett.taskmanageruikit.service.MessageService;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.Constants;
import com.lawlett.taskmanageruikit.utils.CustomHabitDialog;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HabitFragment extends Fragment implements HabitAdapter.IMClickListener {
    private RecyclerView recyclerViewHabit;
    private List<HabitModel> list;
    private HabitModel habitModel;
    private FloatingActionButton floatingActionButton;
    private TextView helpText;
    private HabitAdapter habitAdapter;
    private FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
    private String collectionName;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_habit, container, false);
        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (user!=null){
            collectionName = "Привычки" + "-" + "(" + user.getDisplayName() + ")" + user.getUid();
        }
        initViews(view);
        initListeners();
        initAdapter();
        getRoomRecordsData();
    }

    private void RequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(requireContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + requireActivity().getPackageName()));
                startActivityForResult(intent, 1);
            }
        }
    }

    private void setNotification(long time, HabitModel habitModel) {
        Intent i = new Intent(requireContext(), MessageService.class);
        i.putExtra("displayText", "sample text");
        i.putExtra(MessageService.TITLE, "Planner");
        i.putExtra(MessageService.TEXT, getString(R.string.you_forgot_habbit)+" "+habitModel.getTitle() + " ?");
        PendingIntent pi = PendingIntent.getBroadcast(requireContext(), (int) habitModel.getId(), i, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mAlarm = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        mAlarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pi);
        App.showToast(requireContext(),getString(R.string.set_notification_on)+" " + habitModel.getTitle());
    }

    private void initAdapter() {
        habitAdapter = new HabitAdapter(list, getContext(), this);
        recyclerViewHabit.setAdapter(habitAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getRoomRecordsData() {
        App.getDataBase().habitDao().getAllLive().observe(this, habitModels -> {
            if (habitModels != null) {
                list.clear();
                list.addAll(habitModels);
                helpText.setVisibility(View.GONE);
                Collections.sort(list, (habitModel, t1) -> Boolean.compare(checkOnComplete(habitModel), checkOnComplete(t1)));
                habitAdapter.notifyDataSetChanged();
            }
            if (habitModels.isEmpty()) {
                helpText.setVisibility(View.VISIBLE);
            }
            if (list.size() != 0) {
                writeAllTaskFromRoomToFireStore();
            } else {
                readDataFromFireStore();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void readDataFromFireStore() {
        if (user != null) {
            String title = "title";
            String image = "image";
            String allDays = "allDays";
            String currentDay = "currentDay";
            String myDay = "myDay";

            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Map<String, Object> dataFromFireBase;
                                dataFromFireBase = document.getData();

                                String titleFB = (String) dataFromFireBase.get(title);
                                Long imageFB = (Long) dataFromFireBase.get(image);
                                String allDaysFB = (String) dataFromFireBase.get(allDays);
                                Long currentDayFB = (Long) dataFromFireBase.get(currentDay);
                                Long myDayFB = (Long) dataFromFireBase.get(myDay);

                                int imageInt = imageFB == null ? null : Math.toIntExact(imageFB);
                                int currentDayInt = currentDayFB == null ? null : Math.toIntExact(currentDayFB);
                                int myDayInt = myDayFB == null ? null : Math.toIntExact(myDayFB);

                                HabitModel habitModel = new HabitModel(titleFB, imageInt, allDaysFB, currentDayInt, myDayInt);
                                App.getDataBase().habitDao().insert(habitModel);
                            }
                        }
                    });
        }
    }

    private void writeAllTaskFromRoomToFireStore() {
        if (user != null) {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("habitPreferences", Context.MODE_PRIVATE);
            Calendar calendar = Calendar.getInstance();
            String currentDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String dayFromPreference = sharedPreferences.getString(Constants.CURRENT_DAY, "");
            if (!currentDay.equals(dayFromPreference)) {
                for (int i = 0; i < list.size(); i++) {
                    FireStoreTools.writeOrUpdateDataByFireStore(list.get(i).getTitle(), collectionName, db, list.get(i));
                }
                sharedPreferences.edit().clear().apply();
                sharedPreferences.edit().putString("currentDay", currentDay).apply();
            }
        }
    }

    public boolean checkOnComplete(HabitModel habitModel) {
        if (Integer.parseInt(habitModel.getAllDays()) == habitModel.getCurrentDay()) {
            return true;
        }
        return false;
    }

    @SuppressLint("ResourceAsColor")
    private void initViews(View view) {
        list = new ArrayList<>();
        helpText = view.findViewById(R.id.habit_tv);
        floatingActionButton = view.findViewById(R.id.add_habit_btn);
        floatingActionButton.setColorFilter(Color.WHITE);
        floatingActionButton.setBackgroundColor(R.color.plus_background);
        recyclerViewHabit = view.findViewById(R.id.habit_recycler);
    }

    private void initListeners() {
        floatingActionButton.setOnClickListener(view -> showCustomDialog());

    }

    private void showCustomDialog() {
        CustomHabitDialog customHabitDialog = new CustomHabitDialog(requireContext(), null);
        customHabitDialog.setDialogResult((title, amount, image) -> {
            habitModel = new HabitModel(title, image, amount, 0, -1);
            App.getDataBase().habitDao().insert(habitModel);
            if (user!=null){
                FireStoreTools.writeOrUpdateDataByFireStore(habitModel.getTitle(),collectionName,db,habitModel);
            }
        });
        habitAdapter.notifyDataSetChanged();
        customHabitDialog.show();
    }

    @Override
    public void onItemClick(HabitModel habitModel) {
        DialogHelper dialogHelper = new DialogHelper();
        dialogHelper.myDialog(getContext(), () -> {
            checkDay(habitModel);
            habitAdapter.notifyDataSetChanged();
        }, getString(R.string.you_is_done_habit_today), "");


    }

    @Override
    public void onItemLongClick(HabitModel habitModel) {
        PlannerDialog.showPlannerDialog(requireActivity(), getString(R.string.task_dialog_message), () -> {
            App.getDataBase().habitDao().delete(habitModel);
            if (user!=null){
                FireStoreTools.deleteDataByFireStore(habitModel.getTitle(),collectionName,db,null);
            }
            habitAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onMenuItemClick(HabitModel habitModel, TextView textView) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), textView);
        popupMenu.inflate(R.menu.habit_menu);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.habit_menu_delete:
                    PlannerDialog.showPlannerDialog(requireActivity(), getString(R.string.task_dialog_message), new PlannerDialog.PlannerDialogClick() {
                        @Override
                        public void clickOnYes() {
                            App.getDataBase().habitDao().delete(habitModel);
                            if (user!=null){
                                FireStoreTools.deleteDataByFireStore(habitModel.getTitle(),collectionName,db,null);
                            }
                        }
                    });
                    return true;
                case R.id.habit_menu_edit:
                    CustomHabitDialog customHabitDialog = new CustomHabitDialog(requireContext(), habitModel);
                    customHabitDialog.setDialogResult((title, amount, image) -> {
                        habitModel.setTitle(title);
                        habitModel.setAllDays(amount);
                        habitModel.setImage(image);
                        App.getDataBase().habitDao().update(habitModel);
                        if (user!=null){
                            FireStoreTools.writeOrUpdateDataByFireStore(habitModel.getTitle(),collectionName,db,habitModel);
                        }
                    });
                    customHabitDialog.show();
                    return true;
                case R.id.habit_change_day:
                    showUpdateDaysDialog(habitModel);
                    return true;
                case R.id.habit_notification:
                    RequestPermission();
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(requireActivity(), (timePicker, selectedHour, selectedMinute) -> {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), selectedHour, selectedMinute);
                        long time = calendar.getTimeInMillis();
                        setNotification(time, habitModel);
                    }, hour, minute, true);
                    mTimePicker.setTitle(getString(R.string.select_time));
                    mTimePicker.show();
                    return true;
                default:
                    return false;
            }
        });
        habitAdapter.notifyDataSetChanged();
        popupMenu.show();
    }


    private void checkDay(HabitModel habitModel) {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int dayFromRoom = habitModel.getMyDay();
        if (currentDay != dayFromRoom) {
            habitModel.setMyDay(currentDay);
            habitModel.setCurrentDay(habitModel.getCurrentDay() + 1);
            App.getDataBase().habitDao().update(habitModel);
            if (user!=null){
                FireStoreTools.writeOrUpdateDataByFireStore(habitModel.getTitle(),collectionName,db,habitModel);
            }
        } else {
            Toast.makeText(requireContext(), R.string.your_habit_is_done, Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateDaysDialog(HabitModel habitModel) {
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.create_user_name, null);
        Dialog alertDialog = new Dialog(requireActivity());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextInputLayout textInputLayout = alertDialog.findViewById(R.id.editText_wrapper);
        EditText editText = alertDialog.findViewById(R.id.editText_create_name);
        textInputLayout.setHint(R.string.days_amount);
        editText.setText(habitModel.getAllDays());
        alertDialog.findViewById(R.id.apply_btn).setOnClickListener(v -> {
            if (editText.getText().toString().isEmpty()) {
                App.showToast(requireContext(), getString(R.string.empty));
            } else {
                String name = editText.getText().toString();
                this.habitModel = habitModel;
                this.habitModel.setAllDays(name);
                App.getDataBase().habitDao().update(habitModel);
                if (user!=null){
                    FireStoreTools.writeOrUpdateDataByFireStore(habitModel.getTitle(),collectionName,db,habitModel);
                }
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }
}