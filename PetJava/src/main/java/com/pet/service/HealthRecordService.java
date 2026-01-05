package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.entity.Booking;
import com.pet.entity.HealthRecord;
import com.pet.entity.Pet;
import com.pet.mapper.BookingMapper;
import com.pet.mapper.HealthRecordMapper;
import com.pet.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HealthRecordService {
    private final HealthRecordMapper healthRecordMapper;
    private final BookingMapper bookingMapper;
    private final PetMapper petMapper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public List<Map<String, Object>> getByBookingId(String bookingId) {
        List<HealthRecord> records = healthRecordMapper.selectList(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getBookingId, bookingId)
                        .orderByDesc(HealthRecord::getDate));
        List<Map<String, Object>> result = new ArrayList<>();
        for (HealthRecord record : records) {
            result.add(toRecordVO(record));
        }
        return result;
    }

    @SneakyThrows
    public Map<String, Object> create(String bookingId, String createdBy, Map<String, Object> data) {
        HealthRecord record = new HealthRecord();
        record.setBookingId(bookingId);
        record.setCreatedBy(createdBy);
        record.setDate(data.get("date") != null ? LocalDate.parse((String) data.get("date")) : LocalDate.now());
        record.setFeedingStatus((String) data.get("feedingStatus"));
        record.setActivityLevel((String) data.get("activityLevel"));
        record.setHealthObservations((String) data.get("healthObservations"));
        record.setMood((String) data.get("mood"));
        
        if (data.get("weight") != null) {
            record.setWeight(new java.math.BigDecimal(data.get("weight").toString()));
        }
        if (data.get("temperature") != null) {
            record.setTemperature(new java.math.BigDecimal(data.get("temperature").toString()));
        }
        if (data.get("medications") != null) {
            record.setMedications(objectMapper.writeValueAsString(data.get("medications")));
        }
        if (data.get("photos") != null) {
            record.setPhotos(objectMapper.writeValueAsString(data.get("photos")));
        }
        if (data.get("videos") != null) {
            record.setVideos(objectMapper.writeValueAsString(data.get("videos")));
        }
        
        record.setIsAbnormal(data.get("isAbnormal") != null ? (Boolean) data.get("isAbnormal") : false);
        record.setAbnormalDetails((String) data.get("abnormalDetails"));

        healthRecordMapper.insert(record);
        
        // 发送健康记录通知给用户
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking != null) {
            Pet pet = petMapper.selectById(booking.getPetId());
            String petName = pet != null ? pet.getName() : "您的宠物";
            
            String notifyType = record.getIsAbnormal() ? "health" : "health";
            String title = record.getIsAbnormal() ? "健康异常提醒" : "健康记录更新";
            String content = record.getIsAbnormal() 
                ? petName + " 今日健康检查发现异常，请及时查看详情"
                : petName + " 今日健康记录已更新，状态良好";
            
            notificationService.send(booking.getUserId(), notifyType, title, content,
                "/order/" + booking.getId());
        }
        
        return toRecordVO(record);
    }

    @SneakyThrows
    private Map<String, Object> toRecordVO(HealthRecord record) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", record.getId());
        vo.put("bookingId", record.getBookingId());
        vo.put("date", record.getDate());
        vo.put("feedingStatus", record.getFeedingStatus());
        vo.put("activityLevel", record.getActivityLevel());
        vo.put("healthObservations", record.getHealthObservations());
        vo.put("mood", record.getMood());
        vo.put("weight", record.getWeight());
        vo.put("temperature", record.getTemperature());
        
        if (StringUtils.hasText(record.getMedications())) {
            vo.put("medications", objectMapper.readValue(record.getMedications(), new TypeReference<List<String>>() {}));
        } else {
            vo.put("medications", new ArrayList<>());
        }
        if (StringUtils.hasText(record.getPhotos())) {
            vo.put("photos", objectMapper.readValue(record.getPhotos(), new TypeReference<List<String>>() {}));
        } else {
            vo.put("photos", new ArrayList<>());
        }
        if (StringUtils.hasText(record.getVideos())) {
            vo.put("videos", objectMapper.readValue(record.getVideos(), new TypeReference<List<String>>() {}));
        } else {
            vo.put("videos", new ArrayList<>());
        }
        
        vo.put("isAbnormal", record.getIsAbnormal());
        vo.put("abnormalDetails", record.getAbnormalDetails());
        vo.put("createdBy", record.getCreatedBy());
        vo.put("createdAt", record.getCreatedAt());
        return vo;
    }
}
