package com.pet.controller;

import com.pet.common.PageResult;
import com.pet.common.Result;
import com.pet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    // 获取钱包信息
    @GetMapping
    public Result<Map<String, Object>> getWallet(@RequestHeader("X-User-Id") String userId) {
        return Result.success(walletService.getWallet(userId));
    }

    // 获取余额
    @GetMapping("/balance")
    public Result<Map<String, Object>> getBalance(@RequestHeader("X-User-Id") String userId) {
        return Result.success(walletService.getBalance(userId));
    }

    // 创建充值订单
    @PostMapping("/recharge")
    public Result<Map<String, Object>> createRechargeOrder(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> data) {
        return Result.success(walletService.createRechargeOrder(userId, data));
    }

    // 查询充值状态
    @GetMapping("/recharge/{orderId}")
    public Result<Map<String, Object>> getRechargeStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId) {
        return Result.success(walletService.getRechargeStatus(userId, orderId));
    }

    // 创建提现申请
    @PostMapping("/withdraw")
    public Result<Map<String, Object>> createWithdrawal(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> data) {
        return Result.success(walletService.createWithdrawal(userId, data));
    }

    // 获取提现记录
    @GetMapping("/withdrawals")
    public Result<PageResult<Map<String, Object>>> getWithdrawals(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(walletService.getWithdrawals(userId, page, pageSize));
    }

    // 取消提现
    @PostMapping("/withdrawals/{id}/cancel")
    public Result<Void> cancelWithdrawal(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        walletService.cancelWithdrawal(userId, id);
        return Result.success();
    }

    // 获取交易记录
    @GetMapping("/transactions")
    public Result<PageResult<Map<String, Object>>> getTransactions(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(walletService.getTransactions(userId, page, pageSize, type, startDate, endDate));
    }

    // 获取提现账户列表
    @GetMapping("/accounts")
    public Result<java.util.List<Map<String, Object>>> getWithdrawalAccounts(
            @RequestHeader("X-User-Id") String userId) {
        return Result.success(walletService.getWithdrawalAccounts(userId));
    }

    // 添加提现账户
    @PostMapping("/accounts")
    public Result<Map<String, Object>> addWithdrawalAccount(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> data) {
        return Result.success(walletService.addWithdrawalAccount(userId, data));
    }

    // 删除提现账户
    @DeleteMapping("/accounts/{id}")
    public Result<Void> deleteWithdrawalAccount(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        walletService.deleteWithdrawalAccount(userId, id);
        return Result.success();
    }

    // 设置默认账户
    @PutMapping("/accounts/{id}/default")
    public Result<Void> setDefaultAccount(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        walletService.setDefaultAccount(userId, id);
        return Result.success();
    }

    // 获取收入统计
    @GetMapping("/income/statistics")
    public Result<Map<String, Object>> getIncomeStatistics(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "month") String period) {
        return Result.success(walletService.getIncomeStatistics(userId, period));
    }
}
