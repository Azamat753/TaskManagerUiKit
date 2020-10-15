package com.lawlett.taskmanageruikit.tasksPage.addTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.tasksPage.addTask.adapter.DoneAdapter;
import com.lawlett.taskmanageruikit.tasksPage.data.model.DoneModel;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DoneDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DoneActivity extends AppCompatActivity implements DoneAdapter.IMCheckedListener {
    DoneAdapter adapter;
    List<DoneModel> list;
    DoneModel doneModel;
    EditText editText;
    int pos, previousData, currentData, updateData;
    ImageView doneBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);
        TaskDialogPreference.init(this);
        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(getResources().getColor(R.color.statusBarC));

        changeView();
        list = new ArrayList<>();
        adapter = new DoneAdapter(this);

        App.getDataBase().doneDao().getAllLive().observe(this, doneModels -> {
            if (doneModels != null) {
                list.clear();
                list.addAll(doneModels);
                adapter.updateList(list);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_done);
        recyclerView.setAdapter(adapter);

        editText = findViewById(R.id.editText_done);

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
                App.getDataBase().doneDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(DoneActivity.this);
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.to_delete)
                        .setNegativeButton(R.string.no, (dialog1, which) ->
                                dialog1.cancel())
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pos = viewHolder.getAdapterPosition();
                                doneModel = list.get(pos);
                                if (!doneModel.isDone) {
                                    App.getDataBase().doneDao().delete(list.get(pos));
                                } else {
                                    decrementDone();
                                    App.getDataBase().doneDao().update(list.get(pos));
                                    App.getDataBase().doneDao().delete(list.get(pos));
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(DoneActivity.this, "Удалено", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
                adapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(recyclerView);
        doneBack = findViewById(R.id.personal_back);
        doneBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void addDoneTask(View view) {
        recordDataRoom();
    }

    public void recordDataRoom() {
        if (editText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Пусто", Toast.LENGTH_SHORT).show();
        } else {
            doneModel = new DoneModel(editText.getText().toString().trim(), false);
            App.getDataBase().doneDao().insert(doneModel);
            editText.setText("");
        }
    }

    public void changeView() {
        TextView toolbar = findViewById(R.id.toolbar_title);
        toolbar.setText(TaskDialogPreference.getTitle());

    }
    @Override
    public void onItemCheckClick(int id) {
        doneModel = list.get(id);
        if (!doneModel.isDone) {
            doneModel.isDone = true;
            incrementDone();
        } else {
            doneModel.isDone = false;
            decrementDone();
        }
        App.getDataBase().doneDao().update(list.get(id));
    }

    private void incrementDone() {
        previousData = DoneDoneSizePreference.getInstance(this).getDataSize();
        DoneDoneSizePreference.getInstance(this).saveDataSize(previousData + 1);
    }

    private void decrementDone() {
        currentData = DoneDoneSizePreference.getInstance(this).getDataSize();
        updateData = currentData - 1;
        DoneDoneSizePreference.getInstance(this).saveDataSize(updateData);
    }


}