package com.example.manageexpenses.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DebtCredit {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name; // tên người liên quan
    public double amount;
    public String dueDate;
    public boolean isDebt; // true: mình cho vay, false: mình nợ
    public boolean isPaid; // true: đã trả, false: chưa trả
    public int relatedPersonId; // id thành viên liên quan (nếu có)
} 