package com.example.manageexpenses.util;

import com.example.manageexpenses.entity.Transaction;
import com.example.manageexpenses.entity.Member;
import com.example.manageexpenses.entity.TransactionMember;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for splitting group expenses and calculating debts/credits for each member.
 */
public class ExpenseSplitter {
    /**
     * Tính số tiền mỗi thành viên cần trả hoặc nhận trong nhóm.
     * @param transactions Danh sách giao dịch nhóm
     * @param members Danh sách thành viên nhóm
     * @param transactionMembers Danh sách liên kết giao dịch-thành viên
     * @return Map<MemberId, Số tiền dương: nhận, âm: phải trả>
     */
    public static Map<Integer, Double> calculateDebts(List<Transaction> transactions, List<Member> members, List<TransactionMember> transactionMembers) {
        Map<Integer, Double> paid = new HashMap<>();
        Map<Integer, Double> owes = new HashMap<>();
        for (Member m : members) {
            paid.put(m.id, 0.0);
            owes.put(m.id, 0.0);
        }
        for (Transaction t : transactions) {
            // Lấy các thành viên liên quan giao dịch này
            int count = 0;
            for (TransactionMember tm : transactionMembers) {
                if (tm.transactionId == t.id) count++;
            }
            if (count == 0) continue;
            double perPerson = t.amount / count;
            for (TransactionMember tm : transactionMembers) {
                if (tm.transactionId == t.id) {
                    owes.put(tm.memberId, owes.get(tm.memberId) + perPerson);
                }
            }
            // Người trả tiền
            if (paid.containsKey(t.paidBy)) {
                paid.put(t.paidBy, paid.get(t.paidBy) + t.amount);
            }
        }
        // Tính kết quả: nhận (+), trả (-)
        Map<Integer, Double> result = new HashMap<>();
        for (Member m : members) {
            double value = paid.get(m.id) - owes.get(m.id);
            result.put(m.id, value);
        }
        return result;
    }
} 