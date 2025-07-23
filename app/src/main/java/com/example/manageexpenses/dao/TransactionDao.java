package com.example.manageexpenses.dao;

import androidx.room.*;
import com.example.manageexpenses.entity.Transaction;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM `Transaction` WHERE groupId = :groupId")
    List<Transaction> getTransactionsByGroupId(int groupId);

    @Query("SELECT * FROM `Transaction` WHERE groupId = 0")
    List<Transaction> getPersonalTransactions();

    @Query("SELECT * FROM `Transaction` WHERE id = :id")
    Transaction getTransactionById(int id);

    @Query("SELECT * FROM `Transaction` WHERE title LIKE '%' || :keyword || '%' OR note LIKE '%' || :keyword || '%'")
    List<Transaction> searchTransactions(String keyword);

    @Query("SELECT * FROM `Transaction` WHERE date = :date")
    List<Transaction> getTransactionsByDate(String date);

    @Query("SELECT SUM(amount) FROM `Transaction` WHERE groupId = 0")
    Double getTotalPersonalExpenses();

    @Query("SELECT SUM(amount) FROM `Transaction` WHERE groupId = :groupId")
    Double getTotalGroupExpenses(int groupId);
} 