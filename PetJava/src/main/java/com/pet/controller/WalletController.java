package com.pet.controller;

import com.pet.common.Result;
import com.pet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping
    public Result<Map<String, Object>> getWallet(@RequestAttribute("userId") String userId) {
        return Result.success(walletService.getWalletInfo(userId));
    }

    @PostMapping("/recharge")
    public Result<Map<String, Object>> createRechargeOrder(
            @RequestAttribute("userId") String userId,
            @RequestBody Map<String, Object> data) {
        int amount = ((Number) data.get("amount")).intValue();
        String paymentMethod = (String) data.get("paymentMethod");
        Map<String, Object> result = new HashMap<>();
        result.put("order", walletService.createRechargeOrder(userId, amount, paymentMethod));
        return Result.success(result);
    }

    @PostMapping("/recharge/{orderId}/confirm")
    public Result<Void> confirmRecharge(@PathVariable String orderId) {
        walletService.confirmRecharge(orderId);
        return Result.success();
    }

    @PostMapping("/withdraw")
    public Result<Map<String, Object>> createWithdrawal(
            @RequestAttribute("userId") String userId,
            @RequestBody Map<String, Object> data) {
        int amount = ((Number) data.get("amount")).intValue();
        String accountId = (String) data.get("accountId");
        String withdrawPassword = (String) data.get("withdrawPassword");
        Map<String, Object> result = new HashMap<>();
        result.put("withdrawal", walletService.createWithdrawal(userId, amount, accountId, withdrawPassword));
        return Result.success(result);
    }

    @GetMapping("/transactions")
    public Result<List<Map<String, Object>>> getTransactions(
            @RequestAttribute("userId") String userId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(walletService.getTransactions(userId, type, page, pageSize));
    }

    @GetMapping("/accounts")
    public Result<List<Map<String, Object>>> getWithdrawalAccounts(
            @RequestAttribute("userId") String userId) {
        return Result.success(walletService.getWithdrawalAccounts(userId));
    }

    @PostMapping("/accounts")
    public Result<Map<String, Object>> addWithdrawalAccount(
            @RequestAttribute("userId") String userId,
            @RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("account", walletService.addWithdrawalAccount(userId, data));
        return Result.success(result);
    }

    @DeleteMapping("/accounts/{id}")
    public Result<Void> deleteWithdrawalAccount(
            @RequestAttribute("userId") String userId,
            @PathVariable String id) {
        boolean success = walletService.deleteWithdrawalAccount(userId, id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }

    @PutMapping("/accounts/{id}/default")
    public Result<Void> setDefaultAccount(
            @RequestAttribute("userId") String userId,
            @PathVariable String id) {
        boolean success = walletService.setDefaultAccount(userId, id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("设置失败");
        }
    }

    @PostMapping("/withdraw-password")
    public Result<Void> setWithdrawPassword(
            @RequestAttribute("userId") String userId,
            @RequestBody Map<String, Object> data) {
        String password = (String) data.get("password");
        walletService.setWithdrawPassword(userId, password);
        return Result.success();
    }

    @PostMapping("/withdraw-password/verify")
    public Result<Map<String, Object>> verifyWithdrawPassword(
            @RequestAttribute("userId") String userId,
            @RequestBody Map<String, Object> data) {
        String password = (String) data.get("password");
        boolean valid = walletService.verifyWithdrawPassword(userId, password);
        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);
        return Result.success(result);
    }

    @GetMapping("/income/statistics")
    public Result<Map<String, Object>> getIncomeStatistics(
            @RequestAttribute("userId") String userId) {
        return Result.success(walletService.getIncomeStatistics(userId));
    }
}
