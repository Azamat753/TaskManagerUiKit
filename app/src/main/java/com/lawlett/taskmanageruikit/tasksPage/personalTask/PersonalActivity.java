package com.lawlett.taskmanageruikit.tasksPage.personalTask;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.achievement.models.LevelModel;
import com.lawlett.taskmanageruikit.tasksPage.data.model.PersonalModel;
import com.lawlett.taskmanageruikit.tasksPage.data.model.WorkModel;
import com.lawlett.taskmanageruikit.tasksPage.personalTask.recyclerview.PersonalAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.DoneTasksPreferences;
import com.lawlett.taskmanageruikit.utils.PersonDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PersonalActivity extends AppCompatActivity implements PersonalAdapter.ICheckedListener, ActionForDialog {
    EditText editText;
    PersonalAdapter adapter;
    PersonalModel personalModel;
    List<PersonalModel> list;
    String personal;
    ImageView personalBack, imageAdd, imageMic;
    RecyclerView recyclerView;
    int pos, previousPersonalDone, currentData, updateData;
    private static final int REQUEST_CODE_SPEECH_INPUT = 22;
    boolean knopka = false;
    Animation animationAlpha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        init();
        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(getResources().getColor(R.color.statusBarC));

        changeView();
        list = new ArrayList<>();
        adapter = new PersonalAdapter(this);

        App.getDataBase().personalDao().getAllLive().observe(this, personalModels -> {
            if (personalModels != null) {
                list.clear();
                list.addAll(personalModels);
                Collections.sort(list, new java.util.Comparator<PersonalModel>() {
                    @Override
                    public int compare(PersonalModel personalModel, PersonalModel t1) {
                        return Boolean.compare(t1.isDone, personalModel.isDone);
                    }
                });
                Collections.reverse(list);
                adapter.updateList(list);
            }
        });


        RecyclerView recyclerView = findViewById(R.id.recycler_personal);
        recyclerView.setAdapter(adapter);

        editText = findViewById(R.id.editText_personal);


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
                AlertDialog.Builder dialog = new AlertDialog.Builder(PersonalActivity.this);
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.to_delete)
                        .setNegativeButton(R.string.no, (dialog1, which) -> {
                            dialog1.cancel();
                            adapter.notifyDataSetChanged();
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pos = viewHolder.getAdapterPosition();
                                personalModel = list.get(pos);
                                if (!personalModel.isDone) {
                                    App.getDataBase().personalDao().delete(list.get(pos));
                                } else {
                                    decrementDone();
                                    App.getDataBase().personalDao().update(list.get(pos));
                                    App.getDataBase().personalDao().delete(list.get(pos));
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(PersonalActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
                                }
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
                    int absoluteDisplacement = Math.abs((int) dX);

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


        personalBack.setOnClickListener(v -> onBackPressed());

        editListener();
    }

    private void editListener() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null && !knopka && !editText.getText().toString().trim().isEmpty()) {
//                    imageAdd.startAnimation(animationAlpha);
                    imageMic.setVisibility(View.INVISIBLE);
                    imageAdd.setVisibility(View.VISIBLE);
                    knopka = true;
                }
                if (editText.getText().toString().isEmpty() && knopka) {
//                    imageMic.startAnimation(animationAlpha);
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
        adapter = new PersonalAdapter(this);
        animationAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        recyclerView = findViewById(R.id.recycler_personal);
        recyclerView.setAdapter(adapter);
        editText = findViewById(R.id.editText_personal);
        imageAdd = findViewById(R.id.add_task_personal);
        imageMic = findViewById(R.id.mic_task_personal);
        personalBack = findViewById(R.id.personal_back);
        personalBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.settings_for_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper dialogHelper = new DialogHelper();
                dialogHelper.myDialog(PersonalActivity.this, (ActionForDialog) PersonalActivity.this);
            }
        });
    }

    public void addPersonalTask(View view) {
        recordDataRoom();
    }

    public void recordDataRoom() {
        if (editText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();
        } else {
            personal = editText.getText().toString().trim();
            personalModel = new PersonalModel(personal, false);
            App.getDataBase().personalDao().insert(personalModel);
            editText.setText("");
        }
    }

    public void changeView() {
        TextView toolbar = findViewById(R.id.toolbar_title);
        if (TaskDialogPreference.getPersonTitle().isEmpty()) {
            toolbar.setText(R.string.personal);
        } else {
            toolbar.setText(TaskDialogPreference.getPersonTitle());
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
        App.getDataBase().personalDao().update(list.get(id));
    }

    private void incrementDone() {
        previousPersonalDone = PersonDoneSizePreference.getInstance(this).getPersonalSize();
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

    private void decrementDone() {
        currentData = PersonDoneSizePreference.getInstance(this).getPersonalSize();
        updateData = currentData - 1;
        PersonDoneSizePreference.getInstance(this).savePersonalSize(updateData);
        decrementAllDone();
    }

    @Override
    public void pressOk() {
        App.getDataBase().personalDao().deleteAll(list);
        PersonDoneSizePreference.getInstance(PersonalActivity.this).clearSettings();
    }

    public void micPersonalTask(View view) {
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