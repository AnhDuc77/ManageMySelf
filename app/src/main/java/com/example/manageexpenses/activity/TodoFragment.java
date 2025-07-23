package com.example.manageexpenses.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manageexpenses.AppDatabase;
import com.example.manageexpenses.R;
import com.example.manageexpenses.adapter.TaskAdapter;
import com.example.manageexpenses.entity.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.room.Room;
import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.Locale;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

public class TodoFragment extends Fragment {
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabFilter;
    private AppDatabase db;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private String[] tags = {"Work", "Home", "Groceries", "Study", "Finance"};
    private String filterDate = null;
    private Integer filterPriority = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, container, false);
        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);
        fabFilter = view.findViewById(R.id.fabFilter);
        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "todo_manager.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        for (String tag : tags) {
            tabLayout.addTab(tabLayout.newTab().setText(tag));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        String firstTag = tags[0];
        tabLayout.getTabAt(0).select();
        showTasks(firstTag);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tag = tab.getText().toString();
                showTasks(tag);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        fabAdd.setOnClickListener(v -> {
            String tag = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
            showAddTaskDialog(tag);
        });
        fabFilter.setOnClickListener(v -> showFilterDialog());
        return view;
    }
    private void showFilterDialog() {
        String[] options = {"Filter by Date", "Filter by Priority", "Clear Completed Tasks", "Clear Filter"};
        new android.app.AlertDialog.Builder(getContext())
            .setTitle("Filter Options")
            .setItems(options, (dialog, which) -> {
                if (which == 0) showDateFilterDialog();
                else if (which == 1) showPriorityFilterDialog();
                else if (which == 2) clearCompletedTasks();
                else if (which == 3) { filterDate = null; filterPriority = null; showTasks(getCurrentTag()); }
            })
            .show();
    }
    private void showDateFilterDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            filterDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            showTasks(getCurrentTag());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void showPriorityFilterDialog() {
        String[] priorities = {"Low", "Medium", "High"};
        new android.app.AlertDialog.Builder(getContext())
            .setTitle("Filter by Priority")
            .setItems(priorities, (dialog, which) -> {
                filterPriority = which;
                showTasks(getCurrentTag());
            })
            .setNegativeButton("Clear", (d, w) -> {
                filterPriority = null;
                showTasks(getCurrentTag());
            })
            .show();
    }
    private void clearCompletedTasks() {
        List<Task> completed = db.taskDao().getCompletedTasks();
        for (Task t : completed) db.taskDao().delete(t);
        showTasks(getCurrentTag());
    }
    private String getCurrentTag() {
        return tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
    }
    private void showTasks(String tag) {
        if (filterDate == null && filterPriority == null) {
            taskList = db.taskDao().getTasksByTag(tag);
        } else if (filterDate != null && filterPriority == null) {
            taskList = db.taskDao().getTasksByTagAndDate(tag, filterDate);
        } else if (filterDate == null) {
            taskList = db.taskDao().getTasksByTagAndPriority(tag, filterPriority);
        } else {
            taskList = db.taskDao().getTasksByTagDatePriority(tag, filterDate, filterPriority);
        }
        taskAdapter = new TaskAdapter(getContext(), taskList, new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {}
            @Override
            public void onStatusClick(Task task) {
                task.isCompleted = !task.isCompleted;
                db.taskDao().update(task);
                showTasks(tag);
            }
            @Override
            public void onDeleteClick(Task task) {
                db.taskDao().delete(task);
                showTasks(tag);
            }
        });
        recyclerView.setAdapter(taskAdapter);
    }
    private void showAddTaskDialog(String tag) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_task, null);
        EditText etTaskTitle = dialogView.findViewById(R.id.etTaskTitle);
        EditText etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
        DatePicker datePickerTask = dialogView.findViewById(R.id.datePickerTask);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
        Spinner spinnerTag = dialogView.findViewById(R.id.spinnerTag);

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"Low", "Medium", "High"});
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, tags);
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTag.setAdapter(tagAdapter);
        spinnerTag.setSelection(java.util.Arrays.asList(tags).indexOf(tag));

        new android.app.AlertDialog.Builder(getContext())
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Save", (d, which) -> {
                String title = etTaskTitle.getText().toString();
                String desc = etTaskDescription.getText().toString();
                int priority = spinnerPriority.getSelectedItemPosition();
                String selectedTag = spinnerTag.getSelectedItem().toString();
                int day = datePickerTask.getDayOfMonth();
                int month = datePickerTask.getMonth() + 1;
                int year = datePickerTask.getYear();
                String dueDate = String.format("%04d-%02d-%02d", year, month, day);

                Task newTask = new Task();
                newTask.title = title;
                newTask.description = desc;
                newTask.dueDate = dueDate;
                newTask.priority = priority;
                newTask.isCompleted = false;
                newTask.tag = selectedTag;

                db.taskDao().insert(newTask);
                showTasks(selectedTag);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
} 