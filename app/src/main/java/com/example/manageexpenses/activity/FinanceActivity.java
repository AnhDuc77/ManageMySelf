package com.example.manageexpenses.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manageexpenses.AppDatabase;
import com.example.manageexpenses.R;
import com.example.manageexpenses.adapter.AccountAdapter;
import com.example.manageexpenses.adapter.DebtCreditAdapter;
import com.example.manageexpenses.entity.Account;
import com.example.manageexpenses.entity.DebtCredit;
import com.example.manageexpenses.util.FinanceUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.room.Room;
import java.util.ArrayList;
import java.util.List;

public class FinanceActivity extends AppCompatActivity {
    private TextView tvTotalAssets;
    private RecyclerView rvAccounts, rvDebtsCredits;
    private FloatingActionButton fabAddAccount;
    private AppDatabase db;
    private AccountAdapter accountAdapter;
    private DebtCreditAdapter debtCreditAdapter;
    private List<Account> accountList = new ArrayList<>();
    private List<DebtCredit> debtCreditList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);
        tvTotalAssets = findViewById(R.id.tvTotalAssets);
        rvAccounts = findViewById(R.id.rvAccounts);
        rvDebtsCredits = findViewById(R.id.rvDebtsCredits);
        fabAddAccount = findViewById(R.id.fabAddAccount);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "todo_manager.db").allowMainThreadQueries().build();
        // Load accounts
        accountList = db.accountDao().getAllAccounts();
        accountAdapter = new AccountAdapter(this, accountList, new AccountAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Account a) {}
            @Override
            public void onEditClick(Account a) {}
            @Override
            public void onDeleteClick(Account a) {}
        });
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        rvAccounts.setAdapter(accountAdapter);
        // Load debts/credits
        debtCreditList = db.debtCreditDao().getAllDebts();
        debtCreditList.addAll(db.debtCreditDao().getAllCredits());
        debtCreditAdapter = new DebtCreditAdapter(this, debtCreditList, new DebtCreditAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DebtCredit d) {}
            @Override
            public void onEditClick(DebtCredit d) {}
            @Override
            public void onDeleteClick(DebtCredit d) {}
            @Override
            public void onToggleStatus(DebtCredit d) {}
        });
        rvDebtsCredits.setLayoutManager(new LinearLayoutManager(this));
        rvDebtsCredits.setAdapter(debtCreditAdapter);
        // Tổng tài sản
        double totalAssets = FinanceUtils.getTotalAssets(accountList);
        tvTotalAssets.setText("Total Assets: " + String.format("%,.0f", totalAssets));
        fabAddAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Add account/debt/credit", Toast.LENGTH_SHORT).show();
            // TODO: Hiển thị dialog thêm tài khoản/nợ/vay
        });
    }
} 