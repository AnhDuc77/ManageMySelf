package com.example.manageexpenses.dao;

import androidx.room.*;
import com.example.manageexpenses.entity.DebtCredit;
import java.util.List;

@Dao
public interface DebtCreditDao {
    @Insert
    long insert(DebtCredit debtCredit);

    @Update
    void update(DebtCredit debtCredit);

    @Delete
    void delete(DebtCredit debtCredit);

    @Query("SELECT * FROM DebtCredit WHERE isDebt = 1")
    List<DebtCredit> getAllDebts();

    @Query("SELECT * FROM DebtCredit WHERE isDebt = 0")
    List<DebtCredit> getAllCredits();

    @Query("SELECT * FROM DebtCredit WHERE id = :id")
    DebtCredit getDebtCreditById(int id);

    @Query("SELECT * FROM DebtCredit WHERE dueDate = :dueDate")
    List<DebtCredit> getDebtCreditsByDueDate(String dueDate);
} 