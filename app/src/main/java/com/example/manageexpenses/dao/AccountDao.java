package com.example.manageexpenses.dao;

import androidx.room.*;
import com.example.manageexpenses.entity.Account;
import java.util.List;

@Dao
public interface AccountDao {
    @Insert
    long insert(Account account);

    @Update
    void update(Account account);

    @Delete
    void delete(Account account);

    @Query("SELECT * FROM Account")
    List<Account> getAllAccounts();

    @Query("SELECT * FROM Account WHERE id = :id")
    Account getAccountById(int id);

    @Query("SELECT SUM(balance) FROM Account")
    Double getTotalBalance();
} 