package com.example.manageexpenses.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.manageexpenses.AppDatabase;
import com.example.manageexpenses.R;
import com.example.manageexpenses.entity.Transaction;
import com.example.manageexpenses.entity.BillImage;
import com.example.manageexpenses.entity.Account;
import com.example.manageexpenses.entity.Group;
import com.example.manageexpenses.entity.Member;
import com.squareup.picasso.Picasso;
import androidx.room.Room;
import java.util.List;
import java.io.File;

public class TransactionDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvAmount, tvCategory, tvDate, tvNote, tvAccount, tvGroup, tvMembers;
    private ImageView ivBillImage;
    private AppDatabase db;
    private int transactionId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAmount = findViewById(R.id.tvDetailAmount);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvDate = findViewById(R.id.tvDetailDate);
        tvNote = findViewById(R.id.tvDetailNote);
        tvAccount = findViewById(R.id.tvDetailAccount);
        tvGroup = findViewById(R.id.tvDetailGroup);
        tvMembers = findViewById(R.id.tvDetailMembers);
        ivBillImage = findViewById(R.id.ivDetailBillImage);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "todo_manager.db").allowMainThreadQueries().build();
        transactionId = getIntent().getIntExtra("transactionId", 0);
        Transaction t = db.transactionDao().getTransactionById(transactionId);
        if (t == null) return;
        tvTitle.setText(t.title);
        tvAmount.setText(String.format("%,.0f", t.amount));
        tvCategory.setText(String.valueOf(t.categoryId)); // Có thể map sang tên danh mục
        tvDate.setText(t.date);
        tvNote.setText(t.note);
        // Account
        Account acc = db.accountDao().getAccountById(t.accountId);
        tvAccount.setText(acc != null ? acc.name : "");
        // Group
        if (t.groupId > 0) {
            Group group = db.groupDao().getGroupById(t.groupId);
            tvGroup.setText(group != null ? group.name : "");
            // Members
            List<Member> members = db.memberDao().getMembersByGroupId(t.groupId);
            StringBuilder sb = new StringBuilder();
            for (Member m : members) sb.append(m.name).append(", ");
            tvMembers.setText(sb.toString());
        } else {
            tvGroup.setText("");
            tvMembers.setText("");
        }
        // Bill image
        List<BillImage> images = db.billImageDao().getImagesByTransactionId(transactionId);
        if (images != null && images.size() > 0) {
            Picasso.get().load(new File(images.get(0).imagePath)).placeholder(R.drawable.ic_camera).into(ivBillImage);
        }
    }
} 