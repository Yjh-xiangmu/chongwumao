package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.Notification;
import com.hajimi.adoption.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 1. 获取用户的所有通知（分类返回 + 未读数）
    @GetMapping("/list/{userId}")
    public Result<Map<String, Object>> getList(@PathVariable Long userId) {
        // 系统通知
        List<Notification> systemList = notificationService.list(
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .eq("type", 0)
                        .orderByDesc("create_time"));

        // 社区通知
        List<Notification> communityList = notificationService.list(
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .eq("type", 1)
                        .orderByDesc("create_time"));

        // 总未读数
        long unreadCount = notificationService.count(
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .eq("is_read", 0));

        Map<String, Object> data = new HashMap<>();
        data.put("systemList", systemList);
        data.put("communityList", communityList);
        data.put("unreadCount", unreadCount);

        return Result.success(data, "获取成功");
    }

    // 2. 标记单条已读
    @PostMapping("/read/{id}")
    public Result<String> markRead(@PathVariable Long id) {
        notificationService.update(
                new UpdateWrapper<Notification>()
                        .eq("id", id)
                        .set("is_read", 1));
        return Result.success(null, "已标记已读");
    }

    // 3. 全部标记已读（按类型）
    @PostMapping("/readAll/{userId}")
    public Result<String> markAllRead(@PathVariable Long userId, @RequestParam(defaultValue = "-1") Integer type) {
        UpdateWrapper<Notification> wrapper = new UpdateWrapper<Notification>()
                .eq("user_id", userId)
                .eq("is_read", 0)
                .set("is_read", 1);
        if (type >= 0) wrapper.eq("type", type);
        notificationService.update(wrapper);
        return Result.success(null, "已全部标记已读");
    }

    // 4. 获取未读总数（用于导航栏红点）
    @GetMapping("/unread/{userId}")
    public Result<Long> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.count(
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .eq("is_read", 0));
        return Result.success(count, "获取成功");
    }
}