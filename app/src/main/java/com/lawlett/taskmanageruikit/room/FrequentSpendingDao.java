package com.lawlett.taskmanageruikit.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lawlett.taskmanageruikit.finance.model.FrequentSpendingModel;

import java.util.List;

@Dao
public interface FrequentSpendingDao {

    @Query("SELECT * FROM frequentspendingmodel order by id desc")
    LiveData<List<FrequentSpendingModel>> getAll();

    @Query("SELECT * FROM frequentspendingmodel")
    List<FrequentSpendingModel> getAllList();

    @Insert
    void insert(FrequentSpendingModel model);

    @Delete
    void delete(FrequentSpendingModel model);

    @Update
    void update(FrequentSpendingModel model);
}
