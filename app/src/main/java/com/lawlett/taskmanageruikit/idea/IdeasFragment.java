package com.lawlett.taskmanageruikit.idea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.idea.data.model.QuickModel;
import com.lawlett.taskmanageruikit.idea.recycler.QuickAdapter;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.Constants;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.IIdeaOnClickListener;
import com.lawlett.taskmanageruikit.utils.preferences.IdeaViewPreference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class IdeasFragment extends Fragment implements IIdeaOnClickListener {
    private QuickAdapter adapter;
    private List<QuickModel> list;
    private FloatingActionButton addQuickBtn;
    private int pos;
    private TextView firstText;
    private RecyclerView recyclerViewQuick;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private ImageView btnChange;
    private ProgressBar progressBar;
    private FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
    private String collectionName;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ideas, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (user != null) {
            collectionName = "Идеи" + "-" + "(" + user.getDisplayName() + ")" + user.getUid();
        }
        initViews(view);
        initClickers();
        initAdapter();
        getRoomRecordsData();
        btnGridChange();
        initItemTouchHelper();
    }

    private void getRoomRecordsData() {
        App.getDataBase().ideaDao().getAllLive().observe(this, quickModels -> {
            if (quickModels != null) {
                progressBar.setVisibility(View.GONE);
                list.clear();
                list.addAll(quickModels);
                firstText.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                if (quickModels.isEmpty()) {
                    firstText.setVisibility(View.VISIBLE);
                }
                if (quickModels.size() != 0) {
                    writeAllTaskFromRoomToFireStore();
                } else {
                    readDataFromFireStore();
                }
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void initViews(View view) {
        list = new ArrayList<>();
        firstText = view.findViewById(R.id.quick_tv);
        recyclerViewQuick = view.findViewById(R.id.quick_recycler);
        btnChange = Objects.requireNonNull(getActivity()).findViewById(R.id.tool_btn_grid);
        addQuickBtn = view.findViewById(R.id.add_quick_btn);
        progressBar = view.findViewById(R.id.progress_bar);
        addQuickBtn.setColorFilter(Color.WHITE);
        addQuickBtn.setBackgroundColor(R.color.plus_background);
    }

    private void readDataFromFireStore() {
        if (user != null) {
            String createDateKey = "createData";
            String descriptionKey = "description";
            String imageKey = "image";
            String titleKey = "title";
            progressBar.setVisibility(View.VISIBLE);
            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Map<String, Object> dataFromFireBase;
                                dataFromFireBase = document.getData();
                                String createDate = (String) dataFromFireBase.get(createDateKey);
                                String description = (String) dataFromFireBase.get(descriptionKey);
                                String image = (String) dataFromFireBase.get(imageKey);
                                String title = (String) dataFromFireBase.get(titleKey);
                                QuickModel quickModel = new QuickModel(title, description, createDate, image, 0, "");
                                App.getDataBase().ideaDao().insert(quickModel);
                            }
                        }
                    });
        }
    }

    private void writeAllTaskFromRoomToFireStore() {
        if (user != null) {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ideaPreferences", Context.MODE_PRIVATE);
            Calendar calendar = Calendar.getInstance();
            String currentDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String dayFromPreference = sharedPreferences.getString(Constants.CURRENT_DAY, "");
            if (!currentDay.equals(dayFromPreference)) {
                for (int i = 0; i < list.size(); i++) {
                    FireStoreTools.deleteDataByFireStore(list.get(i).getTitle(), collectionName, db, null);
                }
                for (int i = 0; i < list.size(); i++) {
                    FireStoreTools.writeOrUpdateDataByFireStore(list.get(i).getTitle(), collectionName, db, list.get(i));
                }
                sharedPreferences.edit().clear().apply();
                sharedPreferences.edit().putString("currentDay", currentDay).apply();
            }
        }
    }

    @Override
    public void onItemClick(final int position) {
        Intent intent = new Intent(getActivity(), IdeaActivity.class);
        intent.putExtra("task", list.get(position));
        requireActivity().startActivityForResult(intent, 42);
        adapter.notifyDataSetChanged();
    }

    private void initClickers() {
        addQuickBtn.setOnClickListener(v -> startActivity(new Intent(getContext(), IdeaActivity.class)));
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
                App.getDataBase().ideaDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.to_delete)
                        .setNegativeButton(R.string.no, (dialog1, which) -> {
                            adapter.notifyDataSetChanged();
                            dialog1.cancel();
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pos = viewHolder.getAdapterPosition();
                                App.getDataBase().ideaDao().delete(list.get(pos));
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), R.string.delete, Toast.LENGTH_SHORT).show();
                            }
                        }).show();
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
        }).attachToRecyclerView(recyclerViewQuick);
    }

    private void initAdapter() {
        adapter = new QuickAdapter(list, this, getContext());
        recyclerViewQuick.setAdapter(adapter);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewQuick.setLayoutManager(staggeredGridLayoutManager);
        if (IdeaViewPreference.getInstance(getContext()).getView()) {
            staggeredGridLayoutManager.setSpanCount(2);
        } else {
            staggeredGridLayoutManager.setSpanCount(1);
        }
    }

    public void btnGridChange() {
        btnChange.setOnClickListener(v -> {
            if (!btnChange.isActivated()) {
                btnChange.setActivated(true);
                staggeredGridLayoutManager.setSpanCount(2);
                IdeaViewPreference.getInstance(getContext()).saveView(true);
            } else {
                btnChange.setActivated(false);
                staggeredGridLayoutManager.setSpanCount(1);
                IdeaViewPreference.getInstance(getContext()).saveView(false);
            }
            adapter.notifyDataSetChanged();
            adapter.isChange.setValue(false);
        });
    }


}
