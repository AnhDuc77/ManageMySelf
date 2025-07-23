package com.example.manageexpenses.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Category {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
} 