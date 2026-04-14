package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.common.PageResult;
import com.pet.dto.CreateReviewRequest;
import com.pet.entity.Booking;
import com.pet.entity.Review;
import com.pet.entity.User;
import com.pet.mapper.BookingMapper;
import com.pet.mapper.InstitutionMapper;
import com.pet.mapper.ReviewMapper;
import com.pet.mapper.UserMapper;
import com.pet.entity.Institution;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewMapper reviewMapper;
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Map<String, Object> create(String userId, CreateReviewRequest request) {
        Booking booking = bookingMapper.selectById(request.getBookingId());
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"completed".equals(booking.getStatus())) {
            throw new RuntimeException("订单未完成，无法评价");
        }

        Review existing = reviewMapper.selectOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getBookingId, request.getBookingId()));
        if (existing != null) {
            throw new RuntimeException("该订单已评价");
        }

        Review review = new Review();
        review.setBookingId(request.getBookingId());
        review.setUserId(userId);
        review.setInstitutionId(booking.getInstitutionId());
        review.setRating(objectMapper.writeValueAsString(request.getRating()));
        review.setContent(request.getContent());
        review.setImages(request.getImages() != null ? objectMapper.writeValueAsString(request.getImages()) : "[]");
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);

        System.out.println("创建评价 - bookingId: " + request.getBookingId() + ", institutionId: " + booking.getInstitutionId());
        reviewMapper.insert(review);
        System.out.println("评价创建成功 - reviewId: " + review.getId());

        // 更新机构的评分和评价数量
        updateInstitutionRating(booking.getInstitutionId());

        return toReviewVO(review);
    }

    public Map<String, Object> getDetail(String id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        return toReviewVO(review);
    }

    public PageResult<Map<String, Object>> getByInstitution(String institutionId, String sortBy, int page, int pageSize) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getInstitutionId, institutionId);

        if ("highest".equals(sortBy)) {
            wrapper.orderByDesc(Review::getRating);
        } else if ("lowest".equals(sortBy)) {
            wrapper.orderByAsc(Review::getRating);
        } else {
            wrapper.orderByDesc(Review::getCreatedAt);
        }

        Page<Review> pageResult = reviewMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Review review : pageResult.getRecords()) {
            list.add(toReviewVO(review));
        }
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    public PageResult<Map<String, Object>> getUserReviews(String userId, int page, int pageSize) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId);
        wrapper.orderByDesc(Review::getCreatedAt);

        Page<Review> pageResult = reviewMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Review review : pageResult.getRecords()) {
            list.add(toReviewVO(review));
        }
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    @SneakyThrows
    public Map<String, Object> reply(String id, String content) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }

        Map<String, Object> replyObj = new HashMap<>();
        replyObj.put("content", content);
        replyObj.put("repliedAt", LocalDateTime.now().toString());
        review.setReply(objectMapper.writeValueAsString(replyObj));

        reviewMapper.updateById(review);
        return toReviewVO(review);
    }

    public void delete(String id) {
        Review review = reviewMapper.selectById(id);
        if (review != null) {
            String institutionId = review.getInstitutionId();
            reviewMapper.deleteById(id);
            // 删除评价后也需要更新机构的评分和评价数量
            updateInstitutionRating(institutionId);
        }
    }

    @SneakyThrows
    private void updateInstitutionRating(String institutionId) {
        // 查询该机构的所有评价
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getInstitutionId, institutionId));

        // 计算评价数量
        int reviewCount = reviews.size();

        // 计算平均评分
        java.math.BigDecimal averageRating = java.math.BigDecimal.ZERO;
        if (reviewCount > 0) {
            java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
            for (Review review : reviews) {
                if (StringUtils.hasText(review.getRating())) {
                    Map<String, Object> ratingMap = objectMapper.readValue(
                            review.getRating(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    Object overall = ratingMap.get("overall");
                    if (overall instanceof Number) {
                        sum = sum.add(java.math.BigDecimal.valueOf(((Number) overall).doubleValue()));
                    }
                }
            }
            averageRating = sum.divide(
                    java.math.BigDecimal.valueOf(reviewCount),
                    1,
                    java.math.RoundingMode.HALF_UP);
        }

        // 更新机构
        Institution institution = institutionMapper.selectById(institutionId);
        if (institution != null) {
            institution.setRating(averageRating);
            institution.setReviewCount(reviewCount);
            institutionMapper.updateById(institution);
            System.out.println("机构评分已更新 - institutionId: " + institutionId +
                    ", rating: " + averageRating +
                    ", reviewCount: " + reviewCount);
        }
    }

    // 机构端获取评价列表
    public PageResult<Map<String, Object>> getInstitutionReviews(String staffUserId, int page, int pageSize) {
        User staff = userMapper.selectById(staffUserId);
        System.out.println("获取机构评价 - staffUserId: " + staffUserId);
        if (staff == null) {
            System.out.println("获取机构评价 - 员工不存在");
            return PageResult.of(new ArrayList<>(), page, pageSize, 0);
        }
        System.out.println("获取机构评价 - staff.institutionId: " + staff.getInstitutionId());
        if (staff.getInstitutionId() == null) {
            System.out.println("获取机构评价 - 员工没有关联机构");
            return PageResult.of(new ArrayList<>(), page, pageSize, 0);
        }
        
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getInstitutionId, staff.getInstitutionId());
        wrapper.orderByDesc(Review::getCreatedAt);
        
        Page<Review> pageResult = reviewMapper.selectPage(new Page<>(page, pageSize), wrapper);
        System.out.println("获取机构评价 - 查询到评价数量: " + pageResult.getTotal());
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (Review review : pageResult.getRecords()) {
            System.out.println("评价详情 - id: " + review.getId() + ", institutionId: " + review.getInstitutionId());
            Map<String, Object> vo = toReviewVO(review);
            // 添加用户信息
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                vo.put("userName", review.getIsAnonymous() ? "匿名用户" : user.getName());
                vo.put("userAvatar", "👤");
            }
            // 添加订单信息
            Booking booking = bookingMapper.selectById(review.getBookingId());
            if (booking != null) {
                vo.put("orderId", booking.getOrderNumber());
            }
            // 添加replied标记
            vo.put("replied", StringUtils.hasText(review.getReply()));
            // 添加日期格式
            if (review.getCreatedAt() != null) {
                vo.put("date", review.getCreatedAt().toLocalDate().toString());
            }
            list.add(vo);
        }
        return PageResult.of(list, page, pageSize, pageResult.getTotal());
    }

    // 机构回复评价
    public Map<String, Object> replyReview(String id, String staffUserId, String content) {
        return reply(id, content);
    }

    @SneakyThrows
    private Map<String, Object> toReviewVO(Review review) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", review.getId());
        vo.put("bookingId", review.getBookingId());
        vo.put("userId", review.getUserId());
        vo.put("institutionId", review.getInstitutionId());
        if (StringUtils.hasText(review.getRating())) {
            vo.put("rating", objectMapper.readValue(review.getRating(), new TypeReference<Map<String, Object>>() {}));
        }
        vo.put("content", review.getContent());
        if (StringUtils.hasText(review.getImages())) {
            vo.put("images", objectMapper.readValue(review.getImages(), new TypeReference<List<String>>() {}));
        } else {
            vo.put("images", new ArrayList<>());
        }
        if (StringUtils.hasText(review.getReply())) {
            vo.put("reply", objectMapper.readValue(review.getReply(), new TypeReference<Map<String, Object>>() {}));
        }
        vo.put("isAnonymous", review.getIsAnonymous());
        vo.put("createdAt", review.getCreatedAt());
        vo.put("updatedAt", review.getUpdatedAt());
        return vo;
    }
}
