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
import com.example.manageexpenses.adapter.MemberAdapter;
import com.example.manageexpenses.adapter.TransactionAdapter;
import com.example.manageexpenses.entity.Group;
import com.example.manageexpenses.entity.Member;
import com.example.manageexpenses.entity.Transaction;
import com.example.manageexpenses.entity.TransactionMember;
import com.example.manageexpenses.util.ExpenseSplitter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.room.Room;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupActivity extends AppCompatActivity {
    private TextView tvGroupName, tvSettlementResult;
    private RecyclerView rvMembers, rvGroupTransactions;
    private FloatingActionButton fabAddGroupTransaction;
    private AppDatabase db;
    private MemberAdapter memberAdapter;
    private TransactionAdapter transactionAdapter;
    private int groupId;
    private List<Member> memberList = new ArrayList<>();
    private List<Transaction> transactionList = new ArrayList<>();
    private List<TransactionMember> transactionMemberList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        tvGroupName = findViewById(R.id.tvGroupName);
        tvSettlementResult = findViewById(R.id.tvSettlementResult);
        rvMembers = findViewById(R.id.rvMembers);
        rvGroupTransactions = findViewById(R.id.rvGroupTransactions);
        fabAddGroupTransaction = findViewById(R.id.fabAddGroupTransaction);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "todo_manager.db").allowMainThreadQueries().build();
        // Lấy groupId từ intent (giả sử truyền vào)
        groupId = getIntent().getIntExtra("groupId", 1);
        Group group = db.groupDao().getGroupById(groupId);
        tvGroupName.setText(group != null ? group.name : "Group");
        // Load members
        memberList = db.memberDao().getMembersByGroupId(groupId);
        memberAdapter = new MemberAdapter(this, memberList, m -> {
            Toast.makeText(this, "Member: " + m.name, Toast.LENGTH_SHORT).show();
        });
        rvMembers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvMembers.setAdapter(memberAdapter);
        // Load transactions
        transactionList = db.transactionDao().getTransactionsByGroupId(groupId);
        transactionAdapter = new TransactionAdapter(this, transactionList, new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction t) {
                Toast.makeText(GroupActivity.this, "Transaction: " + t.title, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onEditClick(Transaction t) {
                // Có thể mở dialog edit transaction nếu muốn, hoặc để trống
            }
            @Override
            public void onDeleteClick(Transaction t) {
                // Có thể xử lý xóa transaction nếu muốn, hoặc để trống
            }
        });
        rvGroupTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvGroupTransactions.setAdapter(transactionAdapter);
        // Load transaction-member links
        transactionMemberList = new ArrayList<>();
        for (Transaction t : transactionList) {
            transactionMemberList.addAll(db.transactionMemberDao().getMembersByTransactionId(t.id));
        }
        // Tính chia tiền
        Map<Integer, Double> result = ExpenseSplitter.calculateDebts(transactionList, memberList, transactionMemberList);
        StringBuilder sb = new StringBuilder();
        for (Member m : memberList) {
            double value = result.get(m.id);
            if (value > 0) sb.append(m.name).append(" nhận ").append((int)value).append("\n");
            else if (value < 0) sb.append(m.name).append(" trả ").append((int)(-value)).append("\n");
            else sb.append(m.name).append(" cân bằng\n");
        }
        tvSettlementResult.setText(sb.toString());
        fabAddGroupTransaction.setOnClickListener(v -> {
            Toast.makeText(this, "Add group transaction", Toast.LENGTH_SHORT).show();
            // TODO: Hiển thị dialog thêm giao dịch nhóm
        });
    }
} 