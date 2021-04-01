package com.lawlett.taskmanageruikit.achievement;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.achievement.models.AchievementModel;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.TaskDialogPreference;

import java.util.List;

public class AchievementViewModel extends ViewModel {

    MutableLiveData<List<AchievementModel>> data = new MutableLiveData<>();
    MutableLiveData<String>
            doneTitle = new MutableLiveData<>(),
            homeTitle = new MutableLiveData<>(),
            meetTitle = new MutableLiveData<>(),
            workTitle = new MutableLiveData<>(),
            personalTitle = new MutableLiveData<>();

    public AchievementViewModel() {
        App.getDataBase().achievementDao().getAll().observeForever(data -> {
            this.data.setValue(data);
            checkCategories();
        });
    }

    private void checkCategories() {
//        if (!TaskDialogPreference.getTitle().isEmpty()) {
//            doneTitle.setValue(TaskDialogPreference.getTitle());
//        }
//        if (TaskDialogPreference.getHomeTitle().isEmpty()) {
//            homeTitle.setValue("");
//        }
//        if (!TaskDialogPreference.getMeetTitle().isEmpty()) {
//            meetTitle.setValue(TaskDialogPreference.getMeetTitle());
//        }
//        if (!TaskDialogPreference.getWorkTitle().isEmpty()) {
//            workTitle.setValue(TaskDialogPreference.getWorkTitle());
//        }
//        if (!TaskDialogPreference.getPersonTitle().isEmpty()) {
//            personalTitle.setValue(TaskDialogPreference.getPersonTitle());
//        }
        checkIsEmpty();
    }

    private void checkIsEmpty() {
            for (AchievementModel achievementModel : data.getValue()) {
                switch (achievementModel.getCategory()) {
                    case PERSONAL:
                        personalTitle.setValue(TaskDialogPreference.getPersonTitle());
                        break;
                    case WORK:
                        workTitle.setValue(TaskDialogPreference.getWorkTitle());
                        break;
                    case MEET:
                        meetTitle.setValue(TaskDialogPreference.getMeetTitle());
                        break;
                    case HOME:
//                        if(!TaskDialogPreference.getHomeTitle().isEmpty()){
//                        homeTitle.setValue(TaskDialogPreference.getHomeTitle());
//                        }
                        homeTitle.setValue(TaskDialogPreference.getHomeTitle());
                        break;
                    case DONE:
                        doneTitle.setValue(TaskDialogPreference.getTitle());
                        break;
                }
            }
    }
}
