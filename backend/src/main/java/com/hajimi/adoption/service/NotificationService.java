package com.hajimi.adoption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hajimi.adoption.entity.Notification;

public interface NotificationService extends IService<Notification> {

    /**
     * 发送通知的快捷方法
     * @param userId    接收者ID
     * @param type      大类: 0-系统 1-社区
     * @param subType   子类型字符串
     * @param title     标题
     * @param content   正文
     * @param relatedId 关联业务ID
     */
    void send(Long userId, Integer type, String subType, String title, String content, Long relatedId);
}