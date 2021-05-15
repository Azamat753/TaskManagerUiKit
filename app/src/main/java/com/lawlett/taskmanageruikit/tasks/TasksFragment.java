package com.lawlett.taskmanageruikit.tasks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.help.HelpActivity;
import com.lawlett.taskmanageruikit.tasksPage.addTask.CustomTaskDialog;
import com.lawlett.taskmanageruikit.tasksPage.addTask.DoneActivity;
import com.lawlett.taskmanageruikit.tasksPage.data.model.DoneModel;
import com.lawlett.taskmanageruikit.tasksPage.homeTask.CustomHomeDialog;
import com.lawlett.taskmanageruikit.tasksPage.homeTask.HomeActivity;
import com.lawlett.taskmanageruikit.tasksPage.meetTask.MeetActivity;
import com.lawlett.taskmanageruikit.tasksPage.personalTask.PersonalActivity;
import com.lawlett.taskmanageruikit.tasksPage.privateTask.PrivateActivity;
import com.lawlett.taskmanageruikit.tasksPage.workTask.WorkActivity;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.Constants;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.PassCodeActivity;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;
import com.lawlett.taskmanageruikit.utils.preferences.AddDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.preferences.PasswordPassDonePreference;
import com.lawlett.taskmanageruikit.utils.preferences.TaskDialogPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TasksFragment extends Fragment {
    private ImageView personalImage, workImage, meetImage, homeImage, privateImage, doneImage;
    private TextView personal_amount, work_amount, meet_amount,
            home_amount, private_amount, done_amount, done_title,
            home_title, meet_title, personal_title, work_title;
    private Integer doneAmount, personalAmount, workAmount, meetAmount, homeAmount, privateAmount;
    private ConstraintLayout personConst, workConst, meetConst, homeConst, privateConst, customConst;
    private LinearLayout addConst, doneConst, bubble;
    private List<DoneModel> list;
    private int buttonListen;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TaskDialogPreference.init(requireContext());
        initViews(view);
        try {
            setTitleAndImages();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        setBubble();
        initClickers();
        initRoom();
        setLongClickListeners();
    }

    private void setBubble() {
        if (!TaskDialogPreference.isShown()) {
            bubble.setVisibility(View.VISIBLE);
            bubble.setOnClickListener(view -> startActivity(new Intent(requireActivity(), HelpActivity.class)));
            TaskDialogPreference.saveShown();
        }
        bubble.setVisibility(View.GONE);
    }

    private void setTitleAndImages() {
        if (!TaskDialogPreference.getTitle().isEmpty()) {
            done_title.setText(TaskDialogPreference.getTitle());
            doneImage.setImageResource(TaskDialogPreference.getImage());
            doneConst.setVisibility(View.VISIBLE);
            addConst.setVisibility(View.GONE);
        }
        if (!TaskDialogPreference.getHomeTitle().isEmpty()) {
            home_title.setText(TaskDialogPreference.getHomeTitle());
            homeImage.setImageResource(TaskDialogPreference.getHomeImage());
        } else {
            home_title.setText(getResources().getString(R.string.home));
            homeImage.setImageResource(R.drawable.ic_home);
            TaskDialogPreference.saveHomeImage(R.drawable.ic_home);
            TaskDialogPreference.saveHomeTitle(getResources().getString(R.string.home));
        }
        if (!TaskDialogPreference.getMeetTitle().isEmpty()) {
            meet_title.setText(TaskDialogPreference.getMeetTitle());
            meetImage.setImageResource(TaskDialogPreference.getMeetImage());
        }
        if (!TaskDialogPreference.getPersonTitle().isEmpty()) {
            personal_title.setText(TaskDialogPreference.getPersonTitle());
            personalImage.setImageResource(TaskDialogPreference.getPersonImage());
        }
        if (!TaskDialogPreference.getWorkTitle().isEmpty()) {
            work_title.setText(TaskDialogPreference.getWorkTitle());
            workImage.setImageResource(TaskDialogPreference.getWorkImage());
        }
    }

    private void initClickers() {
        customConst.setOnClickListener(
                view -> {
                    String name = TaskDialogPreference.getTitle();
                    if (!name.isEmpty()) {
                        startActivity(new Intent(requireContext(), DoneActivity.class));
                    } else {
                        showCustomTaskDialog();
                    }
                }
        );
        personConst.setOnClickListener(v -> startActivity(new Intent(getContext(), PersonalActivity.class)));
        workConst.setOnClickListener(v -> startActivity(new Intent(getContext(), WorkActivity.class)));
        meetConst.setOnClickListener(v -> startActivity(new Intent(getContext(), MeetActivity.class)));
        homeConst.setOnClickListener(v -> startActivity(new Intent(getContext(), HomeActivity.class)));
        privateConst.setOnClickListener(v -> {
            if (!PasswordPassDonePreference.getInstance(getContext()).isPass()) {
                startActivity(new Intent(getContext(), PassCodeActivity.class));
            } else {
                startActivity(new Intent(getContext(), PrivateActivity.class));
            }
        });
        personalImage.setOnClickListener(v -> startActivity(new Intent(getContext(), PersonalActivity.class)));
        privateImage.setOnClickListener(v -> {
            if (!PasswordPassDonePreference.getInstance(getContext()).isPass()) {
                startActivity(new Intent(getContext(), PassCodeActivity.class));
            } else {
                startActivity(new Intent(getContext(), PrivateActivity.class));
            }
        });

        addConst.setOnClickListener(v -> showCustomTaskDialog());
    }

    private void initRoom() {
        list = new ArrayList<>();
        App.getDataBase().doneDao().getAllLive().observe(this, doneModels -> {
            if (list != null) {
                list.clear();
                list.addAll(doneModels);
            }
        });
    }

    private void initViews(View view) {
        personConst = view.findViewById(R.id.personconst);
        workConst = view.findViewById(R.id.workconst);
        meetConst = view.findViewById(R.id.meetconst);
        homeConst = view.findViewById(R.id.homeconst);
        privateConst = view.findViewById(R.id.privateconst);
        addConst = view.findViewById(R.id.addconst);
        personalImage = view.findViewById(R.id.person_image);
        workImage = view.findViewById(R.id.work_image);
        meetImage = view.findViewById(R.id.meet_image);
        homeImage = view.findViewById(R.id.home_image);
        privateImage = view.findViewById(R.id.private_image);
        doneImage = view.findViewById(R.id.done_tasks_image);
        personal_amount = view.findViewById(R.id.personal_amount);
        work_amount = view.findViewById(R.id.work_amount);
        meet_amount = view.findViewById(R.id.meet_task_amount);
        home_amount = view.findViewById(R.id.home_task_amount);
        private_amount = view.findViewById(R.id.private_task_amount);
        done_amount = view.findViewById(R.id.done_amount);
        done_title = view.findViewById(R.id.done_task_title);
        home_title = view.findViewById(R.id.home_task_title);
        meet_title = view.findViewById(R.id.meet_task_title);
        personal_title = view.findViewById(R.id.person_task_title);
        work_title = view.findViewById(R.id.work_task_title);
        bubble = view.findViewById(R.id.bubble);
        doneConst = view.findViewById(R.id.doneconst);
        customConst = view.findViewById(R.id.newconst);
        ProgressBar progressBar = view.findViewById(R.id.tasks_progress_bar);
    }

    private void setLongClickListeners() {
        customConst.setOnLongClickListener(v -> {
            final String[] listItems = {getString(R.string.change_image_title), getString(R.string.remove_category)};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.what_to_do);
            builder.setSingleChoiceItems(listItems, -1, (dialog, which) -> {
                if (which == 0) {
                    showCustomTaskDialog();
                }
                if (which == 1) {
                    App.getDataBase().doneDao().deleteAll(list);
                    AddDoneSizePreference.getInstance(getContext()).clearSettings();
                    TaskDialogPreference.remove();
                    doneConst.setVisibility(View.GONE);
                    addConst.setVisibility(View.VISIBLE);
                    done_amount.setText("0");
                }
                dialog.dismiss();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return false;
        });
        homeConst.setOnLongClickListener(v -> {
            buttonListen = 4;
            showAlertDialog();
            return false;
        });
        workConst.setOnLongClickListener(v -> {
            buttonListen = 2;
            showAlertDialog();
            return false;
        });
        personConst.setOnLongClickListener(v -> {
            buttonListen = 1;
            showAlertDialog();
            return false;
        });
        meetConst.setOnLongClickListener(v -> {
            buttonListen = 3;
            showAlertDialog();
            return false;
        });
    }

    private void showAlertDialog() {
        PlannerDialog.showPlannerDialog(requireActivity(), getString(R.string.change_image_title), this::showCustomHomeDialog);
    }

    public void showCustomTaskDialog() {
        CustomTaskDialog customTaskDialog = new CustomTaskDialog(requireContext());
        customTaskDialog.setDialogResult((title, image, visible, gone) -> {
            done_title.setText(title);
            doneImage.setImageResource(image);
            doneConst.setVisibility(visible);
            addConst.setVisibility(gone);
            saveCategoryName(Constants.CUSTOM_DOCUMENT_NAME, title, image);
        });
        customTaskDialog.show();
    }

    public void showCustomHomeDialog() {
        CustomHomeDialog customHomeDialog = new CustomHomeDialog(requireContext());
        customHomeDialog.setDialogResult((title, image) -> {
            switch (buttonListen) {
                case 1:
                    TaskDialogPreference.savePersonImage(image);
                    TaskDialogPreference.savePersonTitle(title);
                    personal_title.setText(title);
                    personalImage.setImageResource(image);
                    saveCategoryName(Constants.PERSONAL_DOCUMENT_NAME, title, image);
                    break;
                case 2:
                    TaskDialogPreference.saveWorkImage(image);
                    TaskDialogPreference.saveWorkTitle(title);
                    work_title.setText(title);
                    workImage.setImageResource(image);
                    saveCategoryName(Constants.WORK_DOCUMENT_NAME, title, image);
                    break;
                case 3:
                    TaskDialogPreference.saveMeetImage(image);
                    TaskDialogPreference.saveMeetTitle(title);
                    meet_title.setText(title);
                    meetImage.setImageResource(image);
                    saveCategoryName(Constants.MEET_DOCUMENT_NAME, title, image);
                    break;
                case 4:
                    TaskDialogPreference.saveHomeImage(image);
                    TaskDialogPreference.saveHomeTitle(title);
                    home_title.setText(title);
                    homeImage.setImageResource(image);
                    saveCategoryName(Constants.HOME_DOCUMENT_NAME, title, image);
                    break;
            }
        });
        customHomeDialog.show();
    }

    private void saveCategoryName(String documentName, String title, int icon) {
        if (user != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("categoryName", title.trim());
            map.put("categoryIcon", icon);
            FireStoreTools.writeOrUpdateDataByFireStore(documentName, Constants.CATEGORY_COLLECTION + "-" + "(" + user.getDisplayName() + ")" + user.getUid(), db, map);
        }
    }

    @SuppressLint("SetTextI18n")
    public void notifyView() {
        personal_amount.setText(personalAmount + "");
        work_amount.setText(workAmount + "");
        meet_amount.setText(meetAmount + "");
        home_amount.setText(homeAmount + "");
        private_amount.setText(privateAmount + "");
        done_amount.setText(doneAmount + "");
    }

    @Override
    public void onResume() {
        super.onResume();
        personalAmount = App.getDataBase().personalDao().getAll().size();
        workAmount = App.getDataBase().workDao().getAll().size();
        meetAmount = App.getDataBase().meetDao().getAll().size();
        homeAmount = App.getDataBase().homeDao().getAll().size();
        doneAmount = App.getDataBase().doneDao().getAll().size();
        privateAmount = App.getDataBase().privateDao().getAll().size();
        notifyView();
    }
}
