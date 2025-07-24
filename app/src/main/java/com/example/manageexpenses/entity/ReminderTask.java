package com.example.manageexpenses.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ReminderTask {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
    public int hour;
    public int minute;
    public int repeatType; // 0: daily, 1: custom
    public String repeatDays; // ví dụ: "1,3,5" cho Mon,Wed,Fri
    public boolean isEnabled;
} 