package com.lawlett.taskmanageruikit.tasksPage.personalTask;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.tasksPage.data.model.PersonalModel;
import com.lawlett.taskmanageruikit.tasksPage.data.model.WorkModel;
import com.lawlett.taskmanageruikit.tasksPage.personalTask.recyclerview.PersonalAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.KeyboardHelper;
import com.lawlett.taskmanageruikit.utils.PersonDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersonalActivity extends AppCompatActivity implements PersonalAdapter.ICheckedListener, ActionForDialog {
    EditText editText;
    PersonalAdapter adapter;
    PersonalModel personalModel;
    List<PersonalModel> list;
    String personal;
    ImageView personalBack;
    int pos, previousPersonalDone, currentData, updateData, id;
    KeyboardHelper keyboardHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
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
                }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                final int DIRECTION_RIGHT = 1;
                final int DIRECTION_LEFT = 0;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive){
                    int direction = dX > 0? DIRECTION_RIGHT : DIRECTION_LEFT;
                    int absoluteDisplacement = Math.abs((int)dX);

                    switch (direction){

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
                dialogHelper.myDialog(PersonalActivity.this, PersonalActivity.this);
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
        if(TaskDialogPreference.getPersonTitle().isEmpty()){
            toolbar.setText(R.string.personal);
        }else{
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
    }

    private void decrementDone() {
        currentData = PersonDoneSizePreference.getInstance(this).getPersonalSize();
        updateData = currentData - 1;
        PersonDoneSizePreference.getInstance(this).savePersonalSize(updateData);
    }

    @Override
    public void pressOk() {
        App.getDataBase().personalDao().deleteAll(list);
        PersonDoneSizePreference.getInstance(PersonalActivity.this).clearSettings();
    }

    @Override
    public void onItemLongClick(int id) {
        findViewById(R.id.add_task_personal).setVisibility(View.GONE);
        findViewById(R.id.change_task_personal).setVisibility(View.VISIBLE);
        personalModel = list.get(id);
        editText.setText(personalModel.getPersonalTask());
        this.id=id;
        keyboardHelper.openKeyboard(PersonalActivity.this);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }

    public void addPersonalChangeTask(View view) {
        if(editText.getText().toString().trim().isEmpty()){
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();

        }else {
            updatePersonalTask(id);
            findViewById(R.id.change_task_personal).setVisibility(View.GONE);
            findViewById(R.id.add_task_personal).setVisibility(View.VISIBLE);
            editText.getText().clear();
            keyboardHelper.hideKeyboard(PersonalActivity.this, view);
        }
    }

    private void updatePersonalTask(int id) {
        personalModel = list.get(id);
        personalModel.setPersonalTask(editText.getText().toString());
        App.getDataBase().personalDao().update(list.get(id));
        adapter.notifyDataSetChanged();
    }
}