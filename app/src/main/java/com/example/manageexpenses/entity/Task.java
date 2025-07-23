package com.example.manageexpenses.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
    public String dueDate;
    public int priority;
    public boolean isCompleted;
    public String tag; // Work, Home, Groceries, Study, Finance
} 