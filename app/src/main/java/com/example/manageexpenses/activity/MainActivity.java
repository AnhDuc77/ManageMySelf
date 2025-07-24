package com.example.manageexpenses.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manageexpenses.AppDatabase;
import com.example.manageexpenses.R;
import com.example.manageexpenses.adapter.TaskAdapter;
import com.example.manageexpenses.adapter.TransactionAdapter;
import com.example.manageexpenses.entity.Task;
import com.example.manageexpenses.entity.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.room.Room;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.app.DatePickerDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private AppDatabase db;
    private TransactionAdapter transactionAdapter;
    private TaskAdapter taskAdapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private List<Task> taskList = new ArrayList<>();
    private String[] tags = {"Work", "Home", "Groceries", "Study", "Finance"};
    private AlertDialog taskDialog;
    private EditText etTaskTitle, etTaskDescription;
    private DatePicker datePickerTask;
    private Spinner spinnerPriority, spinnerTag;
    private String filterDate = null;
    private Integer filterPriority = null;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private Integer openReminderId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Xin quyền notification cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNav = findViewById(R.id.bottomNav);
        // Lấy openReminderId nếu có
        if (getIntent() != null && getIntent().hasExtra("openReminderId")) {
            openReminderId = getIntent().getIntExtra("openReminderId", 0);
        }
        // Mặc định hiển thị TodoFragment
        setToolbarTitle("To-Do List");
        TodoFragment todoFragment = new TodoFragment();
        if (openReminderId != null) {
            Bundle args = new Bundle();
            args.putInt("openReminderId", openReminderId);
            todoFragment.setArguments(args);
        }
        replaceFragment(todoFragment);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            String title = "";
            if (item.getItemId() == R.id.nav_todo) {
                selected = new TodoFragment();
                title = "To-Do List";
            } else if (item.getItemId() == R.id.nav_finance) {
                selected = new FinanceFragment();
                title = "Finance";
            } else if (item.getItemId() == R.id.nav_profile) {
                selected = new ProfileFragment();
                title = "Profile";
            }
            if (selected != null) {
                setToolbarTitle(title);
                replaceFragment(selected);
            }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }
//    private void showFinance() {
//        transactionList = db.transactionDao().getPersonalTransactions();
//        transactionAdapter = new TransactionAdapter(this, transactionList, t -> {
//            // TODO: Mở chi tiết transaction
//            Toast.makeText(this, "Transaction: " + t.title, Toast.LENGTH_SHORT).show();
//        });
//        recyclerView.setAdapter(transactionAdapter);
//    }
//    private void showTasks(String tag) {
//        if (filterDate == null && filterPriority == null) {
//            taskList = db.taskDao().getTasksByTag(tag);
//        } else if (filterDate != null && filterPriority == null) {
//            taskList = db.taskDao().getTasksByTagAndDate(tag, filterDate);
//        } else if (filterDate == null) {
//            taskList = db.taskDao().getTasksByTagAndPriority(tag, filterPriority);
//        } else {
//            taskList = db.taskDao().getTasksByTagDatePriority(tag, filterDate, filterPriority);
//        }
//        taskAdapter = new TaskAdapter(this, taskList, new TaskAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(Task task) {
//                // Có thể mở dialog sửa task nếu muốn
//            }
//            @Override
//            public void onStatusClick(Task task) {
//                task.isCompleted = !task.isCompleted;
//                db.taskDao().update(task);
//                showTasks(tag);
//            }
//            @Override
//            public void onDeleteClick(Task task) {
//                db.taskDao().delete(task);
//                showTasks(tag);
//            }
//        });
//        recyclerView.setAdapter(taskAdapter);
//    }
//    private void showAddTaskDialog(String tag) {
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_task, null);
//        etTaskTitle = dialogView.findViewById(R.id.etTaskTitle);
//        etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
//        datePickerTask = dialogView.findViewById(R.id.datePickerTask);
//        spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
//        spinnerTag = dialogView.findViewById(R.id.spinnerTag);
//
//        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Low", "Medium", "High"});
//        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerPriority.setAdapter(priorityAdapter);
//
//        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tags);
//        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerTag.setAdapter(tagAdapter);
//        spinnerTag.setSelection(java.util.Arrays.asList(tags).indexOf(tag));
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this)
//            .setTitle("Add Task")
//            .setView(dialogView)
//            .setPositiveButton("Save", (d, which) -> {
//                String title = etTaskTitle.getText().toString();
//                String desc = etTaskDescription.getText().toString();
//                int priority = spinnerPriority.getSelectedItemPosition();
//                String selectedTag = spinnerTag.getSelectedItem().toString();
//                int day = datePickerTask.getDayOfMonth();
//                int month = datePickerTask.getMonth() + 1;
//                int year = datePickerTask.getYear();
//                String dueDate = String.format("%04d-%02d-%02d", year, month, day);
//
//                Task newTask = new Task();
//                newTask.title = title;
//                newTask.description = desc;
//                newTask.dueDate = dueDate;
//                newTask.priority = priority;
//                newTask.isCompleted = false;
//                newTask.tag = selectedTag;
//
//                db.taskDao().insert(newTask);
////                showTasks(selectedTag);
//            })
//            .setNegativeButton("Cancel", null);
//        taskDialog = builder.create();
//        taskDialog.show();
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_task_filter, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_filter_date) {
//            showDateFilterDialog();
//            return true;
//        } else if (item.getItemId() == R.id.action_filter_priority) {
//            showPriorityFilterDialog();
//            return true;
//        } else if (item.getItemId() == R.id.action_clear_completed) {
//            clearCompletedTasks();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    private void showDateFilterDialog() {
//        Calendar calendar = Calendar.getInstance();
//        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
//            filterDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
//            showTasks(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString());
//        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
//    }
//
//    private void showPriorityFilterDialog() {
//        String[] priorities = {"Low", "Medium", "High"};
//        new AlertDialog.Builder(this)
//            .setTitle("Filter by Priority")
//            .setItems(priorities, (dialog, which) -> {
//                filterPriority = which;
//                showTasks(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString());
//            })
//            .setNegativeButton("Clear", (d, w) -> {
//                filterPriority = null;
//                showTasks(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString());
//            })
//            .show();
//    }
//
//    private void clearCompletedTasks() {
//        List<Task> completed = db.taskDao().getCompletedTasks();
//        for (Task t : completed) db.taskDao().delete(t);
//        showTasks(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString());
//    }

    private void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
} 