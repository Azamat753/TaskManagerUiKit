package com.lawlett.taskmanageruikit.achievement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.lawlett.taskmanageruikit.achievement.models.LevelModel;
import com.lawlett.taskmanageruikit.utils.App;

import java.util.List;

public class AchievementViewModel extends ViewModel {

    LiveData<List<LevelModel>> data;

    public AchievementViewModel() {
        data = App.getDataBase().levelDao().getAll();
    }
}
