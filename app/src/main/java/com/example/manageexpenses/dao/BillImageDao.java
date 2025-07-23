package com.example.manageexpenses.dao;

import androidx.room.*;
import com.example.manageexpenses.entity.BillImage;
import java.util.List;

@Dao
public interface BillImageDao {
    @Insert
    long insert(BillImage billImage);

    @Update
    void update(BillImage billImage);

    @Delete
    void delete(BillImage billImage);

    @Query("SELECT * FROM BillImage WHERE transactionId = :transactionId")
    List<BillImage> getImagesByTransactionId(int transactionId);
} 