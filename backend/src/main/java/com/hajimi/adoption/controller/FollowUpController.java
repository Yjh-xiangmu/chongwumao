package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.AdoptApplication;
import com.hajimi.adoption.entity.FollowUpRecord;
import com.hajimi.adoption.entity.PetCat;
import com.hajimi.adoption.entity.SysUser;
import com.hajimi.adoption.service.AdoptApplicationService;
import com.hajimi.adoption.service.FollowUpRecordService;
import com.hajimi.adoption.service.PetCatService;
import com.hajimi.adoption.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/followup")
public class FollowUpController {

    @Autowired
    private FollowUpRecordService followUpService;
    @Autowired
    private AdoptApplicationService adoptService;
    @Autowired
    private PetCatService petCatService;
    @Autowired
    private SysUserService sysUserService;

    // 1. 领养人/救助员提交回访记录
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody FollowUpRecord record) {
        record.setCreateTime(LocalDateTime.now());
        followUpService.save(record);
        return Result.success(null, "回访记录提交成功！");
    }

    // 2. 获取某条申请的所有回访记录
    @GetMapping("/list/{applicationId}")
    public Result<List<FollowUpRecord>> listByApplication(@PathVariable Long applicationId) {
        List<FollowUpRecord> list = followUpService.list(
                new QueryWrapper<FollowUpRecord>()
                        .eq("application_id", applicationId)
                        .orderByDesc("create_time"));
        return Result.success(list, "获取成功");
    }

    // 3. 救助员获取所有已领养记录（含回访状态）
    @GetMapping("/adoptedList")
    public Result<List<Map<String, Object>>> getAdoptedList() {
        // 查出所有已完成领养的申请
        List<AdoptApplication> apps = adoptService.list(
                new QueryWrapper<AdoptApplication>().eq("status", 3));

        List<Map<String, Object>> resultList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (AdoptApplication app : apps) {
            Map<String, Object> map = new HashMap<>();
            map.put("application", app);

            PetCat cat = petCatService.getById(app.getCatId());
            map.put("cat", cat);

            SysUser user = sysUserService.getById(app.getUserId());
            if (user != null) user.setPassword(null);
            map.put("user", user);

            // 查最近一次回访记录
            FollowUpRecord lastRecord = followUpService.getOne(
                    new QueryWrapper<FollowUpRecord>()
                            .eq("application_id", app.getId())
                            .orderByDesc("create_time")
                            .last("LIMIT 1"));
            map.put("lastRecord", lastRecord);

            // 计算是否需要回访（超过15天未提交）
            boolean needFollowUp;
            if (lastRecord == null) {
                // 从签约时间算起
                long daysSinceSign = app.getSignTime() != null
                        ? ChronoUnit.DAYS.between(app.getSignTime(), now)
                        : 999;
                needFollowUp = daysSinceSign >= 15;
            } else {
                long daysSinceLast = ChronoUnit.DAYS.between(lastRecord.getCreateTime(), now);
                needFollowUp = daysSinceLast >= 15;
            }
            map.put("needFollowUp", needFollowUp);

            resultList.add(map);
        }

        return Result.success(resultList, "获取成功");
    }
}