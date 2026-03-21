package com.hajimi.adoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hajimi.adoption.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}