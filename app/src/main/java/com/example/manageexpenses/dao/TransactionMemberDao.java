package com.example.manageexpenses.dao;

import androidx.room.*;
import com.example.manageexpenses.entity.TransactionMember;
import java.util.List;

@Dao
public interface TransactionMemberDao {
    @Insert
    long insert(TransactionMember transactionMember);

    @Delete
    void delete(TransactionMember transactionMember);

    @Query("SELECT * FROM TransactionMember WHERE transactionId = :transactionId")
    List<TransactionMember> getMembersByTransactionId(int transactionId);

    @Query("SELECT * FROM TransactionMember WHERE memberId = :memberId")
    List<TransactionMember> getTransactionsByMemberId(int memberId);
} 