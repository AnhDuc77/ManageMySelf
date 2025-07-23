package com.example.manageexpenses.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.manageexpenses.R;
import com.google.android.material.tabs.TabLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.manageexpenses.AppDatabase;
import com.example.manageexpenses.adapter.TransactionAdapter;
import com.example.manageexpenses.adapter.AccountAdapter;
import com.example.manageexpenses.adapter.DebtCreditAdapter;
import com.example.manageexpenses.entity.Transaction;
import com.example.manageexpenses.entity.Account;
import com.example.manageexpenses.entity.DebtCredit;
import androidx.room.Room;
import java.util.ArrayList;
import java.util.List;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import java.util.Calendar;
import android.widget.Toast;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.widget.ImageView;
import android.widget.Button;
import android.content.Intent;
import android.app.Activity;
import com.example.manageexpenses.entity.BillImage;

public class FinanceFragment extends Fragment {
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private AppDatabase db;
    private TransactionAdapter transactionAdapter;
    private AccountAdapter accountAdapter;
    private DebtCreditAdapter debtCreditAdapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private List<Account> accountList = new ArrayList<>();
    private List<DebtCredit> debtCreditList = new ArrayList<>();
    private String[] tabs = {"Transactions", "Accounts", "Debts", "Statistics"};
    private BarChart barChartFinance;
    private FloatingActionButton fabAdd;
    private int currentTab = 0;
    private ActivityResultLauncher<Intent> pickBillImageLauncher;
    private Uri selectedBillImageUri = null;
    private String selectedBillImagePath = null;
    private ImageView currentTransactionImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickBillImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null && currentTransactionImageView != null) {
                        selectedBillImageUri = imageUri;
                        try {
                            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
                            Bitmap bm = BitmapFactory.decodeStream(inputStream);
                            if (bm != null) {
                                currentTransactionImageView.setImageBitmap(bm);
                                selectedBillImagePath = saveBillImageToInternalStorage(bm);
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
        View view = inflater.inflate(R.layout.fragment_finance, container, false);
        tabLayout = view.findViewById(R.id.tabLayoutFinance);
        recyclerView = view.findViewById(R.id.recyclerViewFinance);
        barChartFinance = view.findViewById(R.id.barChartFinance);
        fabAdd = view.findViewById(R.id.fabFinanceAdd);
        fabAdd.setOnClickListener(v -> {
            switch (currentTab) {
                case 0: showAddTransactionDialog(); break;
                case 1: showAddAccountDialog(); break;
                case 2: showAddDebtDialog(); break;
            }
        });
        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "todo_manager.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        for (String t : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(t));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tabLayout.getTabAt(0).select();
        showTab(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                showTab(tab.getPosition());
                fabAdd.setVisibility(tab.getPosition() < 3 ? View.VISIBLE : View.GONE);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        return view;
    }
    private void showTab(int pos) {
        switch (pos) {
            case 0:
                barChartFinance.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                transactionList = db.transactionDao().getPersonalTransactions();
                transactionAdapter = new TransactionAdapter(getContext(), transactionList, t -> {
                    Intent intent = new Intent(getContext(), TransactionDetailActivity.class);
                    intent.putExtra("transactionId", t.id);
                    startActivity(intent);
                });
                recyclerView.setAdapter(transactionAdapter);
                break;
            case 1:
                barChartFinance.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                accountList = db.accountDao().getAllAccounts();
                accountAdapter = new AccountAdapter(getContext(), accountList, new AccountAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Account a) {}
                    @Override
                    public void onEditClick(Account a) { showEditAccountDialog(a); }
                    @Override
                    public void onDeleteClick(Account a) {
                        new AlertDialog.Builder(getContext())
                            .setTitle("Delete")
                            .setMessage("Are you sure you want to delete this account?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                db.accountDao().delete(a);
                                showTab(1);
                            })
                            .setNegativeButton("No", null)
                            .show();
                    }
                });
                recyclerView.setAdapter(accountAdapter);
                break;
            case 2:
                barChartFinance.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                debtCreditList = db.debtCreditDao().getAllDebts();
                debtCreditList.addAll(db.debtCreditDao().getAllCredits());
                debtCreditAdapter = new DebtCreditAdapter(getContext(), debtCreditList, new DebtCreditAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(DebtCredit d) {}
                    @Override
                    public void onEditClick(DebtCredit d) { showEditDebtDialog(d); }
                    @Override
                    public void onDeleteClick(DebtCredit d) {
                        new AlertDialog.Builder(getContext())
                            .setTitle("Delete")
                            .setMessage("Are you sure you want to delete this debt/credit?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                db.debtCreditDao().delete(d);
                                showTab(2);
                            })
                            .setNegativeButton("No", null)
                            .show();
                    }
                    @Override
                    public void onToggleStatus(DebtCredit d) {
                        d.isPaid = !d.isPaid;
                        db.debtCreditDao().update(d);
                        showTab(2);
                    }
                });
                recyclerView.setAdapter(debtCreditAdapter);
                break;
            case 3:
                recyclerView.setVisibility(View.GONE);
                barChartFinance.setVisibility(View.VISIBLE);
                setupBarChart();
                break;
        }
    }
    private void setupBarChart() {
        // Thống kê chi tiêu theo tháng (giả sử lấy 6 tháng gần nhất)
        java.util.List<BarEntry> entries = new java.util.ArrayList<>();
        java.util.List<String> months = new java.util.ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault());
        java.util.Map<String, Float> monthExpense = new java.util.LinkedHashMap<>();
        for (Transaction t : db.transactionDao().getPersonalTransactions()) {
            String month = t.date.substring(0, 7);
            float value = (float) t.amount;
            monthExpense.put(month, monthExpense.getOrDefault(month, 0f) + value);
        }
        int i = 0;
        for (String month : monthExpense.keySet()) {
            entries.add(new BarEntry(i, monthExpense.get(month)));
            months.add(month);
            i++;
        }
        BarDataSet dataSet = new BarDataSet(entries, "Expenses by Month");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData data = new BarData(dataSet);
        data.setValueTextSize(14f);
        barChartFinance.setData(data);
        barChartFinance.getDescription().setEnabled(false);
        barChartFinance.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                return idx >= 0 && idx < months.size() ? months.get(idx) : "";
            }
        });
        barChartFinance.getXAxis().setGranularity(1f);
        barChartFinance.getXAxis().setGranularityEnabled(true);
        barChartFinance.getAxisRight().setEnabled(false);
        barChartFinance.setFitBars(true);
        barChartFinance.animateY(1000);
        barChartFinance.invalidate();
    }
    private void showAddTransactionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_transaction, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        Spinner spinnerAccount = dialogView.findViewById(R.id.spinnerAccount);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        ImageView ivBillImage = dialogView.findViewById(R.id.ivBillImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        currentTransactionImageView = ivBillImage;
        selectedBillImageUri = null;
        selectedBillImagePath = null;
        btnSelectImage.setOnClickListener(v -> pickBillImage());
        // Thiết lập spinner category
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"Food", "Transport", "Shopping", "Other"});
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        // Thiết lập spinner account
        java.util.List<Account> accounts = db.accountDao().getAllAccounts();
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, accounts.stream().map(a -> a.name).toArray(String[]::new));
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccount.setAdapter(accountAdapter);
        new AlertDialog.Builder(getContext())
            .setTitle("Add Transaction")
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
                String title = etTitle.getText().toString().trim();
                String amountStr = etAmount.getText().toString().trim();
                if (title.isEmpty() || amountStr.isEmpty() || accounts.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all required fields!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    double amount = Double.parseDouble(amountStr);
                    Transaction t = new Transaction();
                    t.title = title;
                    t.amount = amount;
                    t.categoryId = spinnerCategory.getSelectedItemPosition();
                    t.date = String.format("%04d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
                    t.note = etNote.getText().toString();
                    t.groupId = 0;
                    t.paidBy = 0;
                    t.accountId = accounts.get(spinnerAccount.getSelectedItemPosition()).id;
                    long transactionId = db.transactionDao().insert(t);
                    // Lưu ảnh hóa đơn nếu có
                    if (selectedBillImagePath != null) {
                        BillImage billImage = new BillImage();
                        billImage.transactionId = (int) transactionId;
                        billImage.imagePath = selectedBillImagePath;
                        db.billImageDao().insert(billImage);
                    }
                    showTab(0);
                } catch (Exception ex) {
                    Toast.makeText(getContext(), "Invalid input!", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void showAddAccountDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_account, null);
        EditText etName = dialogView.findViewById(R.id.etAccountName);
        EditText etBalance = dialogView.findViewById(R.id.etAccountBalance);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerAccountType);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"Cash", "Bank", "E-wallet", "Other"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        new AlertDialog.Builder(getContext())
            .setTitle("Add Account")
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
                Account a = new Account();
                a.name = etName.getText().toString();
                a.balance = Double.parseDouble(etBalance.getText().toString());
                a.type = spinnerType.getSelectedItem().toString();
                db.accountDao().insert(a);
                showTab(1);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void showAddDebtDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_debt, null);
        EditText etName = dialogView.findViewById(R.id.etDebtName);
        EditText etAmount = dialogView.findViewById(R.id.etDebtAmount);
        DatePicker datePicker = dialogView.findViewById(R.id.datePickerDebt);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerDebtType);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"I Owe", "They Owe"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        new AlertDialog.Builder(getContext())
            .setTitle("Add Debt/Credit")
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
                DebtCredit dc = new DebtCredit();
                dc.name = etName.getText().toString();
                dc.amount = Double.parseDouble(etAmount.getText().toString());
                dc.dueDate = String.format("%04d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
                dc.isDebt = spinnerType.getSelectedItemPosition() == 0;
                dc.isPaid = false;
                db.debtCreditDao().insert(dc);
                showTab(2);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void showEditDebtDialog(DebtCredit d) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_debt, null);
        EditText etName = dialogView.findViewById(R.id.etDebtName);
        EditText etAmount = dialogView.findViewById(R.id.etDebtAmount);
        DatePicker datePicker = dialogView.findViewById(R.id.datePickerDebt);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerDebtType);
        etName.setText(d.name);
        etAmount.setText(String.valueOf(d.amount));
        String[] types = {"I Owe", "They Owe"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(d.isDebt ? 0 : 1);
        String[] dateParts = d.dueDate.split("-");
        if (dateParts.length == 3) {
            datePicker.updateDate(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[2]));
        }
        new AlertDialog.Builder(getContext())
            .setTitle("Edit Debt/Credit")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                d.name = etName.getText().toString();
                d.amount = Double.parseDouble(etAmount.getText().toString());
                d.dueDate = String.format("%04d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
                d.isDebt = spinnerType.getSelectedItemPosition() == 0;
                // Không reset isPaid khi sửa
                db.debtCreditDao().update(d);
                showTab(2);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void showEditAccountDialog(Account a) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_account, null);
        EditText etName = dialogView.findViewById(R.id.etAccountName);
        EditText etBalance = dialogView.findViewById(R.id.etAccountBalance);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerAccountType);
        etName.setText(a.name);
        etBalance.setText(String.valueOf(a.balance));
        String[] types = {"Cash", "Bank", "E-wallet", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(java.util.Arrays.asList(types).indexOf(a.type));
        new AlertDialog.Builder(getContext())
            .setTitle("Edit Account")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                a.name = etName.getText().toString();
                a.balance = Double.parseDouble(etBalance.getText().toString());
                a.type = spinnerType.getSelectedItem().toString();
                db.accountDao().update(a);
                showTab(1);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void pickBillImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickBillImageLauncher.launch(intent);
    }
    private String saveBillImageToInternalStorage(Bitmap bitmap) {
        try {
            File file = new File(requireContext().getFilesDir(), "bill_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 