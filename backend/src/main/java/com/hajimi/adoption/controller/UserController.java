package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.SysUser;
import com.hajimi.adoption.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    // 1. 获取救助员列表 (条件: role = 2)
    @GetMapping("/volunteers")
    public Result<List<SysUser>> getVolunteers() {
        List<SysUser> list = sysUserService.list(new QueryWrapper<SysUser>().eq("role", 2));
        // 安全起见，把密码清空后再返回给前端
        list.forEach(user -> user.setPassword(null));
        return Result.success(list, "获取救助员列表成功");
    }

    // 2. 管理员添加救助员
    @PostMapping("/addVolunteer")
    public Result<String> addVolunteer(@RequestBody SysUser user) {
        // 校验手机号是否已存在
        if (sysUserService.count(new QueryWrapper<SysUser>().eq("phone", user.getPhone())) > 0) {
            return Result.error("该手机号已存在，无法添加");
        }

        // 救助员账号由管理员分配，默认密码统一设置为 123456 (存入数据库前需 MD5 加密)
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 强制设置角色为 2 (救助员)
        user.setRole(2);

        sysUserService.save(user);
        return Result.success(null, "救助员添加成功，初始密码为: 123456");
    }

    // 3. 删除用户 (辞退救助员)
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success(null, "删除成功");
    }
}