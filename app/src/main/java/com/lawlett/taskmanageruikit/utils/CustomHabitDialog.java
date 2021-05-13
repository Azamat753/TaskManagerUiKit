package com.lawlett.taskmanageruikit.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.habit.fragment.HabitFragment;
import com.lawlett.taskmanageruikit.tasksPage.addTask.adapter.ImageAdapter;
import com.lawlett.taskmanageruikit.tasksPage.data.model.HabitModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomHabitDialog extends Dialog implements View.OnClickListener, HabitFragment.SendHabit {
    final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
    Button btnOk, btnCancel;
    EditText title, amount;
    GridView gridView;
    ImageAdapter imageAdapter;
    Integer img;
    CustomDialogListener customDialogListener;
    TextView habitTitle, habitAmount;
    ImageView imageView;
    List<HabitModel> list;
    HabitModel habitModel;

    public CustomHabitDialog(@NonNull Context context, HabitModel habit) {
        super(context);
        imageAdapter = new ImageAdapter(context, new Integer[]{
                R.drawable.ic_01, R.drawable.ic_work, R.drawable.ic_08, R.drawable.ic_11,
                R.drawable.ic_17, R.drawable.ic_home, R.drawable.ic_05, R.drawable.ic_meet,
                R.drawable.ic_19, R.drawable.ic_15, R.drawable.ic_12, R.drawable.ic_10,
                R.drawable.ic_09, R.drawable.ic_18, R.drawable.ic_23, R.drawable.ic_06,
                R.drawable.ic_03, R.drawable.ic_07, R.drawable.ic_13, R.drawable.ic_22,
                R.drawable.ic_21, R.drawable.ic_person, R.drawable.ic_04, R.drawable.ic_16,
        });
        this.habitModel = habit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_dialog_habit);
        initView();
        gridViewInit();
    }

//    private void checkEmpty(HabitModel habitModel) {
//            habitTitle.setText(habitModel.getTitle());
//            habitAmount.setText(habitModel.getAllDays());
//            title.setText(habitModel.getTitle());
//            amount.setText(habitModel.getAllDays());
//            imageView.setImageResource(habitModel.getImage());
//    }

    public EditText getTitleEdit() {
        return findViewById(R.id.dialog_habit_title);
    }

    public EditText getAmountDayEdit() {
        return findViewById(R.id.dialog_habit_amount);
    }

    public ImageView getImageView() {
        return findViewById(R.id.dialog_example_img);
    }

    private void gridViewInit() {
        gridView.setAdapter(imageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                img = (Integer) adapterView.getItemAtPosition(i);
                view.startAnimation(animation);
                imageView.setImageResource(img);
                View v = getCurrentFocus();
                KeyboardHelper.hideKeyboard(getContext(), view, title);
            }
        });
    }

    private void initView() {
        Log.e("hgfdghjkjhgfhjkhg", "initView: " + habitModel);
        btnOk = findViewById(R.id.dialog_habit_ok);
        btnCancel = findViewById(R.id.dialog_habit_cancel);
        title = findViewById(R.id.dialog_habit_title);
        amount = findViewById(R.id.dialog_habit_amount);
        gridView = findViewById(R.id.dialog_habit_gridView);
        habitTitle = findViewById(R.id.dialog_example_tittle);
        habitAmount = findViewById(R.id.dialog_example_amount);
        imageView = findViewById(R.id.dialog_example_img);
        list = new ArrayList<>();

        if (habitModel!=null){
            title.setText(habitModel.getTitle());
            amount.setText(habitModel.getAllDays());
            imageView.setImageResource(habitModel.getImage());
        }
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = amount.getText().toString();
                habitAmount.setText(text);
            }
        });
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = title.getText().toString();
                habitTitle.setText(text);
            }
        });
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_habit_ok:
                String currentTitle = title.getText().toString();
                String allDays = amount.getText().toString();
                if (currentTitle.isEmpty() || allDays.isEmpty()) {
                    Toast.makeText(getContext(), R.string.add_title, Toast.LENGTH_LONG).show();
                } else if (img == null) {
                    Toast.makeText(getContext(), R.string.add_icon, Toast.LENGTH_LONG).show();
                } else {
                    customDialogListener.addInformation(currentTitle, allDays, img);
                    dismiss();
                }
                break;
            case R.id.dialog_habit_cancel:
                dismiss();
                break;
        }

    }

    public void setDialogResult(CustomDialogListener listener) {
        customDialogListener = listener;
    }

    @Override
    public void getHabitModel(HabitModel habitModel) {
        if (title!=null&&amount!=null&&imageView!=null){
            title.setText(habitModel.getTitle());
            amount.setText(habitModel.getAllDays());
            imageView.setImageResource(habitModel.getImage());
        }
    }


    public interface CustomDialogListener {
        void addInformation(String title, String amount, Integer image);
    }
}
