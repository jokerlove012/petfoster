package com.pet.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 评分计算工具测试
 */
public class RatingCalculatorTest {

    @Test
    public void testCalculateAverageRating_EmptyList() {
        List<Map<String, Object>> reviews = new ArrayList<>();
        
        BigDecimal average = RatingCalculator.calculateAverageRating(reviews);
        
        assertEquals(BigDecimal.ZERO, average, "空列表的平均评分应该是 0");
    }

    @Test
    public void testCalculateAverageRating_NullList() {
        BigDecimal average = RatingCalculator.calculateAverageRating(null);
        
        assertEquals(BigDecimal.ZERO, average, "null 列表的平均评分应该是 0");
    }

    @Test
    public void testCalculateAverageRating_SingleReview() {
        List<Map<String, Object>> reviews = new ArrayList<>();
        Map<String, Object> review = new HashMap<>();
        Map<String, Object> rating = new HashMap<>();
        rating.put("overall", 5);
        review.put("rating", rating);
        reviews.add(review);
        
        BigDecimal average = RatingCalculator.calculateAverageRating(reviews);
        
        assertEquals(new BigDecimal("5.0"), average, "单个评分的平均分应该是 5.0");
    }

    @Test
    public void testCalculateAverageRating_MultipleReviews() {
        List<Map<String, Object>> reviews = new ArrayList<>();
        
        Map<String, Object> review1 = new HashMap<>();
        Map<String, Object> rating1 = new HashMap<>();
        rating1.put("overall", 4);
        review1.put("rating", rating1);
        reviews.add(review1);
        
        Map<String, Object> review2 = new HashMap<>();
        Map<String, Object> rating2 = new HashMap<>();
        rating2.put("overall", 5);
        review2.put("rating", rating2);
        reviews.add(review2);
        
        Map<String, Object> review3 = new HashMap<>();
        Map<String, Object> rating3 = new HashMap<>();
        rating3.put("overall", 3);
        review3.put("rating", rating3);
        reviews.add(review3);
        
        BigDecimal average = RatingCalculator.calculateAverageRating(reviews);
        
        assertEquals(new BigDecimal("4.0"), average, "平均分应该是 4.0");
    }

    @Test
    public void testCalculateDimensionAverages_EmptyList() {
        Map<String, BigDecimal> averages = RatingCalculator.calculateDimensionAverages(new ArrayList<>());
        
        assertNotNull(averages, "结果不应该为空");
        assertEquals(5, averages.size(), "应该有 5 个维度");
        assertEquals(BigDecimal.ZERO, averages.get("overall"), "overall 维度应该是 0");
        assertEquals(BigDecimal.ZERO, averages.get("environment"), "environment 维度应该是 0");
        assertEquals(BigDecimal.ZERO, averages.get("service"), "service 维度应该是 0");
        assertEquals(BigDecimal.ZERO, averages.get("hygiene"), "hygiene 维度应该是 0");
        assertEquals(BigDecimal.ZERO, averages.get("communication"), "communication 维度应该是 0");
    }

    @Test
    public void testCalculateDimensionAverages_WithReviews() {
        List<Map<String, Object>> reviews = new ArrayList<>();
        
        Map<String, Object> review1 = new HashMap<>();
        Map<String, Object> rating1 = new HashMap<>();
        rating1.put("overall", 5);
        rating1.put("environment", 4);
        rating1.put("service", 5);
        rating1.put("hygiene", 4);
        rating1.put("communication", 5);
        review1.put("rating", rating1);
        reviews.add(review1);
        
        Map<String, Object> review2 = new HashMap<>();
        Map<String, Object> rating2 = new HashMap<>();
        rating2.put("overall", 4);
        rating2.put("environment", 5);
        rating2.put("service", 4);
        rating2.put("hygiene", 5);
        rating2.put("communication", 4);
        review2.put("rating", rating2);
        reviews.add(review2);
        
        Map<String, BigDecimal> averages = RatingCalculator.calculateDimensionAverages(reviews);
        
        assertEquals(new BigDecimal("4.5"), averages.get("overall"), "overall 平均分应该是 4.5");
        assertEquals(new BigDecimal("4.5"), averages.get("environment"), "environment 平均分应该是 4.5");
        assertEquals(new BigDecimal("4.5"), averages.get("service"), "service 平均分应该是 4.5");
    }

    @Test
    public void testCalculateRatingDistribution_EmptyList() {
        Map<Integer, Integer> distribution = RatingCalculator.calculateRatingDistribution(new ArrayList<>());
        
        assertNotNull(distribution, "结果不应该为空");
        assertEquals(5, distribution.size(), "应该有 5 个评分等级");
        for (int i = 1; i <= 5; i++) {
            assertEquals(0, distribution.get(i), "每个等级的数量应该是 0");
        }
    }

    @Test
    public void testCalculateRatingDistribution_WithReviews() {
        List<Map<String, Object>> reviews = new ArrayList<>();
        
        Map<String, Object> review1 = new HashMap<>();
        Map<String, Object> rating1 = new HashMap<>();
        rating1.put("overall", 5);
        review1.put("rating", rating1);
        reviews.add(review1);
        
        Map<String, Object> review2 = new HashMap<>();
        Map<String, Object> rating2 = new HashMap<>();
        rating2.put("overall", 5);
        review2.put("rating", rating2);
        reviews.add(review2);
        
        Map<String, Object> review3 = new HashMap<>();
        Map<String, Object> rating3 = new HashMap<>();
        rating3.put("overall", 3);
        review3.put("rating", rating3);
        reviews.add(review3);
        
        Map<Integer, Integer> distribution = RatingCalculator.calculateRatingDistribution(reviews);
        
        assertEquals(2, distribution.get(5), "5 星应该有 2 个");
        assertEquals(1, distribution.get(3), "3 星应该有 1 个");
        assertEquals(0, distribution.get(4), "4 星应该有 0 个");
    }
}
