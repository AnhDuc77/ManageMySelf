package com.example.manageexpenses.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manageexpenses.R;
import com.example.manageexpenses.entity.ReminderTask;
import java.util.List;

public class ReminderTaskAdapter extends RecyclerView.Adapter<ReminderTaskAdapter.ReminderViewHolder> {
    private List<ReminderTask> reminders;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(ReminderTask task);
        void onDeleteClick(ReminderTask task);
        void onToggleEnable(ReminderTask task);
    }

    public ReminderTaskAdapter(Context context, List<ReminderTask> reminders, OnItemClickListener listener) {
        this.context = context;
        this.reminders = reminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder_task, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ReminderTask t = reminders.get(position);
        holder.tvTitle.setText(t.title);
        holder.tvTime.setText(String.format("%02d:%02d", t.hour, t.minute));
        holder.tvRepeat.setText(t.repeatType == 0 ? "Daily" : getRepeatDaysText(t.repeatDays));
        holder.tvStatus.setText(t.isEnabled ? "Enabled" : "Disabled");
        holder.tvStatus.setTextColor(t.isEnabled ? 0xFF388E3C : 0xFFD32F2F);
        holder.itemView.setOnClickListener(v -> listener.onEditClick(t));
        holder.btnMenu.setOnClickListener(v -> showPopupMenu(holder.btnMenu, t));
    }
    private void showPopupMenu(View anchor, ReminderTask t) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(context, anchor);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.getMenu().add(t.isEnabled ? "Disable" : "Enable");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Edit")) listener.onEditClick(t);
            else if (item.getTitle().equals("Delete")) listener.onDeleteClick(t);
            else listener.onToggleEnable(t);
            return true;
        });
        popup.show();
    }
    private String getRepeatDaysText(String repeatDays) {
        if (repeatDays == null || repeatDays.isEmpty()) return "";
        String[] days = repeatDays.split(",");
        String[] week = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        StringBuilder sb = new StringBuilder();
        for (String d : days) {
            int idx = Integer.parseInt(d);
            if (idx >= 0 && idx < 7) sb.append(week[idx]).append(", ");
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }
    @Override
    public int getItemCount() {
        return reminders.size();
    }
    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvRepeat, tvStatus;
        ImageButton btnMenu;
        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvTime = itemView.findViewById(R.id.tvReminderTime);
            tvRepeat = itemView.findViewById(R.id.tvReminderRepeat);
            tvStatus = itemView.findViewById(R.id.tvReminderStatus);
            btnMenu = itemView.findViewById(R.id.btnReminderMenu);
        }
    }
} 