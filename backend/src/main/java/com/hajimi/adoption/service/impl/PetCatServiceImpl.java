package com.hajimi.adoption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hajimi.adoption.entity.PetCat;
import com.hajimi.adoption.mapper.PetCatMapper;
import com.hajimi.adoption.service.PetCatService;
import org.springframework.stereotype.Service;

// 必须添加 @Service 注解，并继承 ServiceImpl
@Service
public class PetCatServiceImpl extends ServiceImpl<PetCatMapper, PetCat> implements PetCatService {
}