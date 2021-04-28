package com.lawlett.taskmanageruikit.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lawlett.taskmanageruikit.timing.model.TimingModel;

import java.util.List;

@Dao
public interface TimingDao {

    @Query("SELECT*FROM timingModel")
    List<TimingModel> getAll();

    @Query("SELECT*FROM timingModel")
    LiveData<List<TimingModel>> getAllLive();

    @Insert
    void insert(TimingModel timingModel);

    @Delete
    void delete(TimingModel timingModel);

    @Delete
    void deleteAll(List<TimingModel> timingModel);

    @Update
    void update(TimingModel timingModel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateWord(List<TimingModel> timingModel);

}
