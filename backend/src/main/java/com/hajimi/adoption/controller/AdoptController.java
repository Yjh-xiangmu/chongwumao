package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.AdoptApplication;
import com.hajimi.adoption.entity.PetCat;
import com.hajimi.adoption.service.AdoptApplicationService;
import com.hajimi.adoption.service.PetCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/adopt")
public class AdoptController {

    @Autowired
    private AdoptApplicationService adoptService;
    @Autowired
    private PetCatService petCatService;

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody AdoptApplication app) {
        // 1. 防重复提交检查：看看这个用户是不是已经申请过这只猫了（且还在待审核状态）
        long count = adoptService.count(new QueryWrapper<AdoptApplication>()
                .eq("user_id", app.getUserId())
                .eq("cat_id", app.getCatId())
                .eq("status", 0));
        if (count > 0) {
            return Result.error("您已经提交过这只猫咪的申请啦，请耐心等待救助员审核喵~");
        }

        // 2. 保存申请记录
        app.setStatus(0); // 默认为待审核
        app.setApplyTime(LocalDateTime.now());
        adoptService.save(app);

        // 3. 将猫咪的状态更新为 "已被申请" (1)，防止被其他人重复申请
        PetCat cat = petCatService.getById(app.getCatId());
        if (cat != null && cat.getStatus() == 0) {
            cat.setStatus(1);
            petCatService.updateById(cat);
        }

        return Result.success(null, "申请提交成功！救助员会尽快与您联系。");
    }
}