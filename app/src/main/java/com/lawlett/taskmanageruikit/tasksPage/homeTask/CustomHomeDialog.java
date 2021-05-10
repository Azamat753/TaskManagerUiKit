package com.lawlett.taskmanageruikit.tasksPage.homeTask;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.tasksPage.addTask.adapter.ImageAdapter;
import com.lawlett.taskmanageruikit.utils.preferences.TaskDialogPreference;

public class CustomHomeDialog extends Dialog implements View.OnClickListener {
    final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
    Button btnOk, btnCancel;
    EditText dialogEt;
    GridView dialogGridView;
    ImageAdapter imageAdapter;
    Integer dialogImg;
    CustomDialogListener customDialogListener;
    TextView title, amount;
    ImageView imageView;

    public CustomHomeDialog(@NonNull Context context) {
        super(context);
        TaskDialogPreference.init(context);
        imageAdapter = new ImageAdapter(context, new Integer[]{
                R.drawable.ic_01,R.drawable.ic_work, R.drawable.ic_08, R.drawable.ic_11,
                R.drawable.ic_17, R.drawable.ic_home,R.drawable.ic_05, R.drawable.ic_meet,
                R.drawable.ic_19, R.drawable.ic_15,R.drawable.ic_12,R.drawable.ic_10,
                R.drawable.ic_09,R.drawable.ic_18,  R.drawable.ic_23,R.drawable.ic_06,
                R.drawable.ic_03, R.drawable.ic_07,R.drawable.ic_13, R.drawable.ic_22,
                R.drawable.ic_21, R.drawable.ic_person, R.drawable.ic_04, R.drawable.ic_16,
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_dialog);
        initView();
        gridViewInit();
    }

    private void gridViewInit() {
        dialogGridView.setAdapter(imageAdapter);
        dialogGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogImg = (Integer) adapterView.getItemAtPosition(i);
                imageView.setImageResource(dialogImg);
                view.startAnimation(animation);
                hideKeyboard();
            }
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if(view != null){
        InputMethodManager inputMethodManager =
                (InputMethodManager)getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void initView() {
        btnOk = findViewById(R.id.dialog_btn_ok);
        btnCancel = findViewById(R.id.dialog_btn_cancel);
        dialogEt = findViewById(R.id.dialog_et);
        dialogGridView = findViewById(R.id.dialog_gridView);
        title = findViewById(R.id.dialog_h_tv_title);
        amount = findViewById(R.id.dialog_h_tv_amount);
        imageView = findViewById(R.id.dialog_h_img);

        dialogEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = dialogEt.getText().toString();
                title.setText(text);
            }
        });

        btnCancel.setOnClickListener(this);
        alertOk();
    }
    public void alertOk(){
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = dialogEt.getText().toString();
                if(title.isEmpty()){
                    Toast.makeText(getContext(),R.string.add_title, Toast.LENGTH_LONG).show();
                }
                else if(dialogImg == null){
                    Toast.makeText(getContext(),R.string.add_icon, Toast.LENGTH_LONG).show();
                }
                else {
                    customDialogListener.addInformation(title, dialogImg);
                    dismiss();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialog_btn_cancel:
                dismiss();
                break;
        }
    }

    public void setDialogResult(CustomDialogListener listener){
        customDialogListener = listener;
    }

    public interface CustomDialogListener{
        void addInformation(String title, Integer image);
    }
}
