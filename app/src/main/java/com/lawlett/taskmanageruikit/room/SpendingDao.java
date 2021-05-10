package com.lawlett.taskmanageruikit.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lawlett.taskmanageruikit.finance.model.SpendingModel;

import java.util.List;

@Dao
public interface SpendingDao {

    @Query("select * from spendingmodel order by id desc")
    List<SpendingModel> getAll();

    @Insert
    void insert(SpendingModel model);

    @Delete
    void delete(SpendingModel model);

    @Update
    void update(SpendingModel model);
}
