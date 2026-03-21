package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.SysUser;
import com.hajimi.adoption.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    // 1. 获取救助员列表
    @GetMapping("/volunteers")
    public Result<List<SysUser>> getVolunteers() {
        List<SysUser> list = sysUserService.list(new QueryWrapper<SysUser>().eq("role", 2));
        list.forEach(user -> user.setPassword(null));
        return Result.success(list, "获取救助员列表成功");
    }

    // 2. 管理员添加救助员
    @PostMapping("/addVolunteer")
    public Result<String> addVolunteer(@RequestBody SysUser user) {
        if (sysUserService.count(new QueryWrapper<SysUser>().eq("phone", user.getPhone())) > 0) {
            return Result.error("该手机号已存在，无法添加");
        }
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        user.setRole(2);
        sysUserService.save(user);
        return Result.success(null, "救助员添加成功，初始密码为: 123456");
    }

    // 3. 删除用户
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success(null, "删除成功");
    }

    // 4. 获取用户信息（个人中心用）
    @GetMapping("/info/{id}")
    public Result<SysUser> getUserInfo(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) return Result.error("用户不存在");
        user.setPassword(null);
        return Result.success(user, "获取成功");
    }

    // 5. 更新个人信息（昵称、地址、头像等，不含密码和手机号）
    @PutMapping("/update")
    public Result<SysUser> updateProfile(@RequestBody SysUser user) {
        // 只允许更新这几个字段，防止越权修改角色/手机号等敏感字段
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setUsername(user.getUsername());
        update.setAddress(user.getAddress());
        update.setAvatar(user.getAvatar());
        update.setUpdateTime(LocalDateTime.now());
        sysUserService.updateById(update);

        // 返回最新的完整用户信息（供前端刷新 localStorage）
        SysUser latest = sysUserService.getById(user.getId());
        latest.setPassword(null);
        return Result.success(latest, "更新成功");
    }

    // 6. 修改密码
    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestBody ChangePasswordDTO dto) {
        SysUser user = sysUserService.getById(dto.getUserId());
        if (user == null) return Result.error("用户不存在");

        String oldMd5 = DigestUtils.md5DigestAsHex(dto.getOldPassword().getBytes());
        if (!oldMd5.equals(user.getPassword())) {
            return Result.error("原密码不正确");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes()));
        user.setUpdateTime(LocalDateTime.now());
        sysUserService.updateById(user);
        return Result.success(null, "密码修改成功，请重新登录");
    }

    // 内部 DTO
    public static class ChangePasswordDTO {
        private Long userId;
        private String oldPassword;
        private String newPassword;
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}