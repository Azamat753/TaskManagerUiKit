package com.lawlett.taskmanageruikit.tasksPage.homeTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.achievement.models.LevelModel;
import com.lawlett.taskmanageruikit.tasksPage.data.model.HomeModel;
import com.lawlett.taskmanageruikit.tasksPage.homeTask.recycler.HomeAdapter;
import com.lawlett.taskmanageruikit.tasksPage.meetTask.MeetActivity;
import com.lawlett.taskmanageruikit.tasksPage.personalTask.PersonalActivity;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.DoneTasksPreferences;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.HomeDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.KeyboardHelper;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeActivity extends AppCompatActivity implements HomeAdapter.IHCheckedListener, ActionForDialog {
    RecyclerView recyclerView;
    HomeAdapter adapter;
    List<HomeModel> list;
    HomeModel homeModel;
    EditText editText;
    int pos, previousData, currentData, updateData;
    LinearLayout linearLayoutHome;
    ImageView homeBack, imageMic, imageAdd, changeTask_image;
    private static final int REQUEST_CODE_SPEECH_INPUT = 22;
    boolean isButton = false;
    ImageView homeSettings;
    DialogHelper dialogHelper = new DialogHelper();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String oldDocumentName;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private String collectionName;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
        initClickers();
        initToolbar();
        getRecordsFromRoom();
        editListener();
        initItemTouchHelper();
    }

    private void updateTask(int id) {
        homeModel = list.get(id);
        homeModel.setHomeTask(editText.getText().toString());
        App.getDataBase().homeDao().update(list.get(id));
        adapter.notifyDataSetChanged();
    }

    private void initClickers() {
        homeBack.setOnClickListener(v -> onBackPressed());
        homeSettings.setOnClickListener((View.OnClickListener) v -> dialogHelper.myDialog(HomeActivity.this, (ActionForDialog) HomeActivity.this));
        imageAdd.setOnClickListener(v -> {
            recordRoom();
            if (user != null) {
                FireStoreTools.writeOrUpdateDataByFireStore(homeModel.getHomeTask(), collectionName, db, homeModel);
            }
        });

        changeTask_image.setOnClickListener(v -> {
            if (editText.getText().toString().trim().isEmpty()) {
                Toast.makeText(HomeActivity.this, R.string.empty, Toast.LENGTH_SHORT).show();
            } else {
                updateTask(pos);
                changeTask_image.setVisibility(View.GONE);
                imageMic.setVisibility(View.GONE);
                imageAdd.setVisibility(View.VISIBLE);
                KeyboardHelper.hideKeyboard(HomeActivity.this, changeTask_image, editText);
                if (user != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    homeModel = list.get(pos); //todo Для обновления тасков в облаке нужно имя документа которое было назначено в первый раз при создании,нужно создать поля в руме documentName и при обновление таскать его
                    String newDocumentName = editText.getText().toString();//todo Временное решение
                    homeModel.homeTask = editText.getText().toString();
                    FireStoreTools.deleteDataByFireStore(oldDocumentName, collectionName, db, progressBar);
                    FireStoreTools.writeOrUpdateDataByFireStore(newDocumentName, collectionName, db, homeModel);
                }
                editText.getText().clear();
            }
        });
    }

    private void initItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(list, i, i + 1);
                        int order1 = (int) list.get(i).getId();
                        int order2 = (int) list.get(i + 1).getId();
                        list.get(i).setId(order2);
                        list.get(i + 1).setId(order1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(list, i, i - 1);
                        int order1 = (int) list.get(i).getId();
                        int order2 = (int) list.get(i - 1).getId();
                        list.get(i).setId(order2);
                        list.get(i - 1).setId(order1);
                    }
                }
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }


            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                App.getDataBase().homeDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                PlannerDialog.showPlannerDialog(HomeActivity.this, getString(R.string.you_sure_delete), () -> {
                    pos = viewHolder.getAdapterPosition();
                    homeModel = list.get(pos);
                    if (!homeModel.isDone) {
                        App.getDataBase().homeDao().delete(list.get(pos));
                    } else {
                        decrementDone();
                        App.getDataBase().homeDao().update(list.get(pos));
                        App.getDataBase().homeDao().delete(list.get(pos));
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(HomeActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
                    if (user != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        FireStoreTools.deleteDataByFireStore(homeModel.getHomeTask(), collectionName, db, progressBar);
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                final int DIRECTION_RIGHT = 1;
                final int DIRECTION_LEFT = 0;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
                    int direction = dX > 0 ? DIRECTION_RIGHT : DIRECTION_LEFT;
                    Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    switch (direction) {
                        case DIRECTION_RIGHT:
                            View itemView = viewHolder.itemView;
                            final ColorDrawable background = new ColorDrawable(Color.RED);
                            background.setBounds(0, itemView.getTop(), (int) (itemView.getLeft() + dX), itemView.getBottom());
                            background.draw(c);
                            vb.vibrate(100);
                            break;
                        case DIRECTION_LEFT:
                            View itemView2 = viewHolder.itemView;
                            final ColorDrawable background2 = new ColorDrawable(Color.RED);
                            background2.setBounds(itemView2.getRight(), itemView2.getBottom(), (int) (itemView2.getRight() + dX), itemView2.getTop());
                            background2.draw(c);
                            vb.vibrate(100);
                            break;
                    }

                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void initViews() {
        homeBack = findViewById(R.id.personal_back);
        homeSettings = findViewById(R.id.settings_for_task);
        linearLayoutHome = findViewById(R.id.linearHome);
        recyclerView = findViewById(R.id.recycler_home);
        adapter = new HomeAdapter(this);
        recyclerView.setAdapter(adapter);
        editText = findViewById(R.id.editText_home);
        imageMic = findViewById(R.id.mic_task_home);
        imageAdd = findViewById(R.id.add_task_home);
        progressBar = findViewById(R.id.progress_bar);
        changeTask_image = findViewById(R.id.change_task_home);
    }

    private void getRecordsFromRoom() {
        list = new ArrayList<>();
        App.getDataBase().homeDao().getAllLive().observe(this, homeModels -> {
            if (homeModels != null) {
                progressBar.setVisibility(View.GONE);
                list.clear();
                list.addAll(homeModels);
                Collections.reverse(list);
                adapter.updateList(list);
                if (homeModels.size() == 0) {
                    readDataFromFireStore();
                }
            }
        });
    }

    private void readDataFromFireStore() {
        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);
            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                if (task.getResult().getDocuments().size() == 0) {
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    Map<String, Object> dataFromFireBase;
                                    dataFromFireBase = document.getData();
                                    Boolean taskBoolean = (Boolean) dataFromFireBase.get("isDone");
                                    String homeTask = dataFromFireBase.get("homeTask").toString();
                                    homeModel = new HomeModel(homeTask, taskBoolean);
                                    App.getDataBase().homeDao().insert(homeModel);
                                }
                            }
                        }else {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void recordRoom() {
        if (editText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();
        } else {
            homeModel = new HomeModel(editText.getText().toString().trim(), false);
            App.getDataBase().homeDao().insert(homeModel);
            editText.setText("");
        }
    }

    public void initToolbar() {
        TextView toolbar = findViewById(R.id.toolbar_title);
        if (TaskDialogPreference.getHomeTitle().isEmpty()) {
            toolbar.setText(R.string.home);
        } else {
            toolbar.setText(TaskDialogPreference.getHomeTitle());
        }
        if (user != null) {
            collectionName = toolbar.getText().toString() + "-" + "(" + user.getDisplayName() + ")" + user.getUid();
        }
    }

    @Override
    public void onItemCheckClick(int id) {
        homeModel = list.get(id);
        if (!homeModel.isDone) {
            homeModel.isDone = true;
            incrementDone();
        } else {
            homeModel.isDone = false;
            decrementDone();
        }
        App.getDataBase().homeDao().update(list.get(id));
        if (user != null) {
            FireStoreTools.writeOrUpdateDataByFireStore(homeModel.getHomeTask(), collectionName, db, homeModel);
        }
    }

    @Override
    public void onItemLongClick(int pos) {
        if (imageAdd.getVisibility() == View.VISIBLE) {
            imageAdd.setVisibility(View.GONE);
        }
        imageMic.setVisibility(View.GONE);
        changeTask_image.setVisibility(View.VISIBLE);
        homeModel = list.get(pos);
        oldDocumentName = homeModel.getHomeTask();
        editText.setText(homeModel.getHomeTask());
        this.pos = pos;
        KeyboardHelper.openKeyboard(HomeActivity.this);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }

    private void setLevel(int size) {
        if (size < 26) {
            if (size % 5 == 0) {
                int lvl = size / 5;
                String level = getString(R.string.attaboy) + " " + lvl;
                addToLocalDate(lvl, level);
                showDialogLevel(level);
            }
        } else if (size > 26 && size < 51) {
            if (size % 5 == 0) {
                int lev = size / 5;
                String level = getString(R.string.Persistent) + " " + lev;
                addToLocalDate(lev, level);
                showDialogLevel(level);
            }
        } else if (size > 51 && size < 76) {
            if (size % 5 == 0) {
                int lev = size / 5;
                String level = getString(R.string.Overwhelming) + " " + lev;
                addToLocalDate(lev, level);
                showDialogLevel(level);
            }
        }
    }

    private void addToLocalDate(int id, String level) {
        LevelModel levelModel = new LevelModel(id, new Date(System.currentTimeMillis()), level);
        App.getDataBase().levelDao().insert(levelModel);
    }

    private void showDialogLevel(String l) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.important_message))
                .setMessage(getString(R.string.you_got) + l)
                .setPositiveButton(getString(R.string.apply), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create();
        builder.show();
    }

    private void incrementAllDone() {
        int previousSize = DoneTasksPreferences.getInstance(this).getDataSize();
        DoneTasksPreferences.getInstance(this).saveDataSize(previousSize + 1);
        setLevel(DoneTasksPreferences.getInstance(this).getDataSize());
    }

    private void decrementAllDone() {
        int currentSize = DoneTasksPreferences.getInstance(this).getDataSize();
        int updateSize = currentSize - 1;
        if (updateSize >= 0) {
            DoneTasksPreferences.getInstance(this).saveDataSize(updateSize);
        }
    }

    private void incrementDone() {
        previousData = HomeDoneSizePreference.getInstance(this).getDataSize();
        HomeDoneSizePreference.getInstance(this).saveDataSize(previousData + 1);
        incrementAllDone();
    }

    private void decrementDone() {
        currentData = HomeDoneSizePreference.getInstance(this).getDataSize();
        updateData = currentData - 1;
        HomeDoneSizePreference.getInstance(this).saveDataSize(updateData);
        decrementAllDone();
    }

    @Override
    public void pressOk() {
        App.getDataBase().homeDao().deleteAll(list);
        HomeDoneSizePreference.getInstance(HomeActivity.this).clearSettings();
        deleteAllDocumentsFromFireStore();
    }

    private void deleteAllDocumentsFromFireStore() {
        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);
            if (list.size() != 0) {
                for (int i = 0; i < list.size(); i++) {
                    String personalTask = list.get(i).homeTask;
                    FireStoreTools.deleteDataByFireStore(personalTask, collectionName, db, progressBar);
                }
            }
        }
    }

    private void editListener() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null && !isButton && !editText.getText().toString().trim().isEmpty()) {
                    imageMic.setVisibility(View.GONE);
                    if (changeTask_image.getVisibility() == View.VISIBLE) {
                        imageAdd.setVisibility(View.GONE);
                    } else {
                        imageAdd.setVisibility(View.VISIBLE);
                    }
                    isButton = true;
                }
                if (editText.getText().toString().isEmpty() && isButton) {
                    changeTask_image.setVisibility(View.GONE);
                    imageAdd.setVisibility(View.GONE);
                    imageMic.setVisibility(View.VISIBLE);
                    isButton = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void micHomeTask(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_something));
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}