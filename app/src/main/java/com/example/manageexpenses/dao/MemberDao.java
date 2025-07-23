package com.example.manageexpenses.dao;

import androidx.room.*;
import com.example.manageexpenses.entity.Member;
import java.util.List;

@Dao
public interface MemberDao {
    @Insert
    long insert(Member member);

    @Update
    void update(Member member);

    @Delete
    void delete(Member member);

    @Query("SELECT * FROM Member WHERE groupId = :groupId")
    List<Member> getMembersByGroupId(int groupId);

    @Query("SELECT * FROM Member WHERE id = :id")
    Member getMemberById(int id);
} 