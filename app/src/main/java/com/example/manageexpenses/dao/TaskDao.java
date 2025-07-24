package com.example.manageexpenses.dao;

import androidx.room.*;
import com.example.manageexpenses.entity.Task;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM Task WHERE tag = :tag")
    List<Task> getTasksByTag(String tag);

    @Query("SELECT * FROM Task WHERE isCompleted = 0")
    List<Task> getPendingTasks();

    @Query("SELECT * FROM Task WHERE isCompleted = 1")
    List<Task> getCompletedTasks();

    @Query("SELECT * FROM Task WHERE id = :id")
    Task getTaskById(int id);

    @Query("SELECT * FROM Task WHERE title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%'")
    List<Task> searchTasks(String keyword);

    @Query("SELECT COUNT(*) FROM Task WHERE isCompleted = 1")
    int getCompletedTaskCount();

    @Query("SELECT COUNT(*) FROM Task WHERE isCompleted = 0")
    int getPendingTaskCount();

    @Query("SELECT * FROM Task WHERE tag = :tag AND dueDate = :date")
    List<Task> getTasksByTagAndDate(String tag, String date);

    @Query("SELECT * FROM Task WHERE tag = :tag AND priority = :priority")
    List<Task> getTasksByTagAndPriority(String tag, int priority);

    @Query("SELECT * FROM Task WHERE tag = :tag AND dueDate = :date AND priority = :priority")
    List<Task> getTasksByTagDatePriority(String tag, String date, int priority);

    @Query("SELECT COUNT(*) FROM Task WHERE isCompleted = 1 AND dueDate = :date")
    int getCompletedTaskCountByDate(String date);

    @Query("SELECT COUNT(*) FROM Task WHERE isCompleted = 0 AND dueDate = :date")
    int getPendingTaskCountByDate(String date);

    @Query("SELECT COUNT(*) FROM Task WHERE isCompleted = 1 AND dueDate LIKE :month || '%'")
    int getCompletedTaskCountByMonth(String month);

    @Query("SELECT COUNT(*) FROM Task WHERE isCompleted = 0 AND dueDate LIKE :month || '%'")
    int getPendingTaskCountByMonth(String month);
} 