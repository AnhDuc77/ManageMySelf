package com.example.manageexpenses.util;

import com.example.manageexpenses.entity.Account;
import com.example.manageexpenses.entity.Transaction;
import java.util.List;

public class FinanceUtils {
    /**
     * Tính tổng tài sản (tổng số dư các tài khoản)
     */
    public static double getTotalAssets(List<Account> accounts) {
        double total = 0;
        for (Account acc : accounts) {
            total += acc.balance;
        }
        return total;
    }

    /**
     * Trừ tiền thủ công từ tài khoản khi người dùng nhấn nút Deduct
     */
    public static boolean deductAmount(Account account, double amount) {
        if (account.balance >= amount) {
            account.balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * Tính tổng chi tiêu theo categoryId
     */
    public static double getTotalByCategory(List<Transaction> transactions, int categoryId) {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.categoryId == categoryId) {
                total += t.amount;
            }
        }
        return total;
    }
} 