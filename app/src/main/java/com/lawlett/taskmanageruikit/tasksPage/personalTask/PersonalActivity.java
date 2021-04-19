package com.lawlett.taskmanageruikit.tasksPage.personalTask;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.lawlett.taskmanageruikit.tasksPage.data.model.PersonalModel;
import com.lawlett.taskmanageruikit.tasksPage.personalTask.recyclerview.PersonalAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.DoneTasksPreferences;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.KeyboardHelper;
import com.lawlett.taskmanageruikit.utils.PersonDoneSizePreference;
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

public class PersonalActivity extends AppCompatActivity implements PersonalAdapter.ICheckedListener, ActionForDialog {
    private EditText editText;
    private PersonalAdapter adapter;
    private PersonalModel personalModel;
    private List<PersonalModel> list;
    private ImageView addTask_image;
    private ImageView imageMic;
    private ImageView changeTask_image;
    private RecyclerView recyclerView;
    private int position;
    private static final int REQUEST_CODE_SPEECH_INPUT = 22;
    private boolean isButton = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String collectionName;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private ProgressBar progressBar;
    String oldDocumentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        init();
        initClickers();
        initToolbar();
        getRecordsFromRoom();
        initItemTouchHelper();
        editListener();
    }

    private void initClickers() {
        findViewById(R.id.settings_for_task).setOnClickListener(v -> {
            DialogHelper dialogHelper = new DialogHelper();
            dialogHelper.myDialog(PersonalActivity.this, PersonalActivity.this);
        });
        addTask_image.setOnClickListener(view -> {
            recordDataRoom();
            if (user != null) {
                FireStoreTools.writeOrUpdateDataByFireStore(personalModel.getPersonalTask(), collectionName, db, personalModel);
            }
        });

        changeTask_image.setOnClickListener(v -> {
            if (editText.getText().toString().trim().isEmpty()) {
                Toast.makeText(PersonalActivity.this, R.string.empty, Toast.LENGTH_SHORT).show();
            } else {
                updatePersonalTask(position);
                changeTask_image.setVisibility(View.GONE);
                imageMic.setVisibility(View.GONE);
                addTask_image.setVisibility(View.VISIBLE);
                KeyboardHelper.hideKeyboard(PersonalActivity.this, changeTask_image, editText);
                if (user != null) {
                    personalModel = list.get(position); //todo Для обновления тасков в облаке нужно имя документа которое было назначено в первый раз при создании,нужно создать поля в руме documentName и при обновление таскать его
                    String newDocumentName = editText.getText().toString();//todo Временное решение
                    personalModel.personalTask = editText.getText().toString();
                    if (user != null) {
                        FireStoreTools.deleteDataByFireStore(oldDocumentName, collectionName, db);
                        FireStoreTools.writeOrUpdateDataByFireStore(newDocumentName, collectionName, db, personalModel);
                    }
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
                App.getDataBase().personalDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                PlannerDialog.showPlannerDialog(PersonalActivity.this, getString(R.string.you_sure_delete), () -> {
                    position = viewHolder.getAdapterPosition();
                    personalModel = list.get(position);
                    if (!personalModel.isDone) {
                        App.getDataBase().personalDao().delete(list.get(position));
                    } else {
                        decrementDone();
                        App.getDataBase().personalDao().update(list.get(position));
                        App.getDataBase().personalDao().delete(list.get(position));
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(PersonalActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
                    FireStoreTools.deleteDataByFireStore(personalModel.getPersonalTask(), collectionName, db);
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
                    switch (direction) {
                        case DIRECTION_RIGHT:
                            View itemView = viewHolder.itemView;
                            final ColorDrawable background = new ColorDrawable(Color.RED);
                            background.setBounds(0, itemView.getTop(), (int) (itemView.getLeft() + dX), itemView.getBottom());
                            background.draw(c);
                            break;
                        case DIRECTION_LEFT:
                            View itemView2 = viewHolder.itemView;
                            final ColorDrawable background2 = new ColorDrawable(Color.RED);
                            background2.setBounds(itemView2.getRight(), itemView2.getBottom(), (int) (itemView2.getRight() + dX), itemView2.getTop());
                            background2.draw(c);
                            break;
                    }
                }
            }
        }).attachToRecyclerView(recyclerView);
    }


    @Override
    public void pressOk() {
        App.getDataBase().personalDao().deleteAll(list);
        PersonDoneSizePreference.getInstance(PersonalActivity.this).clearSettings();
        deleteAllDocumentsFromFireStore();
    }

    private void deleteAllDocumentsFromFireStore() {
        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);
            if (list.size() != 0) {
                for (int i = 0; i < list.size(); i++) {
                    String personalTask = list.get(i).personalTask;
                    FireStoreTools.deleteDataByFireStore(personalTask, collectionName, db);
                }
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void getRecordsFromRoom() {
        App.getDataBase().personalDao().getAllLive().observe(this, personalModels -> {
            if (personalModels != null) {
                progressBar.setVisibility(View.GONE);
                list.clear();
                list.addAll(personalModels);
                Collections.sort(list, (personalModel, t1) -> Boolean.compare(t1.isDone, personalModel.isDone));
                Collections.reverse(list);
                adapter.updateList(list);
                if (personalModels.size() == 0) {
                    readDataFromFireStore();
                }
            }
        });
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
                        addTask_image.setVisibility(View.GONE);
                    } else {
                        addTask_image.setVisibility(View.VISIBLE);
                    }
                    isButton = true;
                }
                if (editText.getText().toString().isEmpty() && isButton) {
                    changeTask_image.setVisibility(View.GONE);
                    addTask_image.setVisibility(View.GONE);
                    imageMic.setVisibility(View.VISIBLE);
                    isButton = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void updatePersonalTask(int id) {
        personalModel = list.get(id);
        personalModel.setPersonalTask(editText.getText().toString());
        App.getDataBase().personalDao().update(list.get(id));
        adapter.notifyDataSetChanged();
    }

    private void init() {
        list = new ArrayList<>();
        adapter = new PersonalAdapter(this);
        recyclerView = findViewById(R.id.recycler_personal);
        recyclerView.setAdapter(adapter);
        editText = findViewById(R.id.editText_personal);
        addTask_image = findViewById(R.id.add_task_personal);
        imageMic = findViewById(R.id.mic_task_personal);
        editText = findViewById(R.id.editText_personal);
        changeTask_image = findViewById(R.id.change_task_personal);
        progressBar = findViewById(R.id.personal_progress_bar);
    }

    public void recordDataRoom() {
        if (editText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();
        } else {
            String personal = editText.getText().toString().trim();
            personalModel = new PersonalModel(personal, false);
            App.getDataBase().personalDao().insert(personalModel);
            editText.setText("");
        }
    }

    private void readDataFromFireStore() {
        AtomicBoolean isHasData = new AtomicBoolean(false);
        String booleanKey = "isDone";
        String personalTaskKey = "personalTask";
        if (user!=null) {
            progressBar.setVisibility(View.VISIBLE);
            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                if (task.getResult().getDocuments().size() == 0) {
                                    isHasData.set(false);
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    isHasData.set(true);
                                    Map<String, Object> dataFromFireBase;
                                    dataFromFireBase = document.getData();
                                    Boolean taskBoolean = (Boolean) dataFromFireBase.get(booleanKey);
                                    String personalTask = dataFromFireBase.get(personalTaskKey).toString();
                                    personalModel = new PersonalModel(personalTask, taskBoolean);
                                    App.getDataBase().personalDao().insert(personalModel);
                                }
                            }
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    public void initToolbar() {
        TextView toolbar = findViewById(R.id.toolbar_title);
        if (TaskDialogPreference.getPersonTitle().isEmpty()) {
            toolbar.setText(R.string.personal);
        } else {
            toolbar.setText(TaskDialogPreference.getPersonTitle());
        }
        if (user != null) {
            collectionName = toolbar.getText().toString() + "-" + "(" + user.getDisplayName() + ")" + user.getUid();
        }
    }

    @Override
    public void onItemCheckClick(int id) {
        personalModel = list.get(id);
        if (!personalModel.isDone) {
            personalModel.isDone = true;
            incrementDone();
        } else {
            personalModel.isDone = false;
            decrementDone();
        }
        if (user != null) {
            FireStoreTools.writeOrUpdateDataByFireStore(personalModel.getPersonalTask(), collectionName, db, personalModel);
        }
        App.getDataBase().personalDao().update(list.get(id));
    }

    @Override
    public void onItemLongClick(int pos) {
        if (addTask_image.getVisibility() == View.VISIBLE) {
            addTask_image.setVisibility(View.GONE);
        }
        imageMic.setVisibility(View.GONE);
        changeTask_image.setVisibility(View.VISIBLE);
        personalModel = list.get(pos);
        oldDocumentName = personalModel.getPersonalTask();
        editText.setText(personalModel.getPersonalTask());
        position = pos;
        KeyboardHelper.openKeyboard(PersonalActivity.this);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }

    private void incrementDone() {
        int previousPersonalDone = PersonDoneSizePreference.getInstance(this).getPersonalSize();
        PersonDoneSizePreference.getInstance(this).savePersonalSize(previousPersonalDone + 1);
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

    private void setLevel(int size) {
        if (size < 26) {
            if (size % 5 == 0) {
                int lvl = size / 5;
                String level = getString(R.string.attaboy) + lvl;
                addToLocalDate(lvl, level);
                showDialogLevel(level);
            }
        } else if (size > 26 && size < 51) {
            if (size % 5 == 0) {
                int lev = size / 5;
                String level = getString(R.string.Persistent) + lev;
                addToLocalDate(lev, level);
                showDialogLevel(level);
            }
        } else if (size > 51 && size < 76) {
            if (size % 5 == 0) {
                int lev = size / 5;
                String level = getString(R.string.Overwhelming) + lev;
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
                .setMessage(getString(R.string.you_got) + " " + l)
                .setPositiveButton(getString(R.string.apply), (dialog, id) -> {
                    dialog.cancel();
                });
        builder.create();
        builder.show();
    }

    private void decrementDone() {
        int currentData = PersonDoneSizePreference.getInstance(this).getPersonalSize();
        int updateData = currentData - 1;
        PersonDoneSizePreference.getInstance(this).savePersonalSize(updateData);
        decrementAllDone();
    }

    public void micPersonalTask(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_something));
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}