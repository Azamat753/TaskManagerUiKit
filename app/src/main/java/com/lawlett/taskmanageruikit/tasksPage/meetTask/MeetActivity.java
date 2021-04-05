package com.lawlett.taskmanageruikit.tasksPage.meetTask;

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
import com.lawlett.taskmanageruikit.tasksPage.data.model.MeetModel;
import com.lawlett.taskmanageruikit.tasksPage.meetTask.recyclerview.MeetAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.DoneTasksPreferences;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.MeetDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MeetActivity extends AppCompatActivity implements MeetAdapter.IMCheckedListener, ActionForDialog {
    private RecyclerView recyclerView;
    private MeetAdapter adapter;
    private List<MeetModel> list;
    private EditText editText;
    private MeetModel meetModel;
    private int position;
    private ImageView meetBack, imageMic, imageAdd;
    private static final int REQUEST_CODE_SPEECH_INPUT = 22;
    private boolean isAddBtn = false;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);
        init();
        initClickers();
        App.setNavBarColor(this);
        changeView();
        initListFromRoom();
        initItemTouchHelper();
        editListener();
    }

    private void initClickers() {
        imageAdd.setOnClickListener(view -> {
            recordRoom();
            writeDataOrUpdateToFireStore();
        });
        meetBack.setOnClickListener(v -> onBackPressed());
        findViewById(R.id.settings_for_task).setOnClickListener((View.OnClickListener) v -> {
            DialogHelper dialogHelper = new DialogHelper();
            dialogHelper.myDialog(MeetActivity.this, (ActionForDialog) MeetActivity.this);
        });
    }

    private void writeDataOrUpdateToFireStore() {
        db.collection(getString(R.string.meets)).add(meetModel).addOnSuccessListener(documentReference -> userId = documentReference.getId());
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
                App.getDataBase().meetDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MeetActivity.this);
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.to_delete)
                        .setNegativeButton(R.string.no, (dialog1, which) -> {
                            adapter.notifyDataSetChanged();
                            dialog1.cancel();
                        })
                        .setPositiveButton(R.string.yes, (dialog12, which) -> {
                            position = viewHolder.getAdapterPosition();
                            meetModel = list.get(position);
                            if (!meetModel.isDone) {
                                App.getDataBase().meetDao().delete(list.get(position));
                            } else {
                                decrementDone();

                                App.getDataBase().meetDao().update(list.get(position));
                                App.getDataBase().meetDao().delete(list.get(position));
                                FireStoreTools.deleteDataByFireStore(userId, getString(R.string.meets), db);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(MeetActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
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

    private void initListFromRoom() {
        list = new ArrayList<>();
        App.getDataBase().meetDao().getAllLive().observe(this, meetModels -> {
            if (meetModels != null) {
                list.clear();
                list.addAll(meetModels);
                Collections.sort(list, (meetModel, t1) -> Boolean.compare(t1.isDone, meetModel.isDone));
                Collections.reverse(list);
                adapter.updateList(list);
            } else {
                FireStoreTools.readDataFromFireStore(db, getString(R.string.meets));
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
                if (charSequence != null && !isAddBtn && !editText.getText().toString().trim().isEmpty()) {
                    imageMic.setVisibility(View.INVISIBLE);
                    imageAdd.setVisibility(View.VISIBLE);
                    isAddBtn = true;
                }
                if (editText.getText().toString().isEmpty() && isAddBtn) {
                    imageAdd.setVisibility(View.INVISIBLE);
                    imageMic.setVisibility(View.VISIBLE);
                    isAddBtn = false;
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    private void init() {
        list = new ArrayList<>();
        adapter = new MeetAdapter(this);
        recyclerView = findViewById(R.id.recycler_meet);
        recyclerView.setAdapter(adapter);
        editText = findViewById(R.id.editText_meet);
        meetBack = findViewById(R.id.personal_back);
        meetBack.setOnClickListener(v -> onBackPressed());
        imageAdd = findViewById(R.id.add_task_meet);
        imageMic = findViewById(R.id.mic_task_meet);
    }

    public void recordRoom() {
        if (editText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();
        } else {
            meetModel = new MeetModel(editText.getText().toString().trim(), false);
            App.getDataBase().meetDao().insert(meetModel);
            editText.setText("");
        }
    }

    public void changeView() {
        TextView toolbar = findViewById(R.id.toolbar_title);
        if (TaskDialogPreference.getMeetTitle().isEmpty()) {
            toolbar.setText(R.string.meets);
        } else {
            toolbar.setText(TaskDialogPreference.getMeetTitle());
        }
    }

    @Override
    public void onItemCheckClick(int id) {
        meetModel = list.get(id);
        if (!meetModel.isDone) {
            meetModel.isDone = true;
            incrementDone();
        } else {
            meetModel.isDone = false;
            decrementDone();
        }
        App.getDataBase().meetDao().update(list.get(id));
        FireStoreTools.updateDataByFireStore(userId, getString(R.string.meets), db, meetModel);
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
                    // Закрываем окно
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
        int previousData = MeetDoneSizePreference.getInstance(this).getDataSize();
        MeetDoneSizePreference.getInstance(this).saveDataSize(previousData + 1);
        incrementAllDone();
    }

    private void decrementDone() {
        int currentData = MeetDoneSizePreference.getInstance(this).getDataSize();
        int updateData = currentData - 1;
        MeetDoneSizePreference.getInstance(this).saveDataSize(updateData);
        decrementAllDone();
    }

    @Override
    public void pressOk() {
        App.getDataBase().meetDao().deleteAll(list);
        MeetDoneSizePreference.getInstance(MeetActivity.this).clearSettings();
    }

    public void micMeetTask(View view) {
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
}
