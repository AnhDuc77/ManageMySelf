package com.example.manageexpenses.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manageexpenses.entity.Member;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private List<Member> members;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Member member);
    }

    public MemberAdapter(Context context, List<Member> members, OnItemClickListener listener) {
        this.context = context;
        this.members = members;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView tv = new TextView(context);
        tv.setPadding(16, 16, 16, 16);
        tv.setTextSize(16);
        return new MemberViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member m = members.get(position);
        holder.tvName.setText(m.name);
        holder.tvName.setOnClickListener(v -> listener.onItemClick(m));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = (TextView) itemView;
        }
    }
} 