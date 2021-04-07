package com.lawlett.taskmanageruikit.tasksPage.workTask;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.lawlett.taskmanageruikit.tasksPage.data.model.WorkModel;
import com.lawlett.taskmanageruikit.tasksPage.workTask.recycler.WorkAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.DoneTasksPreferences;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;
import com.lawlett.taskmanageruikit.utils.WorkDoneSizePreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class WorkActivity extends AppCompatActivity implements WorkAdapter.IWCheckedListener, ActionForDialog {
    WorkAdapter adapter;
    EditText editText;
    WorkModel workModel;
    List<WorkModel> list;
    private static final int REQUEST_CODE_SPEECH_INPUT = 22;
    int pos, previousData, currentData, updateData;
    RecyclerView recyclerView;
    ImageView workBack, imageAdd, imageMic;
    boolean knopka = false;
    Animation animationAlpha;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userId;
    String collectionName;
    FirebaseAuth mAuth= FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);
        init();
        initClickers();
        App.setNavBarColor(this);
        initToolbar();
        initListFromRoom();
        initItemTouchHelper();
        editListener();
    }

    private void initClickers() {
        workBack.setOnClickListener(v -> onBackPressed());
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
                App.getDataBase().workDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                PlannerDialog.deletion(WorkActivity.this, new PlannerDialog.PlannerDialogClick() {
                    @Override
                    public void clickOnYes() {
                        pos = viewHolder.getAdapterPosition();
                        workModel = list.get(pos);
                        if (!workModel.isDone) {
                            App.getDataBase().workDao().delete(list.get(pos));
                        } else {
                            decrementDone();
                            App.getDataBase().workDao().update(list.get(pos));
                            App.getDataBase().workDao().delete(list.get(pos));
                            FireStoreTools.deleteDataByFireStore(userId, getString(R.string.work), db);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(WorkActivity.this, "Удалено", Toast.LENGTH_SHORT).show();
                        }
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

    private void initListFromRoom() {
        list = new ArrayList<>();
        App.getDataBase().workDao().getAllLive().observe(this, workModels -> {
            if (workModels != null) {
                list.clear();
                list.addAll(workModels);
                Collections.reverse(list);
                adapter.updateList(list);
            } else {
              readDataFromFireStore();
            }
        });
    }
    private void readDataFromFireStore() {
        db.collection(collectionName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Map<String, Object> dataFromFireBase;
                            dataFromFireBase = document.getData();
                            Boolean workBoolean = (Boolean) dataFromFireBase.get("isDone");
                            String workTask = dataFromFireBase.get("workTask").toString();
                            workModel = new WorkModel(workTask, workBoolean);
                            App.getDataBase().workDao().insert(workModel);
                        }
                    } else {
                        Log.w("ololo", "Error getting documents.", task.getException());
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
                if (charSequence != null && !knopka && !editText.getText().toString().trim().isEmpty()) {
                    imageMic.setVisibility(View.INVISIBLE);
                    imageAdd.setVisibility(View.VISIBLE);
                    knopka = true;
                }
                if (editText.getText().toString().isEmpty() && knopka) {
                    imageAdd.setVisibility(View.INVISIBLE);
                    imageMic.setVisibility(View.VISIBLE);
                    knopka = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        findViewById(R.id.settings_for_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper dialogHelper = new DialogHelper();
                dialogHelper.myDialog(WorkActivity.this, (ActionForDialog) WorkActivity.this);
            }
        });

    }

    private void init() {
        list = new ArrayList<>();
        adapter = new WorkAdapter(this);
        recyclerView = findViewById(R.id.recycler_work);
        recyclerView.setAdapter(adapter);
        editText = findViewById(R.id.editText_work);
        editText = findViewById(R.id.editText_work);
        imageAdd = findViewById(R.id.add_task_work);
        imageMic = findViewById(R.id.mic_task_done);
        workBack = findViewById(R.id.personal_back);
        animationAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
    }

    public void addWorkTask(View view) {
        recordDataRoom();
        FireStoreTools.writeOrUpdateDataByFireStore(workModel.getWorkTask(), getString(R.string.work), db, workModel);
    }

    public void recordDataRoom() {
        if (editText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.empty), Toast.LENGTH_SHORT).show();
        } else {
            workModel = new WorkModel(editText.getText().toString().trim(), false);
            App.getDataBase().workDao().insert(workModel);
            editText.getText().clear();
        }
    }

    public void initToolbar() {
        TextView toolbar = findViewById(R.id.toolbar_title);
        if (TaskDialogPreference.getWorkTitle().isEmpty()) {
            toolbar.setText(R.string.work);
        } else {
            toolbar.setText(TaskDialogPreference.getWorkTitle());
        }
        collectionName=toolbar.getText().toString();
    }

    @Override
    public void onItemCheckClick(int id) {
        workModel = list.get(id);
        if (!workModel.isDone) {
            workModel.isDone = true;
            incrementDone();
        } else {
            workModel.isDone = false;
            decrementDone();
        }
        App.getDataBase().workDao().update(list.get(id));
        FireStoreTools.writeOrUpdateDataByFireStore(workModel.getWorkTask(), getString(R.string.work), db, workModel);
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
                .setMessage(getString(R.string.you_got) + l)
                .setPositiveButton(getString(R.string.apply), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Закрываем окно
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
        previousData = WorkDoneSizePreference.getInstance(this).getDataSize();
        WorkDoneSizePreference.getInstance(this).saveDataSize(previousData + 1);
        incrementAllDone();
    }

    private void decrementDone() {
        currentData = WorkDoneSizePreference.getInstance(this).getDataSize();
        updateData = currentData - 1;
        WorkDoneSizePreference.getInstance(this).saveDataSize(updateData);
        decrementAllDone();
    }

    public void micWorkTask(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak something");
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

    @Override
    public void pressOk() {
        App.getDataBase().workDao().deleteAll(list);
        WorkDoneSizePreference.getInstance(WorkActivity.this).clearSettings();
    }
}

