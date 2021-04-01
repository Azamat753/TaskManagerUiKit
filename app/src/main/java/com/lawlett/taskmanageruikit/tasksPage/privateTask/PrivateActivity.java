package com.lawlett.taskmanageruikit.tasksPage.privateTask;

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
import com.lawlett.taskmanageruikit.tasksPage.data.model.PrivateModel;
import com.lawlett.taskmanageruikit.tasksPage.data.model.WorkModel;
import com.lawlett.taskmanageruikit.tasksPage.privateTask.recycler.PrivateAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.KeyboardHelper;
import com.lawlett.taskmanageruikit.utils.PrivateDoneSizePreference;

import java.util.ArrayList;
import java.util.Collections;

public class PrivateActivity extends AppCompatActivity implements PrivateAdapter.IPCheckedListener, ActionForDialog {
    RecyclerView recyclerView;
    PrivateAdapter adapter;
    ArrayList<PrivateModel> list;
    EditText editText;
    PrivateModel privateModel;
    int pos, previousData, currentData, updateData, id;
    ImageView privateBack;
    KeyboardHelper keyboardHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private);

        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(getResources().getColor(R.color.statusBarC));

        changeView();

        list = new ArrayList<>();
        adapter = new PrivateAdapter(this);

        App.getDataBase().privateDao().getAllLive().observe(this, privateModels -> {
            if (privateModels != null) {
                list.clear();
                list.addAll(privateModels);
                Collections.sort(list, new java.util.Comparator<PrivateModel>() {
                    @Override
                    public int compare(PrivateModel privateModel, PrivateModel t1) {
                        return Boolean.compare(t1.isDone, privateModel.isDone);
                    }
                });
                Collections.reverse(list);
                adapter.updateList(list);
            }
        });

        recyclerView = findViewById(R.id.recycler_private);
        recyclerView.setAdapter(adapter);

        editText = findViewById(R.id.editText_private);

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
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pos = viewHolder.getAdapterPosition();
                                privateModel = list.get(pos);
                                if (!privateModel.isDone) {
                                    App.getDataBase().privateDao().delete(list.get(pos));
                                } else {
                                    decrementDone();
                                    App.getDataBase().privateDao().update(list.get(pos));
                                    App.getDataBase().privateDao().delete(list.get(pos));
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(PrivateActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
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

        privateBack = findViewById(R.id.personal_back);
        privateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.settings_for_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper dialogHelper = new DialogHelper();
                dialogHelper.myDialog(PrivateActivity.this, PrivateActivity.this);
            }
        });
    }

    public void addPrivateTask(View view) {
        recordRoom();
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
    }

    private void incrementDone() {
        previousData = PrivateDoneSizePreference.getInstance(this).getDataSize();
        PrivateDoneSizePreference.getInstance(this).saveDataSize(previousData + 1);
    }

    private void decrementDone() {
        currentData = PrivateDoneSizePreference.getInstance(this).getDataSize();
        updateData = currentData - 1;
        PrivateDoneSizePreference.getInstance(this).saveDataSize(updateData);
    }

    @Override
    public void pressOk() {
        App.getDataBase().privateDao().deleteAll(list);
        PrivateDoneSizePreference.getInstance(PrivateActivity.this).clearSettings();
    }

    @Override
    public void onItemLongClick(int id) {
        findViewById(R.id.add_task_private).setVisibility(View.GONE);
        findViewById(R.id.change_task_private).setVisibility(View.VISIBLE);
        privateModel = list.get(id);
        editText.setText(privateModel.getPrivateTask());
        this.id=id;
        keyboardHelper.openKeyboard(PrivateActivity.this);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }

    public void addPrivateChangeTask(View view) {
        if(editText.getText().toString().trim().isEmpty()){
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();

        }else {
            updatePrivateTask(id);
            findViewById(R.id.change_task_private).setVisibility(View.GONE);
            findViewById(R.id.add_task_private).setVisibility(View.VISIBLE);
            editText.getText().clear();
            keyboardHelper.hideKeyboard(PrivateActivity.this, view);
        }
    }

    private void updatePrivateTask(int id) {
        privateModel = list.get(id);
        privateModel.setPrivateTask(editText.getText().toString());
        App.getDataBase().privateDao().update(list.get(id));
        adapter.notifyDataSetChanged();
    }
}