package com.lawlett.taskmanageruikit.idea;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.idea.data.model.QuickModel;
import com.lawlett.taskmanageruikit.idea.recycler.QuickAdapter;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.IQuickOnClickListener;
import com.lawlett.taskmanageruikit.utils.IdeaViewPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class IdeasFragment extends Fragment implements IQuickOnClickListener {
    QuickAdapter adapter;
    private List<QuickModel> list;
    FloatingActionButton addQuickBtn;
    int position;
    int pos;
    RecyclerView recyclerViewQuick;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    ImageView btnChange;

    public IdeasFragment() {
        // Required empty public constructor

    }

    @SuppressLint({"FragmentLiveDataObserve", "ResourceAsColor"})
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ideas, container, false);

        list = new ArrayList<>();

        App.getDataBase().taskDao().getAllLive().observe(this, quickModels -> {
            if (quickModels != null)
                list.clear();
            list.addAll(quickModels);
            adapter.notifyDataSetChanged();

        });
        addQuickBtn = root.findViewById(R.id.add_quick_btn);
        addQuickBtn.setColorFilter(Color.WHITE);
        addQuickBtn.setBackgroundColor(R.color.plus_background);
        addQuickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), IdeaActivity.class));
            }
        });
        return root;
    }

    @Override
    public void onItemClick(final int position) {
        this.position = position;
        Intent intent = new Intent(getActivity(), IdeaActivity.class);
        intent.putExtra("task", list.get(position));
        getActivity().startActivityForResult(intent, 42);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewQuick = view.findViewById(R.id.quick_recycler);
        adapter = new QuickAdapter(list, this, getContext());
        recyclerViewQuick.setAdapter(adapter);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewQuick.setLayoutManager(staggeredGridLayoutManager);

        if (IdeaViewPreference.getInstance(getContext()).getView()) {
            staggeredGridLayoutManager.setSpanCount(2);
        } else staggeredGridLayoutManager.setSpanCount(1);


        btnChange = Objects.requireNonNull(getActivity()).findViewById(R.id.tool_btn_grid);
        btnGridChange();


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);
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
                App.getDataBase().taskDao().updateWord(list);
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.to_delete)
                        .setNegativeButton(R.string.no, (dialog1, which) ->
                                dialog1.cancel())
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pos = viewHolder.getAdapterPosition();
                                App.getDataBase().taskDao().delete(list.get(pos));
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), R.string.delete, Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        }).attachToRecyclerView(recyclerViewQuick);
    }

    public void btnGridChange() {

        btnChange.setOnClickListener(v -> {
            if (!btnChange.isActivated()) {
                btnChange.setActivated(true);
                staggeredGridLayoutManager.setSpanCount(2);
                IdeaViewPreference.getInstance(getContext()).saveView(true);
                adapter.notifyDataSetChanged();

            } else {
                btnChange.setActivated(false);
                staggeredGridLayoutManager.setSpanCount(1);
                IdeaViewPreference.getInstance(getContext()).saveView(false);
                adapter.notifyDataSetChanged();
            }
            adapter.isChange.setValue(false);
        });
    }


}
