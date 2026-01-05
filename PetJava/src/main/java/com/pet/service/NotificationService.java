package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pet.entity.Notification;
import com.pet.entity.User;
import com.pet.mapper.NotificationMapper;
import com.pet.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    /**
     * 获取用户的通知列表
     */
    public List<Notification> getByUserId(String userId, String type, Boolean isRead) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);
        return notificationMapper.selectList(wrapper);
    }

    /**
     * 获取未读数量
     */
    public int getUnreadCount(String userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, false);
        return Math.toIntExact(notificationMapper.selectCount(wrapper));
    }

    /**
     * 标记为已读
     */
    public void markAsRead(String id, String userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
               .eq(Notification::getUserId, userId)
               .set(Notification::getIsRead, true);
        notificationMapper.update(null, wrapper);
    }

    /**
     * 标记全部已读
     */
    public void markAllAsRead(String userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, false)
               .set(Notification::getIsRead, true);
        notificationMapper.update(null, wrapper);
    }

    /**
     * 删除通知
     */
    public void delete(String id, String userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getId, id)
               .eq(Notification::getUserId, userId);
        notificationMapper.delete(wrapper);
    }

    /**
     * 清空已读通知
     */
    public void clearRead(String userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, true);
        notificationMapper.delete(wrapper);
    }

    /**
     * 发送通知
     */
    public Notification send(String userId, String type, String title, String content, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setLink(link);
        notification.setIsRead(false);
        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 发送系统通知给所有用户
     */
    public void sendToAll(List<String> userIds, String type, String title, String content, String link) {
        for (String userId : userIds) {
            send(userId, type, title, content, link);
        }
    }

    /**
     * 发送通知给所有管理员
     */
    public void sendToAdmins(String type, String title, String content, String link) {
        List<User> admins = userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getRole, "admin")
        );
        for (User admin : admins) {
            send(admin.getId(), type, title, content, link);
        }
    }

    /**
     * 转换为VO
     */
    public Map<String, Object> toVO(Notification n) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", n.getId());
        vo.put("userId", n.getUserId());
        vo.put("type", n.getType());
        vo.put("title", n.getTitle());
        vo.put("content", n.getContent());
        vo.put("isRead", n.getIsRead());
        vo.put("link", n.getLink());
        vo.put("createdAt", n.getCreatedAt());
        return vo;
    }
}
