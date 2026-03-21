package com.hajimi.adoption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hajimi.adoption.entity.FollowUpRecord;
import com.hajimi.adoption.mapper.FollowUpRecordMapper;
import com.hajimi.adoption.service.FollowUpRecordService;
import org.springframework.stereotype.Service;

@Service
public class FollowUpRecordServiceImpl extends ServiceImpl<FollowUpRecordMapper, FollowUpRecord> implements FollowUpRecordService {
}