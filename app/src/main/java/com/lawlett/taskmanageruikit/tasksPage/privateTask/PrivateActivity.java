package com.lawlett.taskmanageruikit.tasksPage.privateTask;

import android.annotation.SuppressLint;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.achievement.models.LevelModel;
import com.lawlett.taskmanageruikit.tasksPage.data.model.DoneModel;
import com.lawlett.taskmanageruikit.tasksPage.data.model.PrivateModel;
import com.lawlett.taskmanageruikit.tasksPage.homeTask.HomeActivity;
import com.lawlett.taskmanageruikit.tasksPage.privateTask.recycler.PrivateAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.DoneTasksPreferences;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.KeyboardHelper;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;
import com.lawlett.taskmanageruikit.utils.PrivateDoneSizePreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrivateActivity extends AppCompatActivity implements PrivateAdapter.IPCheckedListener, ActionForDialog {
    RecyclerView recyclerView;
    PrivateAdapter adapter;
    ArrayList<PrivateModel> list;
    EditText editText;
    PrivateModel privateModel;
    int pos, previousData, currentData, updateData;
    ImageView privateBack, imageAdd, imageMic,changeTask_image;
    boolean isButton = false;
    private static final int REQUEST_CODE_SPEECH_INPUT = 22;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String collectionName;
    private ProgressBar progressBar;
    String oldDocumentName;
    DialogHelper dialogHelper = new DialogHelper();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private);
        init();
        initClickers();
        changeView();
        initListFromRoom();
        initItemTouchHelper();
        editListener();
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
                App.getDataBase().privateDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                PlannerDialog.showPlannerDialog(PrivateActivity.this, getString(R.string.you_sure_delete),new PlannerDialog.PlannerDialogClick() {
                    @Override
                    public void clickOnYes() {
                        pos = viewHolder.getAdapterPosition();
                        privateModel = list.get(pos);
                        if (!privateModel.isDone) {
                            App.getDataBase().privateDao().delete(list.get(pos));
                        } else {
                            decrementDone();
                            App.getDataBase().privateDao().update(list.get(pos));
                            App.getDataBase().privateDao().delete(list.get(pos));
                            FireStoreTools.deleteDataByFireStore(privateModel.getPrivateTask(),collectionName, db);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(PrivateActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
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

    private void initClickers() {
        privateBack.setOnClickListener(v -> onBackPressed());
        findViewById(R.id.settings_for_task).setOnClickListener((View.OnClickListener) v -> {
            DialogHelper dialogHelper = new DialogHelper();
            dialogHelper.myDialog(PrivateActivity.this, (ActionForDialog) PrivateActivity.this);
        });
        imageAdd.setOnClickListener(v -> {
            recordRoom();
            if (user!=null){
                FireStoreTools.writeOrUpdateDataByFireStore(privateModel.getPrivateTask(), getString(R.string.privates), db, privateModel);
            }
        });
    }

    private void initListFromRoom() {
        App.getDataBase().privateDao().getAllLive().observe(this, privateModels -> {
            if (privateModels != null) {
                checkOnShowProgressBar();
                list.clear();
                list.addAll(privateModels);
                Collections.sort(list, (privateModel, t1) -> Boolean.compare(t1.isDone, privateModel.isDone));
                Collections.reverse(list);
                adapter.updateList(list);
            } else {
                readDataFromFireStore(true);
            }
        });
    }
    private void checkOnShowProgressBar() {
        if (readDataFromFireStore(false).get()) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private AtomicBoolean readDataFromFireStore(boolean isRead) {
        AtomicBoolean isHasData = new AtomicBoolean(false);
        if (isRead) {
            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                if (task.getResult().getDocuments().size() == 0) {
                                    isHasData.set(false);
                                } else {
                                    isHasData.set(true);
                                }
                                Map<String, Object> dataFromFireBase;
                                dataFromFireBase = document.getData();
                                Boolean taskBoolean = (Boolean) dataFromFireBase.get("isDone");
                                String privateTask = dataFromFireBase.get("privateTask").toString();
                                privateModel = new PrivateModel(privateTask, taskBoolean);
                                App.getDataBase().privateDao().insert(privateModel);
                            }
                            progressBar.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });
        }
        return isHasData;
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

    private void init() {
        list = new ArrayList<>();
        privateBack = findViewById(R.id.personal_back);
        privateBack.setOnClickListener(v -> onBackPressed());
        editText = findViewById(R.id.editText_private);
        recyclerView = findViewById(R.id.recycler_private);
        adapter = new PrivateAdapter(this);
        recyclerView.setAdapter(adapter);
        editText = findViewById(R.id.editText_private);
        imageAdd = findViewById(R.id.add_task_private);
        imageMic = findViewById(R.id.mic_task_private);
    }

    public void recordRoom() {
        if (editText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();
        } else {
            privateModel = new PrivateModel(editText.getText().toString().trim(), false);
            App.getDataBase().privateDao().insert(privateModel);
            editText.setText("");
        }
    }

    public void changeView() {
        TextView toolbar = findViewById(R.id.toolbar_title);
        toolbar.setText(R.string.privates);
        collectionName = toolbar.getText().toString()+ "-" + "(" + user.getDisplayName() + ")" + user.getUid();
    }

    @Override
    public void onItemCheckClick(int id) {
        privateModel = list.get(id);
        if (!privateModel.isDone) {
            privateModel.isDone = true;
            incrementDone();
        } else {
            privateModel.isDone = false;
            decrementDone();
        }
        App.getDataBase().privateDao().update(list.get(id));
        FireStoreTools.writeOrUpdateDataByFireStore(privateModel.getPrivateTask(), getString(R.string.privates), db, privateModel);
    }

    @Override
    public void onItemLongClick(int pos) {
        if (imageAdd.getVisibility() == View.VISIBLE) {
            imageAdd.setVisibility(View.GONE);
        }
        imageMic.setVisibility(View.GONE);
        changeTask_image.setVisibility(View.VISIBLE);
        privateModel = list.get(pos);
        oldDocumentName = privateModel.getPrivateTask();
        editText.setText(privateModel.getPrivateTask());
        this.pos = pos;
        KeyboardHelper.openKeyboard(PrivateActivity.this);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
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
        previousData = PrivateDoneSizePreference.getInstance(this).getDataSize();
        PrivateDoneSizePreference.getInstance(this).saveDataSize(previousData + 1);
        incrementAllDone();
    }

    private void decrementDone() {
        currentData = PrivateDoneSizePreference.getInstance(this).getDataSize();
        updateData = currentData - 1;
        PrivateDoneSizePreference.getInstance(this).saveDataSize(updateData);
        decrementAllDone();
    }

    @Override
    public void pressOk() {
        App.getDataBase().privateDao().deleteAll(list);
        PrivateDoneSizePreference.getInstance(PrivateActivity.this).clearSettings();
    }

    public void micPrivateTask(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_something));
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            e.printStackTrace();
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