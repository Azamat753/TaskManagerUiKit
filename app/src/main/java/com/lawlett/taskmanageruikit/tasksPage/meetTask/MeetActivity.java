package com.lawlett.taskmanageruikit.tasksPage.meetTask;

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
import com.lawlett.taskmanageruikit.tasksPage.data.model.MeetModel;
import com.lawlett.taskmanageruikit.tasksPage.data.model.WorkModel;
import com.lawlett.taskmanageruikit.tasksPage.meetTask.recyclerview.MeetAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.KeyboardHelper;
import com.lawlett.taskmanageruikit.utils.MeetDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeetActivity extends AppCompatActivity implements MeetAdapter.IMCheckedListener, ActionForDialog {
    RecyclerView recyclerView;
    MeetAdapter adapter;
    private List<MeetModel> list;
    EditText editText;
    MeetModel meetModel;
    int position, currentData, updateData, previousData, id;
    ImageView meetBack;
    KeyboardHelper keyboardHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);
        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(getResources().getColor(R.color.statusBarC));

        changeView();

        list = new ArrayList<>();
        adapter = new MeetAdapter(this);

        App.getDataBase().meetDao().getAllLive().observe(this, meetModels -> {
            if (meetModels != null) {
                list.clear();
                list.addAll(meetModels);
                Collections.sort(list, new java.util.Comparator<MeetModel>() {
                    @Override
                    public int compare(MeetModel meetModel, MeetModel t1) {
                        return Boolean.compare(t1.isDone, meetModel.isDone);
                    }
                });
                Collections.reverse(list);
                adapter.updateList(list);
            }
        });


        recyclerView = findViewById(R.id.recycler_meet);
        recyclerView.setAdapter(adapter);
        editText = findViewById(R.id.editText_meet);


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
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                position = viewHolder.getAdapterPosition();
                                meetModel = list.get(position);
                                if (!meetModel.isDone) {
                                    App.getDataBase().meetDao().delete(list.get(position));
                                } else {
                                    decrementDone();

                                    App.getDataBase().meetDao().update(list.get(position));
                                    App.getDataBase().meetDao().delete(list.get(position));
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(MeetActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
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

        meetBack = findViewById(R.id.personal_back);
        meetBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.settings_for_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper dialogHelper = new DialogHelper();
                dialogHelper.myDialog(MeetActivity.this, MeetActivity.this);
            }
        });
    }

    public void addMeetTask(View view) {
        recordRoom();
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
        if(TaskDialogPreference.getMeetTitle().isEmpty()){
            toolbar.setText(R.string.meets);
        }else{
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
    }

    private void incrementDone() {
        previousData = MeetDoneSizePreference.getInstance(this).getDataSize();
        MeetDoneSizePreference.getInstance(this).saveDataSize(previousData + 1);
    }

    private void decrementDone() {
        currentData = MeetDoneSizePreference.getInstance(this).getDataSize();
        updateData = currentData - 1;
        MeetDoneSizePreference.getInstance(this).saveDataSize(updateData);
    }

    @Override
    public void pressOk() {
        App.getDataBase().meetDao().deleteAll(list);
        MeetDoneSizePreference.getInstance(MeetActivity.this).clearSettings();
    }

    @Override
    public void onItemLongClick(int id) {
        findViewById(R.id.add_task_meet).setVisibility(View.GONE);
        findViewById(R.id.change_task_meet).setVisibility(View.VISIBLE);
        meetModel = list.get(id);
        editText.setText(meetModel.getMeetTask());
        this.id=id;
        keyboardHelper.openKeyboard(MeetActivity.this);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }

    public void addMeetChangeTask(View view) {
        if(editText.getText().toString().trim().isEmpty()){
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();

        }else {
            updateMeetTask(id);
            findViewById(R.id.change_task_meet).setVisibility(View.GONE);
            findViewById(R.id.add_task_meet).setVisibility(View.VISIBLE);
            editText.getText().clear();
            keyboardHelper.hideKeyboard(MeetActivity.this, view);
        }
    }

    private void updateMeetTask(int id) {
        meetModel = list.get(id);
        meetModel.setMeetTask(editText.getText().toString());
        App.getDataBase().meetDao().update(list.get(id));
        adapter.notifyDataSetChanged();
    }
}
