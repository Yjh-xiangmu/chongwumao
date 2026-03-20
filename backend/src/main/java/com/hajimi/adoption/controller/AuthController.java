package com.hajimi.adoption.controller;

import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.SysUser;
import com.hajimi.adoption.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody SysUser user) {
        try {
            SysUser loginUser = sysUserService.login(user);
            // 登录成功后，安全起见把密码抹除再返回给前端
            loginUser.setPassword(null);
            return Result.success(loginUser, "登录成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody SysUser user) {
        try {
            sysUserService.register(user);
            return Result.success(null, "注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}