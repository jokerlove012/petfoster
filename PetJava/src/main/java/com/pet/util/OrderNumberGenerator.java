package com.pet.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 订单号生成工具
 * 生成唯一的订单号，格式: PF + 年月日 + 6位随机数
 * 例如: PF20241222123456
 */
public class OrderNumberGenerator {

    private static final Set<String> generatedOrderNumbers = new HashSet<>();
    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("^[A-Z]{2,4}\\d{14,16}$");

    /**
     * 生成随机数字字符串
     */
    private static String generateRandomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成唯一订单号
     */
    public static synchronized String generateOrderNumber() {
        return generateOrderNumber("PF", LocalDate.now());
    }

    /**
     * 生成唯一订单号（带前缀和日期）
     */
    public static synchronized String generateOrderNumber(String prefix, LocalDate date) {
        String dateStr = date.format(DATE_FORMATTER);
        String orderNumber;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            String randomPart = generateRandomDigits(6);
            orderNumber = prefix + dateStr + randomPart;
            attempts++;

            if (attempts >= maxAttempts) {
                String extendedRandom = generateRandomDigits(8);
                orderNumber = prefix + dateStr + extendedRandom;
                break;
            }
        } while (generatedOrderNumbers.contains(orderNumber));

        generatedOrderNumbers.add(orderNumber);
        return orderNumber;
    }

    /**
     * 验证订单号格式
     */
    public static boolean isValidOrderNumber(String orderNumber) {
        return orderNumber != null && ORDER_NUMBER_PATTERN.matcher(orderNumber).matches();
    }

    /**
     * 清除已生成订单号记录（仅用于测试）
     */
    public static synchronized void clearGeneratedOrderNumbers() {
        generatedOrderNumbers.clear();
    }

    /**
     * 获取已生成订单号数量（仅用于测试）
     */
    public static int getGeneratedOrderNumbersCount() {
        return generatedOrderNumbers.size();
    }
}
