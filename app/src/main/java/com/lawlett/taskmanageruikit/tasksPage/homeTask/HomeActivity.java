package com.lawlett.taskmanageruikit.tasksPage.homeTask;

import android.content.DialogInterface;
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
import com.lawlett.taskmanageruikit.tasksPage.data.model.HomeModel;
import com.lawlett.taskmanageruikit.tasksPage.homeTask.recycler.HomeAdapter;
import com.lawlett.taskmanageruikit.utils.ActionForDialog;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.HomeDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.KeyboardHelper;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements HomeAdapter.IHCheckedListener, ActionForDialog {
    RecyclerView recyclerView;
    HomeAdapter adapter;
    List<HomeModel> list;
    HomeModel homeModel;
    EditText editText;
    int pos, previousData, currentData, updateData,id;
    ImageView homeBack, homeSettings;
    DialogHelper dialogHelper = new DialogHelper();
    KeyboardHelper keyboardHelper;
    ImageView homeTaskCheck, homeTaskTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(getResources().getColor(R.color.statusBarC));

        changeView();

        list = new ArrayList<>();
        adapter = new HomeAdapter(this);

        App.getDataBase().homeDao().getAllLive().observe(this, homeModels -> {
            if (homeModels != null) {
                list.clear();
                list.addAll(homeModels);
                Collections.sort(list, new java.util.Comparator<HomeModel>() {
                    @Override
                    public int compare(HomeModel homeModel, HomeModel t1) {
                        return Boolean.compare(t1.isDone, homeModel.isDone);
                    }
                });
                Collections.reverse(list);
                adapter.updateList(list);
            }
        });


        recyclerView = findViewById(R.id.recycler_home);
        recyclerView.setAdapter(adapter);

        editText = findViewById(R.id.editText_home);

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
                App.getDataBase().homeDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.to_delete)
                        .setNegativeButton(R.string.no, (dialog1, which) -> {
                            adapter.notifyDataSetChanged();
                            dialog1.cancel();
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                pos = viewHolder.getAdapterPosition();
                                homeModel = list.get(pos);
                                if (!homeModel.isDone) {
                                    App.getDataBase().homeDao().delete(list.get(pos));
                                } else {
                                    decrementDone();
                                    App.getDataBase().homeDao().update(list.get(pos));
                                    App.getDataBase().homeDao().delete(list.get(pos));
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(HomeActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
            }
        }).attachToRecyclerView(recyclerView);

        homeBack = findViewById(R.id.personal_back);
        homeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        homeSettings = findViewById(R.id.settings_for_task);
        homeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogHelper.myDialogDeleteAll(HomeActivity.this, HomeActivity.this);
            }
        });
    }

    public void addHomeTask(View view) {
        recordRoom();
    }

    private void recordRoom() {
        if (editText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();
        } else {
            homeModel = new HomeModel(editText.getText().toString().trim(), false);
            App.getDataBase().homeDao().insert(homeModel);
            editText.setText("");
        }
    }

    public void changeView() {
        TextView toolbar = findViewById(R.id.toolbar_title);
        if(TaskDialogPreference.getHomeTitle().isEmpty()){
            toolbar.setText(R.string.home);
        }else{
            toolbar.setText(TaskDialogPreference.getHomeTitle());
        }

    }

    @Override
    public void onItemCheckClick(int id) {
        homeModel = list.get(id);
        if (!homeModel.isDone) {
            homeModel.isDone = true;
            incrementDone();
        } else {
            homeModel.isDone = false;
            decrementDone();
        }
        App.getDataBase().homeDao().update(list.get(id));
    }

    private void incrementDone() {
        previousData = HomeDoneSizePreference.getInstance(this).getDataSize();
        HomeDoneSizePreference.getInstance(this).saveDataSize(previousData + 1);
    }

    private void decrementDone() {
        currentData = HomeDoneSizePreference.getInstance(this).getDataSize();
        updateData = currentData - 1;
        HomeDoneSizePreference.getInstance(this).saveDataSize(updateData);
    }

    @Override
    public void pressOk() {
        App.getDataBase().homeDao().deleteAll(list);
        HomeDoneSizePreference.getInstance(HomeActivity.this).clearSettings();
    }


    @Override
    public void onItemLongClick(int id) {
        findViewById(R.id.add_task_home).setVisibility(View.GONE);
        findViewById(R.id.change_task_home).setVisibility(View.VISIBLE);
        homeModel = list.get(id);
        editText.setText(homeModel.getHomeTask());
        this.id=id;
        keyboardHelper.openKeyboard(HomeActivity.this);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }

    public void addHomeChangeTask(View view) {
        if(editText.getText().toString().trim().isEmpty()){
            Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show();

        }else {
            updateHomeTask(id);
            findViewById(R.id.change_task_home).setVisibility(View.GONE);
            findViewById(R.id.add_task_home).setVisibility(View.VISIBLE);
            editText.getText().clear();
            keyboardHelper.hideKeyboard(HomeActivity.this, view, editText);
        }
    }

    private void updateHomeTask(int id) {
        homeModel = list.get(id);
        homeModel.setHomeTask(editText.getText().toString());
        App.getDataBase().homeDao().update(list.get(id));
        adapter.notifyDataSetChanged();
    }

}