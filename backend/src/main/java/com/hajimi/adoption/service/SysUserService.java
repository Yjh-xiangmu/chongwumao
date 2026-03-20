package com.hajimi.adoption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hajimi.adoption.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser login(SysUser user);
    void register(SysUser user);
}