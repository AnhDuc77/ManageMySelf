package com.example.manageexpenses.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manageexpenses.R;
import com.example.manageexpenses.entity.Transaction;
import com.squareup.picasso.Picasso;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> transactions, OnItemClickListener listener) {
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction t = transactions.get(position);
        holder.tvTitle.setText(t.title);
        holder.tvAmount.setText(String.format("%,.0f", t.amount));
        holder.tvCategory.setText(String.valueOf(t.categoryId)); // Sẽ map sang tên danh mục ở Activity
        holder.tvDate.setText(t.date);
        holder.tvNote.setText(t.note);
        // Hiển thị ảnh hóa đơn nếu có
        if (holder.ivBillImage != null && t.id > 0) {
            // Để đơn giản, giả sử ảnh lưu theo path: /storage/emulated/0/Pictures/bill_{id}.jpg
            String imagePath = "/storage/emulated/0/Pictures/bill_" + t.id + ".jpg";
            Picasso.get().load(imagePath).placeholder(R.drawable.ic_camera).into(holder.ivBillImage);
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(t));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount, tvCategory, tvDate, tvNote;
        ImageView ivBillImage;
        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvNote = itemView.findViewById(R.id.tvNote);
            ivBillImage = itemView.findViewById(R.id.ivBillImage);
        }
    }
} 