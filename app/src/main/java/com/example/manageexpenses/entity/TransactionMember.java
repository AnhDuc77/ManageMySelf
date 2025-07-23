package com.example.manageexpenses.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TransactionMember {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int transactionId;
    public int memberId;
} 