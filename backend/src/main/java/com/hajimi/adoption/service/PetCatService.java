package com.hajimi.adoption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hajimi.adoption.entity.PetCat;

// 必须继承 IService 才能拥有原生的 save、list 等方法
public interface PetCatService extends IService<PetCat> {
}