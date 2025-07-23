package com.example.manageexpenses;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.manageexpenses.entity.*;
import com.example.manageexpenses.dao.*;

@Database(entities = {
        Transaction.class,
        Group.class,
        Member.class,
        BillImage.class,
        TransactionMember.class,
        Category.class,
        Task.class,
        Account.class,
        DebtCredit.class
    }, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();
    public abstract GroupDao groupDao();
    public abstract MemberDao memberDao();
    public abstract BillImageDao billImageDao();
    public abstract TransactionMemberDao transactionMemberDao();
    public abstract CategoryDao categoryDao();
    public abstract TaskDao taskDao();
    public abstract AccountDao accountDao();
    public abstract DebtCreditDao debtCreditDao();
} 