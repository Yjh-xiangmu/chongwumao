package com.hajimi.adoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hajimi.adoption.entity.SysUser;
import com.hajimi.adoption.mapper.SysUserMapper;
import com.hajimi.adoption.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser login(SysUser user) {
        // 将前端传来的密码进行 MD5 加密后再去数据库比对
        String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        // 核心修改：这里将 "username" 改成了 "phone" 进行查询验证
        wrapper.eq("phone", user.getPhone())
                .eq("password", md5Password);

        SysUser loginUser = this.getOne(wrapper);
        if (loginUser == null) {
            throw new RuntimeException("手机号或密码错误");
        }
        return loginUser;
    }

    @Override
    public void register(SysUser user) {
        // 1. 检查手机号是否已被注册
        if (this.count(new QueryWrapper<SysUser>().eq("phone", user.getPhone())) > 0) {
            throw new RuntimeException("该手机号已注册，请直接登录");
        }
        // 2. 检查身份证号是否重复
        if (this.count(new QueryWrapper<SysUser>().eq("id_card", user.getIdCard())) > 0) {
            throw new RuntimeException("该身份证号已绑定过账号");
        }

        // 3. 密码 MD5 加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        // 4. 默认角色设为 1 (领养人)
        user.setRole(1);

        this.save(user);
    }
}