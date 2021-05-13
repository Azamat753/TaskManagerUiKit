package com.lawlett.taskmanageruikit.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HabitDao {

    @Query("SELECT*FROM habitModel")
    List<HabitModel> getAll();

    @Query("SELECT*FROM habitModel")
    LiveData<List<HabitModel>> getAllLive();

    @Insert
    void insert(HabitModel habitModel);

    @Delete
    void delete(HabitModel habitModel);

    @Delete
    void deleteAll(List<HabitModel> habitModel);

    @Update
    void update(HabitModel habitModel);
}