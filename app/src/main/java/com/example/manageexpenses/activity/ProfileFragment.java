package com.example.manageexpenses.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.manageexpenses.AppDatabase;
import com.example.manageexpenses.R;
import androidx.room.Room;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import android.database.Cursor;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;
import java.io.FileOutputStream;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

public class ProfileFragment extends Fragment {
    private AppDatabase db;
    private TextView tvStats;
    private ImageView ivAvatar;
    private Button btnUploadAvatar;
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final String PREFS = "profile_prefs";
    private static final String KEY_AVATAR_PATH = "avatar_path";
    private static final String KEY_AVATAR_URI = "avatar_uri";
    private static final String KEY_AVATAR_FILE = "avatar_file";
    private PieChart pieChartTasks;
    private TextView tvCompleted, tvPending, tvExpense, tvBalance, tvDebts, tvCredits;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Button btnPickDate;
    private Spinner spinnerStatType;
    private java.util.Calendar selectedDate;
    private String[] statTypes = {"Day", "Month"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
                            if (inputStream != null) {
                                Bitmap bm = BitmapFactory.decodeStream(inputStream);
                                if (bm != null) {
                                    String filePath = saveAvatarToInternalStorage(bm);
                                    if (filePath != null) {
                                        // Xóa file cũ nếu có
                                        File oldFile = new File(requireContext().getFilesDir(), "avatar.jpg");
                                        if (oldFile.exists()) oldFile.delete();
                                        saveAvatarFilePath(filePath);
                                        if (ivAvatar != null && isAdded()) {
                                            Glide.with(requireContext())
                                                .load(new File(filePath))
                                                .circleCrop()
                                                .signature(new ObjectKey(System.currentTimeMillis()))
                                                .into(ivAvatar);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "todo_manager.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        ivAvatar = view.findViewById(R.id.ivAvatar);
        btnUploadAvatar = view.findViewById(R.id.btnUploadAvatar);
        tvCompleted = view.findViewById(R.id.tvProfileCompleted);
        tvPending = view.findViewById(R.id.tvProfilePending);
        tvExpense = view.findViewById(R.id.tvProfileExpense);
        tvBalance = view.findViewById(R.id.tvProfileBalance);
        tvDebts = view.findViewById(R.id.tvProfileDebts);
        tvCredits = view.findViewById(R.id.tvProfileCredits);
        btnPickDate = view.findViewById(R.id.btnPickDate);
        spinnerStatType = view.findViewById(R.id.spinnerStatType);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, statTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatType.setAdapter(adapter);

        selectedDate = java.util.Calendar.getInstance();
        updatePickDateButton();
        btnPickDate.setOnClickListener(v -> showDatePicker());
        spinnerStatType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateStatistics();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String avatarPath = getAvatarFilePath();
        if (avatarPath != null) {
            try {
                Glide.with(requireContext())
                    .load(new File(avatarPath))
                    .circleCrop()
                    .signature(new ObjectKey(System.currentTimeMillis()))
                    .into(ivAvatar);
            } catch (Exception e) {
                e.printStackTrace();
                ivAvatar.setImageResource(R.drawable.ic_camera);
            }
        }
        btnUploadAvatar.setOnClickListener(v -> pickImage());
        int completed = 0, pending = 0, debtCount = 0, creditCount = 0;
        Double totalExpense = 0.0, totalBalance = 0.0;
        try {
            completed = db.taskDao().getCompletedTaskCount();
            pending = db.taskDao().getPendingTaskCount();
            totalExpense = db.transactionDao().getTotalPersonalExpenses();
            totalBalance = db.accountDao().getTotalBalance();
            java.util.List<com.example.manageexpenses.entity.DebtCredit> debts = db.debtCreditDao().getAllDebts();
            java.util.List<com.example.manageexpenses.entity.DebtCredit> credits = db.debtCreditDao().getAllCredits();
            debtCount = debts != null ? debts.size() : 0;
            creditCount = credits != null ? credits.size() : 0;
        } catch (Exception e) {
            completed = 0; pending = 0; totalExpense = 0.0; totalBalance = 0.0; debtCount = 0; creditCount = 0;
        }
        try {
            tvCompleted.setText("Completed tasks: " + completed);
            tvPending.setText("Pending tasks: " + pending);
            tvExpense.setText("Total expenses: " + (totalExpense != null ? String.format("%,.0f", totalExpense) : "0"));
            tvBalance.setText("Total balance: " + (totalBalance != null ? String.format("%,.0f", totalBalance) : "0"));
            tvDebts.setText("Debts: " + debtCount);
            tvCredits.setText("Credits: " + creditCount);
        } catch (Exception e) {
            // ignore
        }
        pieChartTasks = view.findViewById(R.id.pieChartTasks);
        setupPieChart(completed, pending);

        updateStatistics();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        String avatarPath = getAvatarFilePath();
        if (avatarPath != null && ivAvatar != null && isAdded()) {
            try {
                Glide.with(requireContext())
                    .load(new File(avatarPath))
                    .circleCrop()
                    .signature(new ObjectKey(System.currentTimeMillis()))
                    .into(ivAvatar);
            } catch (Exception e) {
                e.printStackTrace();
                ivAvatar.setImageResource(R.drawable.ic_camera);
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        try (Cursor cursor = requireActivity().getContentResolver().query(contentUri, proj, null, null, null)) {
            if (cursor == null) return null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
    }
    private String saveAvatarToInternalStorage(Bitmap bitmap) {
        try {

            String oldPath = getAvatarFilePath();
            if (oldPath != null) {
                File oldFile = new File(oldPath);
                if (oldFile.exists()) oldFile.delete();
            }

            String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
            File file = new File(requireContext().getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private void saveAvatarPath(String path) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_AVATAR_PATH, path).apply();
    }
    private String getAvatarPath() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_AVATAR_PATH, null);
    }
    private void saveAvatarUri(String uri) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_AVATAR_URI, uri).apply();
    }
    private String getAvatarUri() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_AVATAR_URI, null);
    }
    private void saveAvatarFilePath(String path) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_AVATAR_FILE, path).apply();
    }
    private String getAvatarFilePath() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_AVATAR_FILE, null);
    }

    private void setupPieChart(int completed, int pending) {
        java.util.List<PieEntry> entries = new java.util.ArrayList<>();
        entries.add(new PieEntry(completed, "Completed"));
        entries.add(new PieEntry(pending, "Pending"));
        PieDataSet dataSet = new PieDataSet(entries, "Tasks");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        pieChartTasks.setData(data);
        pieChartTasks.getDescription().setEnabled(false);
        pieChartTasks.setCenterText("Tasks");
        pieChartTasks.animateY(1000);
        pieChartTasks.invalidate();
    }
    private void updatePickDateButton() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        btnPickDate.setText(sdf.format(selectedDate.getTime()));
    }
    private void showDatePicker() {
        int year = selectedDate.get(java.util.Calendar.YEAR);
        int month = selectedDate.get(java.util.Calendar.MONTH);
        int day = selectedDate.get(java.util.Calendar.DAY_OF_MONTH);
        new android.app.DatePickerDialog(requireContext(), (view, y, m, d) -> {
            selectedDate.set(y, m, d);
            updatePickDateButton();
            updateStatistics();
        }, year, month, day).show();
    }
    private void updateStatistics() {
        int completed = 0, pending = 0;
        int statType = spinnerStatType.getSelectedItemPosition();
        java.text.SimpleDateFormat sdfDay = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.text.SimpleDateFormat sdfMonth = new java.text.SimpleDateFormat("yyyy-MM");
        String dateStr = sdfDay.format(selectedDate.getTime());
        String monthStr = sdfMonth.format(selectedDate.getTime());
        try {
            if (statType == 0) {
                completed = db.taskDao().getCompletedTaskCountByDate(dateStr);
                pending = db.taskDao().getPendingTaskCountByDate(dateStr);
            } else {
                completed = db.taskDao().getCompletedTaskCountByMonth(monthStr);
                pending = db.taskDao().getPendingTaskCountByMonth(monthStr);
            }
        } catch (Exception e) {
            completed = 0; pending = 0;
        }
        setupPieChart(completed, pending);
        try {
            tvCompleted.setText("Completed tasks: " + completed);
            tvPending.setText("Pending tasks: " + pending);
        } catch (Exception e) {}
    }
} 