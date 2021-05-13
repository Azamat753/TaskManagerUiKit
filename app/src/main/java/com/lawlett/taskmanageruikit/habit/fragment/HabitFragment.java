package com.lawlett.taskmanageruikit.habit.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.habit.adapter.HabitAdapter;
import com.lawlett.taskmanageruikit.tasksPage.data.model.HabitModel;
import com.lawlett.taskmanageruikit.utils.ActionForDialogSecond;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.CustomHabitDialog;
import com.lawlett.taskmanageruikit.utils.DialogHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HabitFragment extends Fragment implements HabitAdapter.IMClickListener {
    RecyclerView recyclerViewHabit;
    List<HabitModel> list;
    HabitModel habitModel;
    FloatingActionButton floatingActionButton;
    TextView helpText;
    HabitAdapter habitAdapter;
    private SendHabit sendHabit;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initListeners();
        initAdapter();
        getRoomRecordsData();
    }

    private void initAdapter() {
        habitAdapter = new HabitAdapter(list, getContext(), this);
        recyclerViewHabit.setAdapter(habitAdapter);
    }

    private void getRoomRecordsData() {
        App.getDataBase().habitDao().getAllLive().observe(this, habitModels -> {
            if (habitModels != null) {
                list.clear();
                list.addAll(habitModels);
                helpText.setVisibility(View.GONE);
                Collections.sort(list, new Comparator<HabitModel>() {
                    @Override
                    public int compare(HabitModel habitModel, HabitModel t1) {
                        return Boolean.compare(checkOnComplete(habitModel), checkOnComplete(t1));
                    }
                });
                habitAdapter.notifyDataSetChanged();
            }
            if (habitModels.isEmpty()) {
                helpText.setVisibility(View.VISIBLE);
            }
        });
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
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomDialog();
            }
        });
    }

    private void showCustomDialog() {
        CustomHabitDialog customHabitDialog = new CustomHabitDialog(requireContext(), habitModel);
        customHabitDialog.setDialogResult(new CustomHabitDialog.CustomDialogListener() {
            @Override
            public void addInformation(String title, String amount, Integer image) {
                habitModel = new HabitModel(title, image, amount, 0, -1);
                App.getDataBase().habitDao().insert(habitModel);
            }
        });
        habitAdapter.notifyDataSetChanged();
        customHabitDialog.show();
    }

    @Override
    public void onItemClick(HabitModel habitModel) {
        DialogHelper dialogHelper = new DialogHelper();
        dialogHelper.myDialog(getContext(), new ActionForDialogSecond() {
            @Override
            public void pressOkSecond() {
                checkDay(habitModel);
                habitAdapter.notifyDataSetChanged();
            }

        }, getString(R.string.you_is_done_habit_today), "");


    }

    @Override
    public void onItemLongClick(HabitModel habitModel) {
        DialogHelper dialogHelper = new DialogHelper();
        dialogHelper.myDialog(requireContext(), new ActionForDialogSecond() {
            @Override
            public void pressOkSecond() {
                App.getDataBase().habitDao().delete(habitModel);
                habitAdapter.notifyDataSetChanged();
            }
        }, "Вы действительно хотите удалить?", "");
    }

    @Override
    public void onMenuItemClick(HabitModel habitModel, TextView textView) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), textView);
        popupMenu.inflate(R.menu.habit_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.habit_menu_delete:
                        App.getDataBase().habitDao().delete(habitModel);
                        return true;
                    case R.id.habit_menu_edit:
                        CustomHabitDialog customHabitDialog = new CustomHabitDialog(requireContext(), habitModel);
                        //sendHabit.getHabitModel(habitModel);
                        customHabitDialog.setDialogResult(new CustomHabitDialog.CustomDialogListener() {
                            @Override
                            public void addInformation(String title, String amount, Integer image) {
                                habitModel.setTitle(title);
                                habitModel.setAllDays(amount);
                                habitModel.setImage(image);
                                App.getDataBase().habitDao().update(habitModel);
                            }
                        });
                        customHabitDialog.show();

                        return true;
                    case R.id.habit_change_day:
                        showCreateNameDialog(habitModel);
                        return true;
                    default:
                        return false;
                }
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
        } else {
            Toast.makeText(requireContext(), R.string.your_habit_is_done, Toast.LENGTH_SHORT).show();
        }
    }

    private void showCreateNameDialog(HabitModel habitModel) {
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.create_user_name, null);

        Dialog alertDialog = new Dialog(requireActivity());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        EditText editText = alertDialog.findViewById(R.id.editText_create_name);
        alertDialog.findViewById(R.id.apply_btn).setOnClickListener(v -> {
            if (editText.getText().toString().isEmpty()) {
//                App.showToast(MainActivity.this, getString(R.string.you_name));
            } else {
                String name = editText.getText().toString();
                this.habitModel = habitModel;
                this.habitModel.setAllDays(name);
                App.getDataBase().habitDao().update(habitModel);
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

    public interface SendHabit {
        void getHabitModel(HabitModel habitModel);
    }


}
