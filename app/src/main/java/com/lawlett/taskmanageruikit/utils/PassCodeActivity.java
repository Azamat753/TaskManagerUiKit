package com.lawlett.taskmanageruikit.utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.hanks.passcodeview.PasscodeView;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.tasksPage.privateTask.PrivateActivity;
import com.lawlett.taskmanageruikit.utils.preferences.PasswordDonePreference;
import com.lawlett.taskmanageruikit.utils.preferences.PasswordPassDonePreference;
import com.lawlett.taskmanageruikit.utils.preferences.PasswordPreference;


public class PassCodeActivity extends AppCompatActivity {
    PasscodeView passcodeView;
    String password;
    EditText editPassword,editQst,editQstAnswer;
    Button button;
    public static final String SAVED_QST = "saved_qst";
    public static final String SAVED_ANSWER = "saved_answer";

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_code);


        editPassword = findViewById(R.id.edit_password);
        button = findViewById(R.id.ok_btn);
        passcodeView = findViewById(R.id.passcode_view);
//        editQst = findViewById(R.id.edit_question_create);
        editQstAnswer = findViewById(R.id.edit_answer);
        password = PasswordPreference.getInstance(this).returnPassword();

        boolean isShown = PasswordDonePreference.getInstance(getApplication()).isShown();
        if (isShown) {
            passcodeView.setVisibility(View.VISIBLE);
            editPassword.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
        } else {
            passcodeView.setVisibility(View.GONE);
            editPassword.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
        }
        passcodeView.setPasscodeLength(4)
                .setFirstInputTip(getString(R.string.of_symbols))
                .setSecondInputTip(getString(R.string.re_enter))
                .setWrongLengthTip(getString(R.string.of_symbols))
                .setWrongInputTip(getString(R.string.do_not_match))
                .setCorrectInputTip(getString(R.string.correct_password))
                .setLocalPasscode(password)
                .setListener(new PasscodeView.PasscodeViewListener() {
                    @Override
                    public void onFail() {
                        Toast.makeText(PassCodeActivity.this, R.string.Wrong, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(String number) {
                        startActivity(new Intent(PassCodeActivity.this, PrivateActivity.class));
                        PasswordPassDonePreference.getInstance(PassCodeActivity.this).savePass();
                        finish();
                    }
                });
    }


    public void saveBtn(View view) {
//        if (editQst.getText().toString().equals("")){
//            Toast.makeText(PassCodeActivity.this, R.string.qst_not_found, Toast.LENGTH_LONG).show();
//        }
        if(editQstAnswer.getText().toString().equals("")){
            Toast.makeText(PassCodeActivity.this, R.string.answer_not_found, Toast.LENGTH_SHORT).show();
        }else if(editPassword.getText().length()<4){
            Toast.makeText(PassCodeActivity.this, R.string.of_symbols, Toast.LENGTH_LONG).show();
        }
        else{
            SharedPreferences sPref = getSharedPreferences("qst",0);
            SharedPreferences.Editor ed = sPref.edit();
//            ed.putString(SAVED_QST, editQst.getText().toString());
            ed.putString(SAVED_ANSWER,editQstAnswer.getText().toString());
            ed.apply();
        PasswordPreference.getInstance(this).savePassword(editPassword.getText().toString().trim());
        passcodeView.setVisibility(View.VISIBLE);
        editPassword.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        PasswordDonePreference.getInstance(this).saveShown();
        finish();
        }
    }
}