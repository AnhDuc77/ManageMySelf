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
import com.example.manageexpenses.adapter.ReminderTaskAdapter;
import com.example.manageexpenses.entity.ReminderTask;
import android.widget.TimePicker;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.GridLayout;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import com.example.manageexpenses.receiver.ReminderReceiver;
import android.content.Context;
import android.util.Log;

public class TodoFragment extends Fragment {
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabFilter;
    private AppDatabase db;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private String[] tags = {"Work", "Home", "Groceries", "Study", "Finance", "Reminder"};
    private String filterDate = null;
    private Integer filterPriority = null;
    private ReminderTaskAdapter reminderTaskAdapter;
    private List<ReminderTask> reminderList = new ArrayList<>();

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

        Bundle args = getArguments();
        boolean openedFromReminder = false;
        if (args != null && args.containsKey("openReminderId")) {
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                if (tabLayout.getTabAt(i).getText().toString().equals("Reminder")) {
                    tabLayout.getTabAt(i).select();
                    openedFromReminder = true;
                    break;
                }
            }
        }
        if (openedFromReminder) {
            showReminders();
        } else {
            String firstTag = tags[0];
            tabLayout.getTabAt(0).select();
            showTasks(firstTag);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tag = tab.getText().toString();
                if (tag.equals("Reminder")) showReminders();
                else showTasks(tag);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        fabAdd.setOnClickListener(v -> {
            String tag = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
            if (tag.equals("Reminder")) showAddReminderDialog();
            else showAddTaskDialog(tag);
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
            public void onEditClick(Task task) {
                showEditTaskDialog(task, tag);
            }
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
    private void showReminders() {
        reminderList = db.reminderTaskDao().getAll();
        reminderTaskAdapter = new ReminderTaskAdapter(getContext(), reminderList, new ReminderTaskAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(ReminderTask task) {
                showEditReminderDialog(task);
            }
            @Override
            public void onDeleteClick(ReminderTask task) {
                cancelReminder(task);
                db.reminderTaskDao().delete(task);
                showReminders();
            }
            @Override
            public void onToggleEnable(ReminderTask task) {
                task.isEnabled = !task.isEnabled;
                db.reminderTaskDao().update(task);
                if (task.isEnabled) scheduleReminder(task); else cancelReminder(task);
                showReminders();
            }
        });
        recyclerView.setAdapter(reminderTaskAdapter);
    }
    private void scheduleReminder(ReminderTask reminder) {
        try {
            if (!reminder.isEnabled) return;
            Context ctx = getContext();
            if (ctx == null) return;
            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            cancelReminder(reminder); // Hủy cũ trước khi đặt mới
            if (reminder.repeatType == 0) {
                // Daily: chỉ đặt lần tiếp theo
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, reminder.hour);
                cal.set(java.util.Calendar.MINUTE, reminder.minute);
                cal.set(java.util.Calendar.SECOND, 0);
                if (cal.getTimeInMillis() < System.currentTimeMillis()) cal.add(java.util.Calendar.DATE, 1);
                PendingIntent pi = getReminderPendingIntent(reminder);
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                Log.d("ReminderAlarm", "Scheduled daily (exact): id=" + reminder.id + ", time=" + cal.getTime());
            } else if (reminder.repeatDays != null && !reminder.repeatDays.isEmpty()) {
                // Custom days: đặt cho từng ngày đã chọn
                String[] days = reminder.repeatDays.split(",");
                for (String d : days) {
                    int dayOfWeek = Integer.parseInt(d);
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.set(java.util.Calendar.HOUR_OF_DAY, reminder.hour);
                    cal.set(java.util.Calendar.MINUTE, reminder.minute);
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.DAY_OF_WEEK, dayOfWeek + 1); // Calendar: 1=Sun
                    if (cal.getTimeInMillis() < System.currentTimeMillis()) cal.add(java.util.Calendar.WEEK_OF_YEAR, 1);
                    PendingIntent pi = getReminderPendingIntent(reminder, dayOfWeek);
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                    Log.d("ReminderAlarm", "Scheduled custom (exact): id=" + reminder.id + ", day=" + dayOfWeek + ", time=" + cal.getTime());
                }
            }
        } catch (Exception e) {
            Log.e("ReminderAlarm", "scheduleReminder error: " + e);
        }
    }
    private void cancelReminder(ReminderTask reminder) {
        try {
            Context ctx = getContext();
            if (ctx == null) return;
            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            if (reminder.repeatType == 0) {
                PendingIntent pi = getReminderPendingIntent(reminder);
                am.cancel(pi);
                Log.d("ReminderAlarm", "Cancel daily: id=" + reminder.id);
            } else if (reminder.repeatDays != null && !reminder.repeatDays.isEmpty()) {
                String[] days = reminder.repeatDays.split(",");
                for (String d : days) {
                    int dayOfWeek = Integer.parseInt(d);
                    PendingIntent pi = getReminderPendingIntent(reminder, dayOfWeek);
                    am.cancel(pi);
                    Log.d("ReminderAlarm", "Cancel custom: id=" + reminder.id + ", day=" + dayOfWeek);
                }
            }
        } catch (Exception e) {
            Log.e("ReminderAlarm", "cancelReminder error: " + e);
        }
    }
    private PendingIntent getReminderPendingIntent(ReminderTask reminder) {
        Intent intent = new Intent(getContext(), ReminderReceiver.class);
        intent.putExtra("reminderId", reminder.id);
        intent.putExtra("title", reminder.title);
        intent.putExtra("desc", reminder.description);
        return PendingIntent.getBroadcast(getContext(), reminder.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    private PendingIntent getReminderPendingIntent(ReminderTask reminder, int dayOfWeek) {
        Intent intent = new Intent(getContext(), ReminderReceiver.class);
        intent.putExtra("reminderId", reminder.id * 10 + dayOfWeek);
        intent.putExtra("title", reminder.title);
        intent.putExtra("desc", reminder.description);
        return PendingIntent.getBroadcast(getContext(), reminder.id * 10 + dayOfWeek, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    private void showAddReminderDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_reminder, null);
        EditText etTitle = dialogView.findViewById(R.id.etReminderTitle);
        EditText etDesc = dialogView.findViewById(R.id.etReminderDescription);
        TimePicker timePicker = dialogView.findViewById(R.id.timePickerReminder);
        timePicker.setIs24HourView(true);
        RadioGroup rgRepeatType = dialogView.findViewById(R.id.rgRepeatType);
        RadioButton rbDaily = dialogView.findViewById(R.id.rbDaily);
        RadioButton rbCustom = dialogView.findViewById(R.id.rbCustom);
        TextView tvRepeatOn = dialogView.findViewById(R.id.tvRepeatOn);
        GridLayout gridRepeatDays = dialogView.findViewById(R.id.gridRepeatDays);
        CheckBox[] cbDays = new CheckBox[] {
            dialogView.findViewById(R.id.cbSun), dialogView.findViewById(R.id.cbMon), dialogView.findViewById(R.id.cbTue),
            dialogView.findViewById(R.id.cbWed), dialogView.findViewById(R.id.cbThu), dialogView.findViewById(R.id.cbFri), dialogView.findViewById(R.id.cbSat)
        };
        CheckBox cbEnabled = dialogView.findViewById(R.id.cbReminderEnabled);
        rgRepeatType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCustom) {
                tvRepeatOn.setVisibility(View.VISIBLE);
                gridRepeatDays.setVisibility(View.VISIBLE);
            } else {
                tvRepeatOn.setVisibility(View.GONE);
                gridRepeatDays.setVisibility(View.GONE);
            }
        });
        new android.app.AlertDialog.Builder(getContext())
            .setTitle("Add Reminder")
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
                String title = etTitle.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                int repeatType = rbDaily.isChecked() ? 0 : 1;
                StringBuilder repeatDays = new StringBuilder();
                if (repeatType == 1) {
                    for (int i = 0; i < 7; i++) if (cbDays[i].isChecked()) repeatDays.append(i).append(",");
                    if (repeatDays.length() > 0) repeatDays.setLength(repeatDays.length() - 1);
                }
                boolean isEnabled = cbEnabled.isChecked();
                ReminderTask reminder = new ReminderTask();
                reminder.title = title;
                reminder.description = desc;
                reminder.hour = hour;
                reminder.minute = minute;
                reminder.repeatType = repeatType;
                reminder.repeatDays = repeatDays.toString();
                reminder.isEnabled = isEnabled;
                db.reminderTaskDao().insert(reminder);
                scheduleReminder(reminder);
                showReminders();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void showEditReminderDialog(ReminderTask reminder) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_reminder, null);
        EditText etTitle = dialogView.findViewById(R.id.etReminderTitle);
        EditText etDesc = dialogView.findViewById(R.id.etReminderDescription);
        TimePicker timePicker = dialogView.findViewById(R.id.timePickerReminder);
        timePicker.setIs24HourView(true);
        RadioGroup rgRepeatType = dialogView.findViewById(R.id.rgRepeatType);
        RadioButton rbDaily = dialogView.findViewById(R.id.rbDaily);
        RadioButton rbCustom = dialogView.findViewById(R.id.rbCustom);
        TextView tvRepeatOn = dialogView.findViewById(R.id.tvRepeatOn);
        GridLayout gridRepeatDays = dialogView.findViewById(R.id.gridRepeatDays);
        CheckBox[] cbDays = new CheckBox[] {
            dialogView.findViewById(R.id.cbSun), dialogView.findViewById(R.id.cbMon), dialogView.findViewById(R.id.cbTue),
            dialogView.findViewById(R.id.cbWed), dialogView.findViewById(R.id.cbThu), dialogView.findViewById(R.id.cbFri), dialogView.findViewById(R.id.cbSat)
        };
        CheckBox cbEnabled = dialogView.findViewById(R.id.cbReminderEnabled);

        etTitle.setText(reminder.title);
        etDesc.setText(reminder.description);
        timePicker.setHour(reminder.hour);
        timePicker.setMinute(reminder.minute);
        if (reminder.repeatType == 0) {
            rbDaily.setChecked(true);
            tvRepeatOn.setVisibility(View.GONE);
            gridRepeatDays.setVisibility(View.GONE);
        } else {
            rbCustom.setChecked(true);
            tvRepeatOn.setVisibility(View.VISIBLE);
            gridRepeatDays.setVisibility(View.VISIBLE);
            if (reminder.repeatDays != null && !reminder.repeatDays.isEmpty()) {
                String[] days = reminder.repeatDays.split(",");
                for (String d : days) {
                    int idx = Integer.parseInt(d);
                    if (idx >= 0 && idx < 7) cbDays[idx].setChecked(true);
                }
            }
        }
        cbEnabled.setChecked(reminder.isEnabled);
        rgRepeatType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCustom) {
                tvRepeatOn.setVisibility(View.VISIBLE);
                gridRepeatDays.setVisibility(View.VISIBLE);
            } else {
                tvRepeatOn.setVisibility(View.GONE);
                gridRepeatDays.setVisibility(View.GONE);
            }
        });
        new android.app.AlertDialog.Builder(getContext())
            .setTitle("Edit Reminder")
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
                String title = etTitle.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                int repeatType = rbDaily.isChecked() ? 0 : 1;
                StringBuilder repeatDays = new StringBuilder();
                if (repeatType == 1) {
                    for (int i = 0; i < 7; i++) if (cbDays[i].isChecked()) repeatDays.append(i).append(",");
                    if (repeatDays.length() > 0) repeatDays.setLength(repeatDays.length() - 1);
                }
                boolean isEnabled = cbEnabled.isChecked();
                reminder.title = title;
                reminder.description = desc;
                reminder.hour = hour;
                reminder.minute = minute;
                reminder.repeatType = repeatType;
                reminder.repeatDays = repeatDays.toString();
                reminder.isEnabled = isEnabled;
                db.reminderTaskDao().update(reminder);
                scheduleReminder(reminder);
                showReminders();
            })
            .setNegativeButton("Cancel", null)
            .show();
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
    private void showEditTaskDialog(Task task, String tag) {
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
        spinnerTag.setSelection(java.util.Arrays.asList(tags).indexOf(task.tag));

        etTaskTitle.setText(task.title);
        etTaskDescription.setText(task.description);
        spinnerPriority.setSelection(task.priority);
        String[] dateParts = task.dueDate.split("-");
        if (dateParts.length == 3) datePickerTask.updateDate(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[2]));

        new android.app.AlertDialog.Builder(getContext())
            .setTitle("Edit Task")
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
                task.title = title;
                task.description = desc;
                task.dueDate = dueDate;
                task.priority = priority;
                task.tag = selectedTag;
                db.taskDao().update(task);
                showTasks(selectedTag);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
} 