package com.pet.service;

import com.pet.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WalletService {
    
    // 模拟数据存储
    private final Map<String, Map<String, Object>> wallets = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> transactions = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> withdrawals = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> accounts = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> rechargeOrders = new ConcurrentHashMap<>();

    // 获取或创建钱包
    public Map<String, Object> getWallet(String userId) {
        return wallets.computeIfAbsent(userId, k -> createWallet(userId));
    }

    private Map<String, Object> createWallet(String userId) {
        Map<String, Object> wallet = new HashMap<>();
        wallet.put("id", UUID.randomUUID().toString());
        wallet.put("userId", userId);
        wallet.put("userType", "pet_owner");
        wallet.put("balance", 10000); // 初始余额 100 元（单位：分）
        wallet.put("frozenBalance", 0);
        wallet.put("totalIncome", 0);
        wallet.put("totalWithdraw", 0);
        wallet.put("status", "active");
        wallet.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        wallet.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return wallet;
    }

    public Map<String, Object> getBalance(String userId) {
        Map<String, Object> wallet = getWallet(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("balance", wallet.get("balance"));
        result.put("frozenBalance", wallet.get("frozenBalance"));
        return result;
    }

    public Map<String, Object> createRechargeOrder(String userId, Map<String, Object> data) {
        Map<String, Object> wallet = getWallet(userId);
        
        Number amountNum = (Number) data.get("amount");
        int amount = (int) (amountNum.doubleValue() * 100); // 元转分
        String paymentMethod = (String) data.getOrDefault("paymentMethod", "alipay");
        
        String orderId = UUID.randomUUID().toString();
        Map<String, Object> order = new HashMap<>();
        order.put("id", orderId);
        order.put("walletId", wallet.get("id"));
        order.put("amount", amount);
        order.put("paymentMethod", paymentMethod);
        order.put("status", "paid"); // 模拟直接支付成功
        order.put("paymentOrderId", "PAY" + System.currentTimeMillis());
        order.put("paidAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        order.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        order.put("expiredAt", LocalDateTime.now().plusMinutes(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        rechargeOrders.put(orderId, order);
        
        // 更新钱包余额
        int currentBalance = (int) wallet.get("balance");
        wallet.put("balance", currentBalance + amount);
        wallet.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 添加交易记录
        addTransaction(userId, "recharge", amount, 0, "充值 - " + paymentMethod, null);
        
        return order;
    }

    public Map<String, Object> getRechargeStatus(String userId, String orderId) {
        return rechargeOrders.getOrDefault(orderId, new HashMap<>());
    }

    public Map<String, Object> createWithdrawal(String userId, Map<String, Object> data) {
        Map<String, Object> wallet = getWallet(userId);
        
        Number amountNum = (Number) data.get("amount");
        int amount = (int) (amountNum.doubleValue() * 100); // 元转分
        String accountId = (String) data.get("accountId");
        
        int currentBalance = (int) wallet.get("balance");
        if (amount > currentBalance) {
            throw new RuntimeException("余额不足");
        }
        
        // 计算手续费 (1%, 最低1元)
        int fee = Math.max(amount / 100, 100);
        int actualAmount = amount - fee;
        
        String withdrawalId = UUID.randomUUID().toString();
        Map<String, Object> withdrawal = new HashMap<>();
        withdrawal.put("id", withdrawalId);
        withdrawal.put("walletId", wallet.get("id"));
        withdrawal.put("amount", amount);
        withdrawal.put("fee", fee);
        withdrawal.put("actualAmount", actualAmount);
        withdrawal.put("accountId", accountId);
        withdrawal.put("status", "pending");
        withdrawal.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        withdrawals.computeIfAbsent(userId, k -> new ArrayList<>()).add(0, withdrawal);
        
        // 冻结余额
        wallet.put("balance", currentBalance - amount);
        wallet.put("frozenBalance", (int) wallet.get("frozenBalance") + amount);
        wallet.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 添加交易记录
        addTransaction(userId, "withdrawal", amount, fee, "提现申请", withdrawalId);
        
        return withdrawal;
    }

    public PageResult<Map<String, Object>> getWithdrawals(String userId, int page, int pageSize) {
        List<Map<String, Object>> userWithdrawals = withdrawals.getOrDefault(userId, new ArrayList<>());
        int total = userWithdrawals.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        
        List<Map<String, Object>> list = start < total ? userWithdrawals.subList(start, end) : new ArrayList<>();
        return PageResult.of(list, page, pageSize, total);
    }

    public void cancelWithdrawal(String userId, String id) {
        List<Map<String, Object>> userWithdrawals = withdrawals.getOrDefault(userId, new ArrayList<>());
        Map<String, Object> wallet = getWallet(userId);
        
        for (Map<String, Object> w : userWithdrawals) {
            if (id.equals(w.get("id")) && "pending".equals(w.get("status"))) {
                w.put("status", "cancelled");
                // 解冻余额
                int amount = (int) w.get("amount");
                wallet.put("balance", (int) wallet.get("balance") + amount);
                wallet.put("frozenBalance", (int) wallet.get("frozenBalance") - amount);
                break;
            }
        }
    }

    public PageResult<Map<String, Object>> getTransactions(String userId, int page, int pageSize, 
            String type, String startDate, String endDate) {
        List<Map<String, Object>> userTx = transactions.getOrDefault(userId, new ArrayList<>());
        
        // 过滤
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> tx : userTx) {
            if (type != null && !type.isEmpty() && !type.equals(tx.get("type"))) continue;
            filtered.add(tx);
        }
        
        int total = filtered.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        
        List<Map<String, Object>> list = start < total ? filtered.subList(start, end) : new ArrayList<>();
        return PageResult.of(list, page, pageSize, total);
    }

    public List<Map<String, Object>> getWithdrawalAccounts(String userId) {
        return accounts.computeIfAbsent(userId, k -> {
            // 创建默认账户
            List<Map<String, Object>> defaultAccounts = new ArrayList<>();
            Map<String, Object> account = new HashMap<>();
            account.put("id", UUID.randomUUID().toString());
            account.put("userId", userId);
            account.put("type", "alipay");
            account.put("accountName", "测试用户");
            account.put("accountNumber", "138****8888");
            account.put("isDefault", true);
            account.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            defaultAccounts.add(account);
            return defaultAccounts;
        });
    }

    public Map<String, Object> addWithdrawalAccount(String userId, Map<String, Object> data) {
        List<Map<String, Object>> userAccounts = accounts.computeIfAbsent(userId, k -> new ArrayList<>());
        
        Map<String, Object> account = new HashMap<>();
        account.put("id", UUID.randomUUID().toString());
        account.put("userId", userId);
        account.put("type", data.get("type"));
        account.put("accountName", data.get("accountName"));
        account.put("accountNumber", maskAccountNumber((String) data.get("accountNumber")));
        account.put("bankName", data.get("bankName"));
        account.put("isDefault", userAccounts.isEmpty());
        account.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        userAccounts.add(account);
        return account;
    }

    public void deleteWithdrawalAccount(String userId, String id) {
        List<Map<String, Object>> userAccounts = accounts.getOrDefault(userId, new ArrayList<>());
        userAccounts.removeIf(a -> id.equals(a.get("id")));
    }

    public void setDefaultAccount(String userId, String id) {
        List<Map<String, Object>> userAccounts = accounts.getOrDefault(userId, new ArrayList<>());
        for (Map<String, Object> account : userAccounts) {
            account.put("isDefault", id.equals(account.get("id")));
        }
    }

    public Map<String, Object> getIncomeStatistics(String userId, String period) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIncome", 50000); // 500元
        stats.put("periodIncome", 10000); // 100元
        stats.put("orderCount", 5);
        stats.put("averageOrderAmount", 10000);
        
        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE));
            day.put("amount", (int) (Math.random() * 5000));
            day.put("orderCount", (int) (Math.random() * 3));
            dailyData.add(day);
        }
        stats.put("dailyData", dailyData);
        
        return stats;
    }

    private void addTransaction(String userId, String type, int amount, int fee, String description, String relatedId) {
        Map<String, Object> wallet = getWallet(userId);
        List<Map<String, Object>> userTx = transactions.computeIfAbsent(userId, k -> new ArrayList<>());
        
        Map<String, Object> tx = new HashMap<>();
        tx.put("id", UUID.randomUUID().toString());
        tx.put("walletId", wallet.get("id"));
        tx.put("type", type);
        tx.put("amount", amount);
        tx.put("fee", fee);
        tx.put("balanceBefore", (int) wallet.get("balance") - amount);
        tx.put("balanceAfter", wallet.get("balance"));
        tx.put("status", "success");
        tx.put("description", description);
        tx.put("relatedWithdrawalId", relatedId);
        tx.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        userTx.add(0, tx);
    }

    private String maskAccountNumber(String number) {
        if (number == null || number.length() < 8) return number;
        return number.substring(0, 3) + "****" + number.substring(number.length() - 4);
    }

    // 添加收入（用于机构收款）
    public void addIncome(String userId, int amount, String description, String relatedOrderId) {
        Map<String, Object> wallet = getWallet(userId);
        
        // 更新余额
        int currentBalance = (int) wallet.get("balance");
        int currentTotalIncome = (int) wallet.get("totalIncome");
        wallet.put("balance", currentBalance + amount);
        wallet.put("totalIncome", currentTotalIncome + amount);
        wallet.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 添加交易记录
        List<Map<String, Object>> userTx = transactions.computeIfAbsent(userId, k -> new ArrayList<>());
        
        Map<String, Object> tx = new HashMap<>();
        tx.put("id", UUID.randomUUID().toString());
        tx.put("walletId", wallet.get("id"));
        tx.put("type", "income");
        tx.put("amount", amount);
        tx.put("fee", 0);
        tx.put("balanceBefore", currentBalance);
        tx.put("balanceAfter", currentBalance + amount);
        tx.put("status", "success");
        tx.put("description", description);
        tx.put("relatedOrderId", relatedOrderId);
        tx.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        userTx.add(0, tx);
    }

    // 扣除余额（用于订单支付）
    public boolean deductBalance(String userId, int amount, String description, String relatedOrderId) {
        Map<String, Object> wallet = getWallet(userId);
        int currentBalance = (int) wallet.get("balance");
        
        // 检查余额是否足够
        if (currentBalance < amount) {
            return false;
        }
        
        // 扣除余额
        wallet.put("balance", currentBalance - amount);
        wallet.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 添加交易记录
        List<Map<String, Object>> userTx = transactions.computeIfAbsent(userId, k -> new ArrayList<>());
        
        Map<String, Object> tx = new HashMap<>();
        tx.put("id", UUID.randomUUID().toString());
        tx.put("walletId", wallet.get("id"));
        tx.put("type", "payment");
        tx.put("amount", amount);
        tx.put("fee", 0);
        tx.put("balanceBefore", currentBalance);
        tx.put("balanceAfter", currentBalance - amount);
        tx.put("status", "success");
        tx.put("description", description);
        tx.put("relatedOrderId", relatedOrderId);
        tx.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        userTx.add(0, tx);
        return true;
    }
}
