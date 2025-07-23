package com.example.manageexpenses.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Account {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public double balance;
    public String type; // cash, bank, etc.
} 