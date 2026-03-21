package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.*;
import com.hajimi.adoption.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired private PetCatService petCatService;
    @Autowired private SysUserService sysUserService;
    @Autowired private AdoptApplicationService adoptService;
    @Autowired private PostService postService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> data = new HashMap<>();

        // 核心数字
        long totalCats       = petCatService.count();
        long availableCats   = petCatService.count(new QueryWrapper<PetCat>().eq("status", 0));
        long adoptedCats     = petCatService.count(new QueryWrapper<PetCat>().eq("status", 2));
        long totalAdopters   = sysUserService.count(new QueryWrapper<SysUser>().eq("role", 1));
        long totalVolunteers = sysUserService.count(new QueryWrapper<SysUser>().eq("role", 2));
        long totalApps       = adoptService.count();
        long pendingApps     = adoptService.count(new QueryWrapper<AdoptApplication>().eq("status", 0));
        long totalPosts      = postService.count();

        data.put("totalCats",       totalCats);
        data.put("availableCats",   availableCats);
        data.put("adoptedCats",     adoptedCats);
        data.put("totalAdopters",   totalAdopters);
        data.put("totalVolunteers", totalVolunteers);
        data.put("totalApps",       totalApps);
        data.put("pendingApps",     pendingApps);
        data.put("totalPosts",      totalPosts);

        // 近7天每日新增申请数（折线图）
        List<String> days   = new ArrayList<>();
        List<Long>   counts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date      = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end   = date.plusDays(1).atStartOfDay();
            long cnt = adoptService.count(
                    new QueryWrapper<AdoptApplication>()
                            .ge("apply_time", start)
                            .lt("apply_time", end));
            days.add(date.getMonthValue() + "/" + date.getDayOfMonth());
            counts.add(cnt);
        }
        data.put("chartDays",   days);
        data.put("chartCounts", counts);

        // 申请状态分布（饼图）
        long s0 = adoptService.count(new QueryWrapper<AdoptApplication>().eq("status", 0));
        long s1 = adoptService.count(new QueryWrapper<AdoptApplication>().eq("status", 1));
        long s2 = adoptService.count(new QueryWrapper<AdoptApplication>().eq("status", 2));
        long s3 = adoptService.count(new QueryWrapper<AdoptApplication>().eq("status", 3));
        long s4 = adoptService.count(new QueryWrapper<AdoptApplication>().eq("status", 4));
        data.put("pieData", List.of(
                Map.of("name", "待审核", "value", s0),
                Map.of("name", "待签约", "value", s1),
                Map.of("name", "已驳回", "value", s2),
                Map.of("name", "已完成", "value", s3),
                Map.of("name", "已取消", "value", s4)
        ));

        return Result.success(data, "获取成功");
    }
}