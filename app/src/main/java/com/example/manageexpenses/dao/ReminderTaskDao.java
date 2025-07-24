package com.example.manageexpenses.dao;

import androidx.room.*;
import com.example.manageexpenses.entity.ReminderTask;
import java.util.List;

@Dao
public interface ReminderTaskDao {
    @Insert
    long insert(ReminderTask task);
    @Update
    void update(ReminderTask task);
    @Delete
    void delete(ReminderTask task);
    @Query("SELECT * FROM ReminderTask")
    List<ReminderTask> getAll();
    @Query("SELECT * FROM ReminderTask WHERE id = :id")
    ReminderTask getById(int id);
} 