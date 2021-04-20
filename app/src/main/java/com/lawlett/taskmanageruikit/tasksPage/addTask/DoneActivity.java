package com.lawlett.taskmanageruikit.tasksPage.addTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.lawlett.taskmanageruikit.tasksPage.addTask.adapter.DoneAdapter;
import com.lawlett.taskmanageruikit.tasksPage.data.model.DoneModel;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.preferences.AddDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.Constants;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.DoneTasksPreferences;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.KeyboardHelper;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;
import com.lawlett.taskmanageruikit.utils.preferences.TaskDialogPreference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DoneActivity extends AppCompatActivity implements DoneAdapter.IMCheckedListener, ActionForDialog {
    DoneAdapter adapter;
    List<DoneModel> list;
    DoneModel doneModel;
    EditText editText;
    int pos, previousData, currentData, updateData;
    ImageView doneBack, addTask, imageMic, changeTask_image;
    private static final int REQUEST_CODE_SPEECH_INPUT = 22;
    boolean isButton = false;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    private String collectionName;
    private ProgressBar progressBar;
    String oldDocumentName;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);
        TaskDialogPreference.init(this);
        initViews();
        initClickers();
        initToolbar();
        getRecordsRoomData();
        editListener();
        initItemTouchHelper();
    }

    private void readDataFromFireStore() {
        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);
            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                progressBar.setVisibility(View.GONE);
                                Map<String, Object> dataFromFireBase;
                                dataFromFireBase = document.getData();
                                Boolean taskBoolean = (Boolean) dataFromFireBase.get("isDone");
                                String homeTask = dataFromFireBase.get("doneTask").toString();
                                doneModel = new DoneModel(homeTask, taskBoolean);
                                App.getDataBase().doneDao().insert(doneModel);
                            }
                        }
                    });
        }
    }

    private void writeAllTaskFromRoomToFireStore() {
        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);
            SharedPreferences sharedPreferences = getSharedPreferences("donePreferences", Context.MODE_PRIVATE);
            Calendar calendar = Calendar.getInstance();
            String currentDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String dayFromPreference = sharedPreferences.getString(Constants.CURRENT_DAY, "");
            if (!currentDay.equals(dayFromPreference)) {
                deleteAllDocumentsFromFireStore();
                for (int i = 0; i < list.size(); i++) {
                    FireStoreTools.writeOrUpdateDataByFireStore(list.get(i).getDoneTask(), collectionName, db, doneModel);
                }
                sharedPreferences.edit().clear().apply();
                sharedPreferences.edit().putString("currentDay", currentDay).apply();
            }
        }
    }

    private void initClickers() {
        doneBack.setOnClickListener(v -> onBackPressed());
        findViewById(R.id.settings_for_task).setOnClickListener(v -> {
            DialogHelper dialogHelper = new DialogHelper();
            dialogHelper.myDialog(DoneActivity.this, DoneActivity.this);
        });
        addTask.setOnClickListener(v -> {
            recordDataRoom();
            if (user != null) {
                FireStoreTools.writeOrUpdateDataByFireStore(doneModel.getDoneTask(), collectionName, db, doneModel);
                writeAllTaskFromRoomToFireStore();
            }
        });
        changeTask_image.setOnClickListener(v -> {
            if (editText.getText().toString().trim().isEmpty()) {
                Toast.makeText(DoneActivity.this, R.string.empty, Toast.LENGTH_SHORT).show();
            } else {
                updateTask(pos);
                changeTask_image.setVisibility(View.GONE);
                imageMic.setVisibility(View.GONE);
                addTask.setVisibility(View.VISIBLE);
                KeyboardHelper.hideKeyboard(DoneActivity.this, changeTask_image, editText);
                if (user != null) {
                    doneModel = list.get(pos); //todo Для обновления тасков в облаке нужно имя документа которое было назначено в первый раз при создании,нужно создать поля в руме documentName и при обновление таскать его
                    String newDocumentName = editText.getText().toString();//todo Временное решение
                    doneModel.doneTask = editText.getText().toString();
                    if (user != null) {
                        FireStoreTools.deleteDataByFireStore(oldDocumentName, collectionName, db, progressBar);
                        FireStoreTools.writeOrUpdateDataByFireStore(newDocumentName, collectionName, db, doneModel);
                    }
                }
                editText.getText().clear();
            }
        });
    }

    private void updateTask(int id) {
        doneModel = list.get(id);
        doneModel.setDoneTask(editText.getText().toString());
        App.getDataBase().doneDao().update(list.get(id));
        adapter.notifyDataSetChanged();
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
                App.getDataBase().doneDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                PlannerDialog.showPlannerDialog(DoneActivity.this, getString(R.string.you_sure_delete), () -> {
                    pos = viewHolder.getAdapterPosition();
                    doneModel = list.get(pos);
                    if (!doneModel.isDone) {
                        App.getDataBase().doneDao().delete(list.get(pos));
                    } else {
                        decrementDone();
                        App.getDataBase().doneDao().update(list.get(pos));
                        App.getDataBase().doneDao().delete(list.get(pos));
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(DoneActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
                    if (user != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        FireStoreTools.deleteDataByFireStore(doneModel.getDoneTask(), collectionName, db, progressBar);
                    }
                });
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void getRecordsRoomData() {
        App.getDataBase().doneDao().getAllLive().observe(this, doneModels -> {
            if (doneModels != null) {
                progressBar.setVisibility(View.GONE);
                list.clear();
                list.addAll(doneModels);
                Collections.sort(list, (doneModel, t1) -> Boolean.compare(t1.isDone, doneModel.isDone));
                Collections.reverse(list);
                adapter.updateList(list);
                if (doneModels.size() == 0) {
                    readDataFromFireStore();
                }
            }
        });
    }

    private void initViews() {
        list = new ArrayList<>();
        adapter = new DoneAdapter(this);
        recyclerView = findViewById(R.id.recycler_done);
        recyclerView.setAdapter(adapter);
        editText = findViewById(R.id.editText_done);
        addTask = findViewById(R.id.add_task_done);
        imageMic = findViewById(R.id.mic_task_done);
        doneBack = findViewById(R.id.personal_back);
        progressBar = findViewById(R.id.progress_bar);
        changeTask_image = findViewById(R.id.change_task_done);
    }

    public void recordDataRoom() {
        if (editText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Пусто", Toast.LENGTH_SHORT).show();
        } else {
            doneModel = new DoneModel(editText.getText().toString().trim(), false);
            App.getDataBase().doneDao().insert(doneModel);
            editText.setText("");
        }
    }

    public void initToolbar() {
        TextView toolbar = findViewById(R.id.toolbar_title);
        toolbar.setText(TaskDialogPreference.getTitle());
        if (user != null) {
            collectionName = toolbar.getText().toString() + "-" + "(" + user.getDisplayName() + ")" + user.getUid();
        }
    }

    @Override
    public void onItemCheckClick(int id) {
        doneModel = list.get(id);
        if (!doneModel.isDone) {
            doneModel.isDone = true;
            incrementDone();
        } else {
            doneModel.isDone = false;
            decrementDone();
        }
        App.getDataBase().doneDao().update(list.get(id));
        if (user != null) {
            FireStoreTools.writeOrUpdateDataByFireStore(doneModel.getDoneTask(), collectionName, db, doneModel);
        }
    }

    @Override
    public void onItemLongClick(int pos) {
        if (addTask.getVisibility() == View.VISIBLE) {
            addTask.setVisibility(View.GONE);
        }
        imageMic.setVisibility(View.GONE);
        changeTask_image.setVisibility(View.VISIBLE);
        doneModel = list.get(pos);
        oldDocumentName = doneModel.getDoneTask();
        editText.setText(doneModel.getDoneTask());
        this.pos = pos;
        KeyboardHelper.openKeyboard(DoneActivity.this);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }

    private void incrementDone() {
        previousData = AddDoneSizePreference.getInstance(this).getDataSize();
        AddDoneSizePreference.getInstance(this).saveDataSize(previousData + 1);
        incrementAllDone();
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

    private void decrementDone() {
        currentData = AddDoneSizePreference.getInstance(this).getDataSize();
        updateData = currentData - 1;
        AddDoneSizePreference.getInstance(this).saveDataSize(updateData);
        decrementAllDone();
    }

    @Override
    public void pressOk() {
        App.getDataBase().doneDao().deleteAll(list);
        AddDoneSizePreference.getInstance(DoneActivity.this).clearSettings();
        deleteAllDocumentsFromFireStore();
    }

    private void deleteAllDocumentsFromFireStore() {
        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);
            if (list.size() != 0) {
                for (int i = 0; i < list.size(); i++) {
                    String personalTask = list.get(i).getDoneTask();
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
                        addTask.setVisibility(View.GONE);
                    } else {
                        addTask.setVisibility(View.VISIBLE);
                    }
                    isButton = true;
                }
                if (editText.getText().toString().isEmpty() && isButton) {
                    changeTask_image.setVisibility(View.GONE);
                    addTask.setVisibility(View.GONE);
                    imageMic.setVisibility(View.VISIBLE);
                    isButton = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    public void micDoneTask(View view) {
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            assert result != null;
            editText.setText(editText.getText() + " " + result.get(0));
        }
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
                .setPositiveButton(getString(R.string.apply), (dialog, id) -> {
                    dialog.cancel();
                });
        builder.create();
        builder.show();
    }
}