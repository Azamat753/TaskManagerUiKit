package com.lawlett.taskmanageruikit.timing.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.timing.activity.StopwatchActivity;
import com.lawlett.taskmanageruikit.timing.activity.TimerActivity;
import com.lawlett.taskmanageruikit.timing.adapter.TimingAdapter;
import com.lawlett.taskmanageruikit.timing.model.TimingModel;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TimingFragment extends Fragment {
    private TimingAdapter adapter;
    private List<TimingModel> list;
    private TextView tvTiming;
    private int pos;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionStopwatch, floatingActionTimer;
    private ProgressBar progressBar;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String collectionName = Constants.TIMING_COLLECTION;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timing, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initClickers();
        initRoom();
        initRecycler(view);
        initItemTouchHelper();
    }

    private void initItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(list, fromPosition, toPosition);
                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.to_delete)
                        .setNegativeButton(R.string.no, (dialog1, which) ->
                                dialog1.cancel())
                        .setPositiveButton(R.string.yes, (dialog12, which) -> {
                            pos = viewHolder.getAdapterPosition();
                            App.getDataBase().timingDao().delete(list.get(pos));
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), R.string.delete, Toast.LENGTH_SHORT).show();
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
        floatingActionTimer.setOnClickListener(v -> startActivity(new Intent(getContext(), TimerActivity.class)));
        floatingActionStopwatch.setOnClickListener(v -> startActivity(new Intent(getContext(), StopwatchActivity.class)));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initRoom() {
        list = new ArrayList<>();
        App.getDataBase().timingDao().getAllLive().observe(this, timingModels -> {
            if (timingModels != null) {
                progressBar.setVisibility(View.GONE);
                list.clear();
                tvTiming.setVisibility(View.GONE);
                list.addAll(timingModels);
                adapter.notifyDataSetChanged();
            }
            if (timingModels.isEmpty()) {
                tvTiming.setVisibility(View.VISIBLE);
            }
            if (timingModels.size() == 0) {
                readDataFromFireStore();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void readDataFromFireStore() {
        if (user != null) {
            String timerTitleKey = "timerTitle";
            String timerMinutesKey = "timerMinutes";
            String timerDayKey = "timerDay";
            String stopwatchTitleKey = "stopwatchTitle";
            String stopwatchMinutesKey = "stopwatchMinutes";
            String stopwatchDayKey = "stopwatchDay";
            progressBar.setVisibility(View.VISIBLE);
            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Map<String, Object> dataFromFireBase;
                                dataFromFireBase = document.getData();
                                String timerTitle = (String) dataFromFireBase.get(timerTitleKey);
                                Long timerMinutes = (Long) dataFromFireBase.get(timerMinutesKey);
                                Long stopwatchMinutes = (Long) dataFromFireBase.get(stopwatchMinutesKey);
                                String timerDay = (String) dataFromFireBase.get(timerDayKey);
                                String stopwatchTitle = (String) dataFromFireBase.get(stopwatchTitleKey);
                                String stopwatchDay = (String) dataFromFireBase.get(stopwatchDayKey);
                                Integer timerMinutesInt = timerMinutes == null ? null : Math.toIntExact(timerMinutes);
                                Integer stopwatchMinutesInt = stopwatchMinutes == null ? null : Math.toIntExact(stopwatchMinutes);
                                TimingModel timingModel = new TimingModel(timerTitle, timerMinutesInt, timerDay, stopwatchTitle, stopwatchMinutesInt, stopwatchDay);
                                App.getDataBase().timingDao().insert(timingModel);
                            }
                        }
                    });
        }
    }

    private void initViews(View view) {
        floatingActionStopwatch = view.findViewById(R.id.fab_stopwatch);
        floatingActionTimer = view.findViewById(R.id.fab_timer);
        tvTiming = view.findViewById(R.id.timing_tv);
        progressBar = view.findViewById(R.id.timing_progress_bar);
    }

    private void initRecycler(View view) {
        recyclerView = view.findViewById(R.id.timing_recycler);
        adapter = new TimingAdapter(list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}