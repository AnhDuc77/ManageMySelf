package com.example.manageexpenses.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public double amount;
    public int categoryId;
    public String date;
    public String note;
    public int groupId; // 0 nếu là cá nhân
    public int paidBy; // Member ID (0 nếu là cá nhân)
    public int accountId; // nguồn tài khoản chi trả
} 