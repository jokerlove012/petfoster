package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.entity.*;
import com.pet.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletMapper walletMapper;
    private final WalletTransactionMapper transactionMapper;
    private final RechargeOrderMapper rechargeOrderMapper;
    private final WithdrawalMapper withdrawalMapper;
    private final WithdrawalAccountMapper accountMapper;
    private final WalletAuditLogMapper auditLogMapper;

    private static final int MIN_WITHDRAWAL = 1000;
    private static final int MAX_DAILY_WITHDRAWAL = 5000000;
    private static final int MAX_DAILY_WITHDRAWAL_COUNT = 5;

    public Wallet getOrCreateWallet(String userId, String userType) {
        Wallet wallet = walletMapper.selectOne(new LambdaQueryWrapper<Wallet>()
                .eq(Wallet::getUserId, userId));
        
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setUserType(userType);
            wallet.setBalance(0);
            wallet.setFrozenBalance(0);
            wallet.setTotalIncome(0);
            wallet.setTotalWithdraw(0);
            wallet.setStatus("active");
            wallet.setDailyWithdrawAmount(0);
            wallet.setDailyWithdrawCount(0);
            walletMapper.insert(wallet);
        } else {
            checkAndResetDailyLimit(wallet);
        }
        
        return wallet;
    }

    private void checkAndResetDailyLimit(Wallet wallet) {
        LocalDate today = LocalDate.now();
        if (wallet.getLastWithdrawDate() == null || !wallet.getLastWithdrawDate().equals(today)) {
            wallet.setDailyWithdrawAmount(0);
            wallet.setDailyWithdrawCount(0);
            wallet.setLastWithdrawDate(today);
            walletMapper.updateById(wallet);
        }
    }

    public RechargeOrder createRechargeOrder(String userId, int amount, String paymentMethod) {
        if (amount <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }

        Wallet wallet = getOrCreateWallet(userId, "pet_owner");
        
        RechargeOrder order = new RechargeOrder();
        order.setWalletId(wallet.getId());
        order.setUserId(userId);
        order.setAmount(amount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus("pending");
        order.setCreatedAt(LocalDateTime.now());
        order.setExpiredAt(LocalDateTime.now().plusHours(1));
        rechargeOrderMapper.insert(order);
        
        return order;
    }

    @Transactional
    public boolean confirmRecharge(String orderId) {
        RechargeOrder order = rechargeOrderMapper.selectById(orderId);
        if (order == null || !"pending".equals(order.getStatus())) {
            return false;
        }

        Wallet wallet = walletMapper.selectById(order.getWalletId());
        if (wallet == null) {
            return false;
        }

        int oldBalance = wallet.getBalance();
        wallet.setBalance(oldBalance + order.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);

        order.setStatus("paid");
        order.setPaidAt(LocalDateTime.now());
        rechargeOrderMapper.updateById(order);

        addTransaction(wallet.getId(), wallet.getUserId(), "recharge", order.getAmount(), 0, 
                oldBalance, wallet.getBalance(), "充值成功", null, null);

        return true;
    }

    @Transactional
    public Withdrawal createWithdrawal(String userId, int amount, String accountId, String withdrawPassword) {
        if (amount < MIN_WITHDRAWAL) {
            throw new RuntimeException("最低提现金额为10元");
        }

        Wallet wallet = getOrCreateWallet(userId, "pet_owner");
        
        if (!"active".equals(wallet.getStatus())) {
            throw new RuntimeException("钱包已被冻结，无法提现");
        }

        if (wallet.getWithdrawPassword() != null) {
            if (withdrawPassword == null || !withdrawPassword.equals(wallet.getWithdrawPassword())) {
                throw new RuntimeException("提现密码错误");
            }
        }

        checkAndResetDailyLimit(wallet);
        
        if (wallet.getDailyWithdrawAmount() + amount > MAX_DAILY_WITHDRAWAL) {
            throw new RuntimeException("超出今日最高提现限额50000元");
        }
        
        if (wallet.getDailyWithdrawCount() >= MAX_DAILY_WITHDRAWAL_COUNT) {
            throw new RuntimeException("今日提现次数已达上限5次");
        }

        int fee = calculateFee(amount);
        int totalAmount = amount + fee;
        
        if (wallet.getBalance() < totalAmount) {
            throw new RuntimeException("余额不足");
        }

        int actualAmount = amount;
        
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setWalletId(wallet.getId());
        withdrawal.setUserId(userId);
        withdrawal.setAmount(amount);
        withdrawal.setFee(fee);
        withdrawal.setActualAmount(actualAmount);
        withdrawal.setAccountId(accountId);
        withdrawal.setStatus("pending");
        withdrawal.setCreatedAt(LocalDateTime.now());
        withdrawalMapper.insert(withdrawal);

        int oldBalance = wallet.getBalance();
        wallet.setBalance(oldBalance - totalAmount);
        wallet.setTotalWithdraw(wallet.getTotalWithdraw() + amount);
        wallet.setDailyWithdrawAmount(wallet.getDailyWithdrawAmount() + amount);
        wallet.setDailyWithdrawCount(wallet.getDailyWithdrawCount() + 1);
        wallet.setLastWithdrawDate(LocalDate.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);

        addTransaction(wallet.getId(), wallet.getUserId(), "withdrawal", amount, fee, 
                oldBalance, wallet.getBalance(), "申请提现", null, withdrawal.getId());

        return withdrawal;
    }

    private int calculateFee(int amount) {
        int fee = (int) Math.ceil(amount * 0.01);
        return Math.max(fee, 100);
    }

    @Transactional
    public boolean deductBalance(String userId, int amount, String description, String relatedOrderId) {
        Wallet wallet = getOrCreateWallet(userId, "pet_owner");
        
        if (wallet.getBalance() < amount) {
            log.warn("余额不足: userId={}, balance={}, required={}", userId, wallet.getBalance(), amount);
            return false;
        }

        int oldBalance = wallet.getBalance();
        wallet.setBalance(oldBalance - amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);

        addTransaction(wallet.getId(), userId, "payment", amount, 0, 
                oldBalance, wallet.getBalance(), description, relatedOrderId, null);

        log.info("扣款成功: userId={}, amount={}, balanceBefore={}, balanceAfter={}", 
                userId, amount, oldBalance, wallet.getBalance());
        return true;
    }

    @Transactional
    public boolean addIncome(String userId, int amount, String description, String relatedOrderId) {
        Wallet wallet = getOrCreateWallet(userId, "institution_staff");
        
        int oldBalance = wallet.getBalance();
        wallet.setBalance(oldBalance + amount);
        wallet.setTotalIncome(wallet.getTotalIncome() + amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);

        addTransaction(wallet.getId(), userId, "income", amount, 0, 
                oldBalance, wallet.getBalance(), description, relatedOrderId, null);

        return true;
    }

    private void addTransaction(String walletId, String userId, String type, int amount, int fee, 
                                 int balanceBefore, int balanceAfter, String description, 
                                 String relatedOrderId, String relatedWithdrawalId) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(walletId);
        transaction.setUserId(userId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setStatus("success");
        transaction.setDescription(description);
        transaction.setRelatedOrderId(relatedOrderId);
        transaction.setRelatedWithdrawalId(relatedWithdrawalId);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(transaction);
    }

    public Map<String, Object> getWalletInfo(String userId) {
        Wallet wallet = getOrCreateWallet(userId, "pet_owner");
        
        Map<String, Object> info = new HashMap<>();
        info.put("balance", wallet.getBalance());
        info.put("frozenBalance", wallet.getFrozenBalance());
        info.put("totalIncome", wallet.getTotalIncome());
        info.put("totalWithdraw", wallet.getTotalWithdraw());
        info.put("status", wallet.getStatus());
        info.put("hasWithdrawPassword", wallet.getWithdrawPassword() != null);
        return info;
    }

    public List<Map<String, Object>> getTransactions(String userId, String type, int page, int pageSize) {
        Wallet wallet = getOrCreateWallet(userId, "pet_owner");
        
        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletTransaction::getWalletId, wallet.getId());
        if (type != null && !type.isEmpty()) {
            wrapper.eq(WalletTransaction::getType, type);
        }
        wrapper.orderByDesc(WalletTransaction::getCreatedAt);
        
        List<WalletTransaction> transactions = transactionMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (WalletTransaction t : transactions) {
            Map<String, Object> vo = new HashMap<>();
            vo.put("id", t.getId());
            vo.put("type", t.getType());
            vo.put("amount", t.getAmount());
            vo.put("fee", t.getFee());
            vo.put("status", t.getStatus());
            vo.put("description", t.getDescription());
            vo.put("createdAt", t.getCreatedAt());
            result.add(vo);
        }
        
        return result;
    }

    public List<Map<String, Object>> getWithdrawalAccounts(String userId) {
        List<WithdrawalAccount> accounts = accountMapper.selectList(new LambdaQueryWrapper<WithdrawalAccount>()
                .eq(WithdrawalAccount::getUserId, userId)
                .eq(WithdrawalAccount::getDeleted, 0)
                .orderByDesc(WithdrawalAccount::getIsDefault)
                .orderByDesc(WithdrawalAccount::getCreatedAt));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (WithdrawalAccount a : accounts) {
            Map<String, Object> vo = new HashMap<>();
            vo.put("id", a.getId());
            vo.put("type", a.getType());
            vo.put("accountName", a.getAccountName());
            String accountNumber = a.getAccountNumber();
            if (accountNumber != null && accountNumber.length() > 4) {
                vo.put("accountNumber", "****" + accountNumber.substring(accountNumber.length() - 4));
            } else {
                vo.put("accountNumber", accountNumber);
            }
            vo.put("bankName", a.getBankName());
            vo.put("bankBranch", a.getBankBranch());
            vo.put("isDefault", a.getIsDefault() == 1);
            result.add(vo);
        }
        return result;
    }

    public WithdrawalAccount addWithdrawalAccount(String userId, Map<String, Object> data) {
        WithdrawalAccount account = new WithdrawalAccount();
        account.setUserId(userId);
        account.setType((String) data.get("type"));
        account.setAccountName((String) data.get("accountName"));
        account.setAccountNumber((String) data.get("accountNumber"));
        account.setBankName((String) data.get("bankName"));
        account.setBankBranch((String) data.get("bankBranch"));
        account.setIsDefault(data.get("isDefault") != null ? (Boolean) data.get("isDefault") ? 1 : 0 : 0);
        account.setCreatedAt(LocalDateTime.now());
        
        if (account.getIsDefault() == 1) {
            List<WithdrawalAccount> existing = accountMapper.selectList(new LambdaQueryWrapper<WithdrawalAccount>()
                    .eq(WithdrawalAccount::getUserId, userId));
            for (WithdrawalAccount a : existing) {
                a.setIsDefault(0);
                accountMapper.updateById(a);
            }
        }
        
        accountMapper.insert(account);
        return account;
    }

    @Transactional
    public boolean deleteWithdrawalAccount(String userId, String accountId) {
        WithdrawalAccount account = accountMapper.selectOne(new LambdaQueryWrapper<WithdrawalAccount>()
                .eq(WithdrawalAccount::getId, accountId)
                .eq(WithdrawalAccount::getUserId, userId)
                .eq(WithdrawalAccount::getDeleted, 0));
        
        if (account == null) {
            return false;
        }
        
        account.setDeleted(1);
        account.setUpdatedAt(LocalDateTime.now());
        accountMapper.updateById(account);
        return true;
    }

    @Transactional
    public boolean setDefaultAccount(String userId, String accountId) {
        WithdrawalAccount account = accountMapper.selectOne(new LambdaQueryWrapper<WithdrawalAccount>()
                .eq(WithdrawalAccount::getId, accountId)
                .eq(WithdrawalAccount::getUserId, userId)
                .eq(WithdrawalAccount::getDeleted, 0));
        
        if (account == null) {
            return false;
        }
        
        List<WithdrawalAccount> existing = accountMapper.selectList(new LambdaQueryWrapper<WithdrawalAccount>()
                .eq(WithdrawalAccount::getUserId, userId)
                .eq(WithdrawalAccount::getDeleted, 0));
        for (WithdrawalAccount a : existing) {
            a.setIsDefault(a.getId().equals(accountId) ? 1 : 0);
            accountMapper.updateById(a);
        }
        
        return true;
    }

    public void setWithdrawPassword(String userId, String password) {
        Wallet wallet = getOrCreateWallet(userId, "pet_owner");
        wallet.setWithdrawPassword(password);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);
    }

    public boolean verifyWithdrawPassword(String userId, String password) {
        Wallet wallet = getOrCreateWallet(userId, "pet_owner");
        return wallet.getWithdrawPassword() != null && wallet.getWithdrawPassword().equals(password);
    }

    public Map<String, Object> getIncomeStatistics(String userId) {
        Wallet wallet = getOrCreateWallet(userId, "institution_staff");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        int dailyIncome = calculateIncomeByDateRange(wallet.getId(), startOfDay, now);
        int weeklyIncome = calculateIncomeByDateRange(wallet.getId(), startOfWeek, now);
        int monthlyIncome = calculateIncomeByDateRange(wallet.getId(), startOfMonth, now);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIncome", wallet.getTotalIncome());
        stats.put("dailyIncome", dailyIncome);
        stats.put("weeklyIncome", weeklyIncome);
        stats.put("monthlyIncome", monthlyIncome);
        return stats;
    }

    private int calculateIncomeByDateRange(String walletId, LocalDateTime start, LocalDateTime end) {
        List<WalletTransaction> transactions = transactionMapper.selectList(new LambdaQueryWrapper<WalletTransaction>()
                .eq(WalletTransaction::getWalletId, walletId)
                .eq(WalletTransaction::getType, "income")
                .between(WalletTransaction::getCreatedAt, start, end));
        
        return transactions.stream().mapToInt(WalletTransaction::getAmount).sum();
    }

    private void addAuditLog(String walletId, String userId, String operation, String details) {
        WalletAuditLog auditLog = new WalletAuditLog();
        auditLog.setWalletId(walletId);
        auditLog.setUserId(userId);
        auditLog.setOperation(operation);
        auditLog.setDetails(details);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(auditLog);
    }
}
