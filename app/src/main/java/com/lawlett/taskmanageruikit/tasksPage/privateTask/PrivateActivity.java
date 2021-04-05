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
import android.view.View;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.achievement.models.LevelModel;
import com.lawlett.taskmanageruikit.tasksPage.data.model.PrivateModel;
import com.lawlett.taskmanageruikit.tasksPage.privateTask.recycler.PrivateAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.DoneTasksPreferences;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.PrivateDoneSizePreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class PrivateActivity extends AppCompatActivity implements PrivateAdapter.IPCheckedListener, ActionForDialog {
    RecyclerView recyclerView;
    PrivateAdapter adapter;
    ArrayList<PrivateModel> list;
    EditText editText;
    PrivateModel privateModel;
    int pos, previousData, currentData, updateData;
    ImageView privateBack, imageAdd, imageMic;
    boolean knopka = false;
    private static final int REQUEST_CODE_SPEECH_INPUT = 22;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private);
        init();
        initClickers();
        App.setNavBarColor(this);
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(PrivateActivity.this);
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.to_delete)
                        .setNegativeButton(R.string.no, (dialog1, which) -> {
                            adapter.notifyDataSetChanged();
                            dialog1.cancel();
                        })
                        .setPositiveButton(R.string.yes, (dialog12, which) -> {
                            pos = viewHolder.getAdapterPosition();
                            privateModel = list.get(pos);
                            if (!privateModel.isDone) {
                                App.getDataBase().privateDao().delete(list.get(pos));
                            } else {
                                decrementDone();
                                App.getDataBase().privateDao().update(list.get(pos));
                                App.getDataBase().privateDao().delete(list.get(pos));
                                FireStoreTools.deleteDataByFireStore(userId, getString(R.string.privates), db);
                                adapter.notifyDataSetChanged();

                                Toast.makeText(PrivateActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
                            }
                        }).show();
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
    }

    private void initListFromRoom() {
        App.getDataBase().privateDao().getAllLive().observe(this, privateModels -> {
            if (privateModels != null) {
                list.clear();
                list.addAll(privateModels);
                Collections.sort(list, (privateModel, t1) -> Boolean.compare(t1.isDone, privateModel.isDone));
                Collections.reverse(list);
                adapter.updateList(list);
            } else {
                FireStoreTools.readDataFromFireStore(db, getString(R.string.privates));
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

    public void addPrivateTask(View view) {
        recordRoom();
        writeDataOrUpdateToFireStore();
    }

    private void writeDataOrUpdateToFireStore() {
        db.collection(getString(R.string.privates)).add(privateModel).addOnSuccessListener(documentReference -> userId = documentReference.getId());
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
        FireStoreTools.updateDataByFireStore(userId, getString(R.string.privates), db, privateModel);
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