package com.hajimi.adoption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hajimi.adoption.entity.Notification;
import com.hajimi.adoption.mapper.NotificationMapper;
import com.hajimi.adoption.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public void send(Long userId, Integer type, String subType, String title, String content, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setSubType(subType);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        n.setCreateTime(LocalDateTime.now());
        this.save(n);
    }
}