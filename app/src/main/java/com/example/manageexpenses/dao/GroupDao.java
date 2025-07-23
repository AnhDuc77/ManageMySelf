package com.example.manageexpenses.dao;

import androidx.room.*;
import com.example.manageexpenses.entity.Group;
import java.util.List;

@Dao
public interface GroupDao {
    @Insert
    long insert(Group group);

    @Update
    void update(Group group);

    @Delete
    void delete(Group group);

    @Query("SELECT * FROM `Group`")
    List<Group> getAllGroups();

    @Query("SELECT * FROM `Group` WHERE id = :id")
    Group getGroupById(int id);
} 