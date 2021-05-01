package com.lawlett.taskmanageruikit.calendarEvents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.calendarEvents.data.model.CalendarTaskModel;
import com.lawlett.taskmanageruikit.calendarEvents.recycler.CalendarEventAdapter;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.Constants;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.ICalendarEventOnClickListener;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;
import com.lawlett.taskmanageruikit.utils.preferences.LanguagePreference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class CalendarEventsFragment extends Fragment implements ICalendarEventOnClickListener {
    private RecyclerView recyclerViewToday;
    private FloatingActionButton addEventBtn;
    private List<CalendarTaskModel> list;
    private CalendarEventAdapter adapter;
    private TextView calendarText;
    private int pos;
    ProgressBar progressBar;
    private FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
    private String collectionName;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar_events, container, false);
    }

    private void writeAllTaskFromRoomToFireStore() {
        if (user != null) {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("eventsPreferences", Context.MODE_PRIVATE);
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
    private void readDataFromFireStore() {
        if (user != null) {
            String dateTimeKey = "dataTime";
            String endTimeKey = "endTime";
            String startTimeKey = "startTime";
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
                                String dateTime = (String) dataFromFireBase.get(dateTimeKey);
                                String endTime = (String) dataFromFireBase.get(endTimeKey);
                                String startTime = (String) dataFromFireBase.get(startTimeKey);
                                String title = (String) dataFromFireBase.get(titleKey);
                                CalendarTaskModel calendarTaskModel = new CalendarTaskModel(dateTime, title, startTime, endTime, 0);
                                App.getDataBase().eventsDao().insert(calendarTaskModel);
                            }
                        }
                    });
        }
    }
    private void initRoom() {
        list = new ArrayList<>();
        App.getDataBase().eventsDao().getAllLive().observe(this, calendarTaskModels -> {
            if (calendarTaskModels != null) {
                progressBar.setVisibility(View.GONE);
                list.clear();
                list.addAll(App.getDataBase().eventsDao().getSortedCalendarTaskModel());
                adapter.notifyDataSetChanged();
                calendarText.setVisibility(View.GONE);
            }
            assert calendarTaskModels != null;
            if (calendarTaskModels.isEmpty()) {
                calendarText.setVisibility(View.VISIBLE);
            }
            if (list.size()!=0){
                writeAllTaskFromRoomToFireStore();
            }else {
                readDataFromFireStore();
            }
        });
        adapter = new CalendarEventAdapter((ArrayList<CalendarTaskModel>) list, this, getContext());
        recyclerViewToday.setAdapter(adapter);
    }
    private void initViews(View view) {
        calendarText = view.findViewById(R.id.calendar_tv);
        addEventBtn = view.findViewById(R.id.add_task_btn);
        recyclerViewToday = view.findViewById(R.id.today_recycler);
        progressBar=view.findViewById(R.id.progress_bar);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (user!=null){
            collectionName = "События" + "-" + "(" + user.getDisplayName() + ")" + user.getUid();
        }
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
                App.getDataBase().eventsDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                PlannerDialog.showPlannerDialog(requireActivity(), getString(R.string.you_sure_delete), () -> {
                    pos = viewHolder.getAdapterPosition();
                    App.getDataBase().eventsDao().delete(list.get(pos));
                    Toast.makeText(getContext(), R.string.delete, Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    if (user!=null){
                        FireStoreTools.deleteDataByFireStore(list.get(pos).getTitle(),collectionName,db,progressBar);
                    }
                });
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
