package com.example.manageexpenses.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manageexpenses.R;
import com.example.manageexpenses.entity.Task;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Task task);
        void onDeleteClick(Task task);
        void onStatusClick(Task task);
    }

    public TaskAdapter(Context context, List<Task> tasks, OnItemClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task t = tasks.get(position);
        holder.tvTitle.setText(t.title);
        holder.tvDescription.setText(t.description);
        holder.tvDeadline.setText(t.dueDate);
        holder.tvTag.setText(t.tag);
        holder.tvStatus.setText(t.isCompleted ? "Done" : "Pending");
        holder.tvStatus.setTextColor(t.isCompleted ? 0xFF388E3C : 0xFFD32F2F);
        holder.itemView.setOnClickListener(v -> listener.onEditClick(t));
        holder.btnMenu.setOnClickListener(v -> showPopupMenu(holder.btnMenu, t));
    }
    private void showPopupMenu(View anchor, Task t) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(context, anchor);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.getMenu().add(t.isCompleted ? "Mark as Pending" : "Mark as Done");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Edit")) listener.onEditClick(t);
            else if (item.getTitle().equals("Delete")) listener.onDeleteClick(t);
            else listener.onStatusClick(t);
            return true;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDeadline, tvTag, tvStatus;
        ImageButton btnMenu;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            tvTag = itemView.findViewById(R.id.tvTaskTag);
            tvStatus = itemView.findViewById(R.id.tvTaskStatus);
            btnMenu = itemView.findViewById(R.id.btnTaskMenu);
        }
    }
} 