package com.example.manageexpenses.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.MenuInflater;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manageexpenses.R;
import com.example.manageexpenses.entity.Account;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private List<Account> accounts;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Account account);
        void onEditClick(Account account);
        void onDeleteClick(Account account);
    }

    public AccountAdapter(Context context, List<Account> accounts, OnItemClickListener listener) {
        this.context = context;
        this.accounts = accounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account a = accounts.get(position);
        holder.tvName.setText(a.name);
        holder.tvType.setText(a.type);
        holder.tvBalance.setText(String.format("Balance: %,.0f", a.balance));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(a));
        holder.btnMenu.setOnClickListener(v -> showPopupMenu(holder.btnMenu, a));
    }

    private void showPopupMenu(View anchor, Account a) {
        PopupMenu popup = new PopupMenu(context, anchor);
        MenuInflater inflater = popup.getMenuInflater();
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Edit")) listener.onEditClick(a);
            else if (item.getTitle().equals("Delete")) listener.onDeleteClick(a);
            return true;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvBalance;
        ImageView ivIcon;
        ImageButton btnMenu;
        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAccountName);
            tvType = itemView.findViewById(R.id.tvAccountType);
            tvBalance = itemView.findViewById(R.id.tvAccountBalance);
            ivIcon = itemView.findViewById(R.id.ivAccountIcon);
            btnMenu = itemView.findViewById(R.id.btnAccountMenu);
        }
    }
} 