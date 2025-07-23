package com.example.manageexpenses.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Member {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public int groupId;
} 