package com.lawlett.taskmanageruikit.calendarEvents;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.calendarEvents.data.model.CalendarTaskModel;
import com.lawlett.taskmanageruikit.calendarEvents.recycler.CalendarEventAdapter;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.ICalendarEventOnClickListener;
import com.lawlett.taskmanageruikit.utils.LanguagePreference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class CalendarEventsFragment extends Fragment implements ICalendarEventOnClickListener {
    RecyclerView recyclerViewToday;
    FloatingActionButton addEventBtn;
    List<CalendarTaskModel> list;
    CalendarEventAdapter adapter;
    TextView calendarText;
    int position, pos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar_events, container, false);
    }

    private void initRoom() {
        list = new ArrayList<>();
        App.getDataBase().dataDao().getAllLive().observe(this, calendarTaskModels -> {
            if (calendarTaskModels != null) {
                list.clear();
                list.addAll(App.getDataBase().dataDao().getSortedCalendarTaskModel());
                adapter.notifyDataSetChanged();
                calendarText.setVisibility(View.GONE);
            }
            assert calendarTaskModels != null;
            if (calendarTaskModels.isEmpty()) {
                calendarText.setVisibility(View.VISIBLE);
            }
        });
        adapter = new CalendarEventAdapter((ArrayList<CalendarTaskModel>) list, this, getContext());
        recyclerViewToday.setAdapter(adapter);
    }

    private void initViews(View view) {
        calendarText = view.findViewById(R.id.calendar_tv);
        addEventBtn = view.findViewById(R.id.add_task_btn);
        recyclerViewToday = view.findViewById(R.id.today_recycler);
    }
    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initRoom();
        initCalendar();
        initAddEventButton();
        initItemTouchHelper();
        loadLocale();
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
                App.getDataBase().dataDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
                dialog.setTitle(R.string.are_you_sure).setMessage(R.string.to_delete)
                        .setNegativeButton(R.string.no, (dialog1, which) ->
                                dialog1.cancel())
                        .setPositiveButton(R.string.yes, (dialog12, which) -> {
                            pos = viewHolder.getAdapterPosition();
                            App.getDataBase().dataDao().delete(list.get(pos));
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
        }).attachToRecyclerView(recyclerViewToday);
    }

    @SuppressLint("ResourceAsColor")
    private void initAddEventButton() {
        addEventBtn.setColorFilter(Color.WHITE);
        addEventBtn.setBackgroundColor(R.color.plus_background);
        addEventBtn.setOnClickListener(v -> startActivity(new Intent(getContext(), AddEventActivity.class)));
    }

    private void initCalendar() {
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);

        final HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(requireActivity(), R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @SuppressLint({"LogNotTimber", "NewApi"})
            @Override
            public void onDateSelected(Calendar date, int position) {
            }

            @Override
            public void onCalendarScroll(HorizontalCalendarView calendarView, int dx, int dy) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("LogNotTimber")
            @Override
            public boolean onDateLongClicked(Calendar date, int position) {
                return true;
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        this.position = position;
        Intent intent = new Intent(getContext(), AddEventActivity.class);
        intent.putExtra("calendar", list.get(position));
        adapter.notifyDataSetChanged();
        Objects.requireNonNull(getActivity()).startActivityForResult(intent, 42);
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public void loadLocale() {
        String language = LanguagePreference.getInstance(getContext()).getLanguage();
        setLocale(language);
    }
}
