package com.lawlett.taskmanageruikit.achievement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.achievement.adapter.AchievementAdapter;
import com.lawlett.taskmanageruikit.achievement.models.AchievementModel;
import com.lawlett.taskmanageruikit.utils.AddDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.HomeDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.MeetDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.PersonDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.PrivateDoneSizePreference;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;
import com.lawlett.taskmanageruikit.utils.WorkDoneSizePreference;

import java.util.Calendar;
import java.util.List;

import static com.lawlett.taskmanageruikit.achievement.models.AchievementModel.*;

public class AchievementActivity extends AppCompatActivity {

    private AchievementViewModel mViewModel;
    private RecyclerView recyclerViewPersonal, recyclerViewWork, recyclerViewMeet, recyclerViewHome, recyclerViewPrivate, recyclerViewDone;
    private AchievementAdapter personalAdapter, workAdapter, meetAdapter, homeAdapter, privateAdapter, doneAdapter;
    private TextView personalTitle, workTitle, meetTitle, homeTitle, privateTitle, doneTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        initViewModel();
        initRecyclerView();
        subscribeAchievementData();
        insertedAchievements();
        initViews();
//        checkOnEmptyCategory();
        subscribeCategoryTitles();
    }

    private void subscribeCategoryTitles() {
        mViewModel.homeTitle.observe(this, title -> {
            homeTitle.setText(title);
            if (!title.isEmpty())
             homeTitle.setVisibility(View.VISIBLE);
        });
    }

    private void checkOnEmptyCategory() {
        if(App.getDataBase().achievementDao().getAllByCategory(Category.HOME).isEmpty()){
            homeTitle.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        personalTitle = findViewById(R.id.text_view_personal);
        workTitle = findViewById(R.id.text_view_work);
        meetTitle = findViewById(R.id.text_view_meet);
        homeTitle = findViewById(R.id.text_view_home);
        privateTitle = findViewById(R.id.text_view_private);
        doneTitle = findViewById(R.id.text_view_done);
    }

    private void insertedAchievements() {
        insertedAchievementsCategory(PersonDoneSizePreference.getInstance(this).getPersonalSize(), Category.PERSONAL);
        insertedAchievementsCategory(WorkDoneSizePreference.getInstance(this).getDataSize(), Category.WORK);
        insertedAchievementsCategory(MeetDoneSizePreference.getInstance(this).getDataSize(), Category.MEET);
        insertedAchievementsCategory(HomeDoneSizePreference.getInstance(this).getDataSize(), Category.HOME);
        insertedAchievementsCategory(PrivateDoneSizePreference.getInstance(this).getDataSize(), Category.PRIVATE);
        insertedAchievementsCategory(AddDoneSizePreference.getInstance(this).getDataSize(), Category.DONE);
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
        recyclerViewPersonal.setAdapter(personalAdapter);
        recyclerViewWork.setAdapter(workAdapter);
        recyclerViewMeet.setAdapter(meetAdapter);
        recyclerViewHome.setAdapter(homeAdapter);
        recyclerViewPrivate.setAdapter(privateAdapter);
        recyclerViewDone.setAdapter(doneAdapter);
    }

    private void initAdapters() {
        personalAdapter = new AchievementAdapter();
        workAdapter = new AchievementAdapter();
        meetAdapter = new AchievementAdapter();
        homeAdapter = new AchievementAdapter();
        privateAdapter = new AchievementAdapter();
        doneAdapter = new AchievementAdapter();
    }

    private void initRecyclerViews() {
        recyclerViewPersonal = findViewById(R.id.recycler_view_personal);
        recyclerViewWork = findViewById(R.id.recycler_view_work);
        recyclerViewMeet = findViewById(R.id.recycler_view_meet);
        recyclerViewHome = findViewById(R.id.recycler_view_home);
        recyclerViewPrivate = findViewById(R.id.recycler_view_private);
        recyclerViewDone = findViewById(R.id.recycler_view_done);

    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(App.instance))
                .get(AchievementViewModel.class);
    }

    private void subscribeAchievementData() {
        mViewModel.data.observe(this, achievementModels -> {
            personalAdapter.clearAll();
            workAdapter.clearAll();
            meetAdapter.clearAll();
            homeAdapter.clearAll();
            privateAdapter.clearAll();
            doneAdapter.clearAll();
            for (AchievementModel achievementModel : achievementModels) {
                switch (achievementModel.getCategory()) {
                    case PERSONAL:
                        personalAdapter.addItem(achievementModel);
                        break;
                    case WORK:
                        workAdapter.addItem(achievementModel);
                        break;
                    case MEET:
                        meetAdapter.addItem(achievementModel);
                        break;
                    case HOME:
                        homeAdapter.addItem(achievementModel);
                        break;
                    case PRIVATE:
                        privateAdapter.addItem(achievementModel);
                        break;
                    case DONE:
                        doneAdapter.addItem(achievementModel);
                        break;
                }
            }
        });
    }

}
