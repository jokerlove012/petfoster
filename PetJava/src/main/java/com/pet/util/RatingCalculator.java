package com.pet.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评分计算工具
 */
public class RatingCalculator {

    /**
     * 计算综合平均评分
     */
    public static BigDecimal calculateAverageRating(List<Map<String, Object>> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (Map<String, Object> review : reviews) {
            Object ratingObj = review.get("rating");
            if (ratingObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rating = (Map<String, Object>) ratingObj;
                Object overall = rating.get("overall");
                if (overall instanceof Number) {
                    sum = sum.add(BigDecimal.valueOf(((Number) overall).doubleValue()));
                }
            }
        }

        return sum.divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
    }

    /**
     * 计算各维度平均评分
     */
    public static Map<String, BigDecimal> calculateDimensionAverages(List<Map<String, Object>> reviews) {
        Map<String, BigDecimal> result = new HashMap<>();
        String[] dimensions = {"overall", "environment", "service", "hygiene", "communication"};

        for (String dim : dimensions) {
            result.put(dim, BigDecimal.ZERO);
        }

        if (reviews == null || reviews.isEmpty()) {
            return result;
        }

        Map<String, BigDecimal> sums = new HashMap<>();
        for (String dim : dimensions) {
            sums.put(dim, BigDecimal.ZERO);
        }

        for (Map<String, Object> review : reviews) {
            Object ratingObj = review.get("rating");
            if (ratingObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rating = (Map<String, Object>) ratingObj;
                for (String dim : dimensions) {
                    Object value = rating.get(dim);
                    if (value instanceof Number) {
                        sums.put(dim, sums.get(dim).add(BigDecimal.valueOf(((Number) value).doubleValue())));
                    }
                }
            }
        }

        int count = reviews.size();
        for (String dim : dimensions) {
            result.put(dim, sums.get(dim).divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP));
        }

        return result;
    }

    /**
     * 计算评分分布
     */
    public static Map<Integer, Integer> calculateRatingDistribution(List<Map<String, Object>> reviews) {
        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }

        if (reviews == null || reviews.isEmpty()) {
            return distribution;
        }

        for (Map<String, Object> review : reviews) {
            Object ratingObj = review.get("rating");
            if (ratingObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rating = (Map<String, Object>) ratingObj;
                Object overall = rating.get("overall");
                if (overall instanceof Number) {
                    int rounded = (int) Math.round(((Number) overall).doubleValue());
                    if (rounded >= 1 && rounded <= 5) {
                        distribution.put(rounded, distribution.get(rounded) + 1);
                    }
                }
            }
        }

        return distribution;
    }

    /**
     * 获取评分等级描述
     */
    public static Map<String, String> getRatingLevel(double rating) {
        Map<String, String> result = new HashMap<>();
        if (rating >= 4.5) {
            result.put("level", "极好");
            result.put("color", "#52c41a");
        } else if (rating >= 4.0) {
            result.put("level", "很好");
            result.put("color", "#73d13d");
        } else if (rating >= 3.5) {
            result.put("level", "较好");
            result.put("color", "#faad14");
        } else if (rating >= 3.0) {
            result.put("level", "一般");
            result.put("color", "#fa8c16");
        } else if (rating >= 2.0) {
            result.put("level", "较差");
            result.put("color", "#ff7a45");
        } else {
            result.put("level", "很差");
            result.put("color", "#ff4d4f");
        }
        return result;
    }

    /**
     * 验证评价内容长度
     */
    public static Map<String, Object> validateReviewContent(String content, int minLength, int maxLength) {
        Map<String, Object> result = new HashMap<>();
        String trimmed = content != null ? content.trim() : "";

        if (trimmed.length() < minLength) {
            result.put("valid", false);
            result.put("error", "评价内容至少需要 " + minLength + " 个字符");
            return result;
        }

        if (trimmed.length() > maxLength) {
            result.put("valid", false);
            result.put("error", "评价内容不能超过 " + maxLength + " 个字符");
            return result;
        }

        result.put("valid", true);
        return result;
    }

    /**
     * 计算推荐指数（贝叶斯平均）
     */
    public static BigDecimal calculateRecommendationScore(double averageRating, int reviewCount) {
        if (reviewCount == 0) {
            return BigDecimal.ZERO;
        }

        double C = 10; // 置信度参数
        double m = 3.5; // 全局平均评分

        double score = (reviewCount * averageRating + C * m) / (reviewCount + C);
        return BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP);
    }
}
