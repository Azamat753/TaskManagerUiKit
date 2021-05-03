package com.lawlett.taskmanageruikit.achievement;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.achievement.adapter.AchievementAdapter;
import com.lawlett.taskmanageruikit.achievement.models.AchievementModel;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.preferences.HomeDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.preferences.MeetDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.preferences.PersonDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.preferences.PrivateDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.preferences.WorkDoneSizePreference;

import java.util.Calendar;
import java.util.List;

import static com.lawlett.taskmanageruikit.achievement.models.AchievementModel.Category;

public class AchievementActivity extends AppCompatActivity {

    private AchievementViewModel mViewModel;
    private RecyclerView recyclerView;
    private AchievementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);
        initViewModel();
        initRecyclerView();
        subscribeAchievementData();
        insertedAchievements();

    }

    private void insertedAchievements() {
        insertedAchievementsCategory(PersonDoneSizePreference.getInstance(this).getPersonalSize(), Category.PERSONAL);
        insertedAchievementsCategory(WorkDoneSizePreference.getInstance(this).getDataSize(), Category.WORK);
        insertedAchievementsCategory(MeetDoneSizePreference.getInstance(this).getDataSize(), Category.MEET);
        insertedAchievementsCategory(HomeDoneSizePreference.getInstance(this).getDataSize(), Category.HOME);
        insertedAchievementsCategory(PrivateDoneSizePreference.getInstance(this).getDataSize(), Category.DONE);
    }

    private void insertedAchievementsCategory(int size, Category category) {
        List<AchievementModel> dataCategory = App.getDataBase().achievementDao().getAllByCategory(category);
        for (int i = 0; i < size / 5; i++) {
            if (i + 1 > dataCategory.size()) {
                App.getDataBase().achievementDao().insert(new AchievementModel(Calendar.getInstance().getTime(), (i + 1) * 5, category));
            }
        }
    }

    private void initRecyclerView() {
        initRecyclerViews();
        initAdapters();
        setAdapters();
    }

    private void setAdapters() {
        recyclerView.setAdapter(adapter);
    }

    private void initAdapters() {
        adapter = new AchievementAdapter();
    }

    private void initRecyclerViews() {
        recyclerView = findViewById(R.id.achievement_recycler);
    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(App.instance))
                .get(AchievementViewModel.class);
    }

    private void subscribeAchievementData() {
        mViewModel.data.observe(this, levelModels -> {
            adapter.clearAll();
            adapter.setData(levelModels);
        });
    }

    public void backBtn(View view) {
        onBackPressed();
    }
}
