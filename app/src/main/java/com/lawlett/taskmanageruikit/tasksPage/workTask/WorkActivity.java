package com.lawlett.taskmanageruikit.tasksPage.workTask;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
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
import com.lawlett.taskmanageruikit.tasksPage.data.model.WorkModel;
import com.lawlett.taskmanageruikit.tasksPage.workTask.recycler.WorkAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.KeyboardHelper;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;
import com.lawlett.taskmanageruikit.utils.WorkDoneSizePreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WorkActivity extends AppCompatActivity implements WorkAdapter.IWCheckedListener, ActionForDialog {
    WorkAdapter adapter;
    EditText editText;
    WorkModel workModel;
    List<WorkModel> list;
    int pos, previousData, currentData, updateData, id;
    ImageView workBack;
    KeyboardHelper keyboardHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);
        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(getResources().getColor(R.color.statusBarC));

        changeView();

        list = new ArrayList<>();
        adapter = new WorkAdapter(this);

        App.getDataBase().workDao().getAllLive().observe(this, workModels -> {
            if (workModels != null) {
                list.clear();
                list.addAll(workModels);
                Collections.sort(list, new java.util.Comparator<WorkModel>() {
                    @Override
                    public int compare(WorkModel workModel, WorkModel t1) {
                        return Boolean.compare(t1.isDone, workModel.isDone);
                    }
                });
                Collections.reverse(list);
                adapter.updateList(list);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_work);
        recyclerView.setAdapter(adapter);

        editText = findViewById(R.id.editText_work);


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
                AlertDialog.Builder dialog = new AlertDialog.Builder(WorkActivity.this);
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.to_delete)
                        .setNegativeButton(R.string.no, (dialog1, which) -> {
                            adapter.notifyDataSetChanged();
                            dialog1.cancel();
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pos = viewHolder.getAdapterPosition();
                                workModel = list.get(pos);
                                if (!workModel.isDone) {
                                    App.getDataBase().workDao().delete(list.get(pos));
                                } else {
                                    decrementDone();
                                    App.getDataBase().workDao().update(list.get(pos));
                                    App.getDataBase().workDao().delete(list.get(pos));
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(WorkActivity.this, "Удалено", Toast.LENGTH_SHORT).show();
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

        workBack = findViewById(R.id.personal_back);
        workBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.settings_for_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper dialogHelper = new DialogHelper();
                dialogHelper.myDialog(WorkActivity.this, WorkActivity.this);
            }
        });
    }

    public void addWorkTask(View view) {
        recordDataRoom();
    }

    public void recordDataRoom() {
        if (editText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Пусто", Toast.LENGTH_SHORT).show();
        } else {
            workModel = new WorkModel(editText.getText().toString().trim(), false);
            App.getDataBase().workDao().insert(workModel);
            editText.setText("");
        }
    }

    public void changeView() {
        TextView toolbar = findViewById(R.id.toolbar_title);
        if(TaskDialogPreference.getWorkTitle().isEmpty()){
            toolbar.setText(R.string.work);
        }else{
        toolbar.setText(TaskDialogPreference.getWorkTitle());
        }

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
    }

    private void incrementDone() {
        previousData = WorkDoneSizePreference.getInstance(this).getDataSize();
        WorkDoneSizePreference.getInstance(this).saveDataSize(previousData + 1);
    }

    private void decrementDone() {
        currentData = WorkDoneSizePreference.getInstance(this).getDataSize();
        updateData = currentData - 1;
        WorkDoneSizePreference.getInstance(this).saveDataSize(updateData);
    }

    @Override
    public void pressOk() {
        App.getDataBase().workDao().deleteAll(list);
        WorkDoneSizePreference.getInstance(WorkActivity.this).clearSettings();
    }

    @Override
    public void onItemLongClick(int id) {
        findViewById(R.id.add_task_work).setVisibility(View.GONE);
        findViewById(R.id.change_task_work).setVisibility(View.VISIBLE);
        workModel = list.get(id);
        editText.setText(workModel.getWorkTask());
        this.id=id;
        keyboardHelper.openKeyboard(WorkActivity.this);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }

    public void addWorkChangeTask(View view) {
        if(editText.getText().toString().trim().isEmpty()){
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();

        }else {
            updateWorkTask(id);
            findViewById(R.id.change_task_work).setVisibility(View.GONE);
            findViewById(R.id.add_task_work).setVisibility(View.VISIBLE);
            editText.getText().clear();
            keyboardHelper.hideKeyboard(WorkActivity.this, view);
        }
    }

    private void updateWorkTask(int id) {
        workModel = list.get(id);
        workModel.setWorkTask(editText.getText().toString());
        App.getDataBase().workDao().update(list.get(id));
        adapter.notifyDataSetChanged();
    }

}

