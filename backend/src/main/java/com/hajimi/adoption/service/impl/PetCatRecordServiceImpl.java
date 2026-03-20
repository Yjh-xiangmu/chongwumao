package com.hajimi.adoption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hajimi.adoption.entity.PetCatRecord;
import com.hajimi.adoption.mapper.PetCatRecordMapper;
import com.hajimi.adoption.service.PetCatRecordService;
import org.springframework.stereotype.Service;

@Service
public class PetCatRecordServiceImpl extends ServiceImpl<PetCatRecordMapper, PetCatRecord> implements PetCatRecordService {
}