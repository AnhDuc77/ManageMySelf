package com.example.manageexpenses.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manageexpenses.R;
import com.example.manageexpenses.entity.DebtCredit;
import java.util.List;

public class DebtCreditAdapter extends RecyclerView.Adapter<DebtCreditAdapter.DebtCreditViewHolder> {
    private List<DebtCredit> items;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DebtCredit debtCredit);
        void onEditClick(DebtCredit debtCredit);
        void onDeleteClick(DebtCredit debtCredit);
        void onToggleStatus(DebtCredit debtCredit);
    }

    public DebtCreditAdapter(Context context, List<DebtCredit> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DebtCreditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_debt_credit, parent, false);
        return new DebtCreditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtCreditViewHolder holder, int position) {
        DebtCredit d = items.get(position);
        holder.tvName.setText(d.name);
        holder.tvType.setText(d.isDebt ? "Debt" : "Credit");
        holder.tvType.setTextColor(context.getResources().getColor(d.isDebt ? android.R.color.holo_red_dark : android.R.color.holo_green_dark));
        holder.tvAmount.setText(String.format("Amount: %,.0f", d.amount));
        holder.tvDueDate.setText("Due: " + d.dueDate);
        holder.tvStatus.setText(d.isPaid ? "Status: Paid" : "Status: Unpaid");
        holder.tvStatus.setTextColor(context.getResources().getColor(d.isPaid ? android.R.color.holo_green_dark : android.R.color.holo_orange_dark));
        holder.tvStatus.setOnClickListener(v -> listener.onToggleStatus(d));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(d));
        holder.btnMenu.setOnClickListener(v -> showPopupMenu(holder.btnMenu, d));
    }

    private void showPopupMenu(View anchor, DebtCredit d) {
        PopupMenu popup = new PopupMenu(context, anchor);
        MenuInflater inflater = popup.getMenuInflater();
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.getMenu().add(d.isDebt ? "Mark as Paid" : "Mark as Unpaid");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Edit")) listener.onEditClick(d);
            else if (item.getTitle().equals("Delete")) listener.onDeleteClick(d);
            else listener.onToggleStatus(d);
            return true;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class DebtCreditViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvAmount, tvDueDate, tvStatus;
        ImageView ivIcon;
        ImageButton btnMenu;
        public DebtCreditViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDebtName);
            tvType = itemView.findViewById(R.id.tvDebtType);
            tvAmount = itemView.findViewById(R.id.tvDebtAmount);
            tvDueDate = itemView.findViewById(R.id.tvDebtDueDate);
            ivIcon = itemView.findViewById(R.id.ivDebtIcon);
            btnMenu = itemView.findViewById(R.id.btnDebtMenu);
            tvStatus = itemView.findViewById(R.id.tvDebtStatus);
        }
    }
} 