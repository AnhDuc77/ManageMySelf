package com.example.manageexpenses.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class BillImage {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int transactionId;
    public String imagePath;
} 