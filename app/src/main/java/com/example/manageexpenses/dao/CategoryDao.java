package com.example.manageexpenses.dao;

import androidx.room.*;
import com.example.manageexpenses.entity.Category;
import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM Category")
    List<Category> getAllCategories();

    @Query("SELECT * FROM Category WHERE id = :id")
    Category getCategoryById(int id);
} 