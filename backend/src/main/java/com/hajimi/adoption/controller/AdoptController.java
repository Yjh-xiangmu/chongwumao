package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.AdoptApplication;
import com.hajimi.adoption.entity.PetCat;
import com.hajimi.adoption.entity.SysUser;
import com.hajimi.adoption.service.AdoptApplicationService;
import com.hajimi.adoption.service.PetCatService;
import com.hajimi.adoption.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adopt")
public class AdoptController {

    @Autowired
    private AdoptApplicationService adoptService;
    @Autowired
    private PetCatService petCatService;
    @Autowired
    private SysUserService sysUserService;

    // 1. 提交申请 (带附件)
    @PostMapping("/apply")
    public Result<String> apply(@RequestBody AdoptApplication app) {
        long count = adoptService.count(new QueryWrapper<AdoptApplication>()
                .eq("user_id", app.getUserId())
                .eq("cat_id", app.getCatId())
                .in("status", 0, 1)); // 如果还在待审核或待签约，不能重复申请
        if (count > 0) return Result.error("您已有该猫咪的进度在处理中啦喵~");

        app.setStatus(0);
        app.setApplyTime(LocalDateTime.now());
        adoptService.save(app);

        PetCat cat = petCatService.getById(app.getCatId());
        if (cat != null && cat.getStatus() == 0) {
            cat.setStatus(1); // 猫咪变为: 已被申请
            petCatService.updateById(cat);
        }
        return Result.success(null, "申请提交成功！");
    }

    // 2. 获取所有申请 (救助员用)
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getList() {
        List<AdoptApplication> apps = adoptService.list(new QueryWrapper<AdoptApplication>().orderByDesc("apply_time"));
        return Result.success(buildAppList(apps), "获取成功");
    }

    // 3. 获取个人的申请记录 (领养人用)
    @GetMapping("/myList/{userId}")
    public Result<List<Map<String, Object>>> getMyList(@PathVariable Long userId) {
        List<AdoptApplication> apps = adoptService.list(new QueryWrapper<AdoptApplication>()
                .eq("user_id", userId).orderByDesc("apply_time"));
        return Result.success(buildAppList(apps), "获取成功");
    }

    // 4. 救助员审核
    @PostMapping("/audit")
    public Result<String> audit(@RequestBody AdoptApplication auditData) {
        AdoptApplication app = adoptService.getById(auditData.getId());
        app.setStatus(auditData.getStatus());
        app.setReviewRemark(auditData.getReviewRemark());
        app.setReviewerId(auditData.getReviewerId());
        app.setReviewTime(LocalDateTime.now());
        adoptService.updateById(app);

        PetCat cat = petCatService.getById(app.getCatId());
        if (cat != null && auditData.getStatus() == 2) {
            // 如果驳回(2)，猫咪恢复待领养(0)。如果同意(1)，猫咪仍是已被申请状态，等签约再变。
            cat.setStatus(0);
            petCatService.updateById(cat);
        }
        return Result.success(null, "审核处理完成！");
    }

    // 5. 领养人签署电子协议
    @PostMapping("/sign")
    public Result<String> signAgreement(@RequestBody AdoptApplication signData) {
        AdoptApplication app = adoptService.getById(signData.getId());
        if (app.getStatus() != 1) return Result.error("当前状态无法签约");

        app.setSignature(signData.getSignature());
        app.setSignTime(LocalDateTime.now());
        app.setStatus(3); // 申请单变为：已完成
        adoptService.updateById(app);

        // 猫咪状态变为：已领养(2)
        PetCat cat = petCatService.getById(app.getCatId());
        if (cat != null) {
            cat.setStatus(2);
            petCatService.updateById(cat);
        }
        return Result.success(null, "签约成功！恭喜您拥有了新的家人喵！");
    }

    // 封装返回数据的公共方法
    private List<Map<String, Object>> buildAppList(List<AdoptApplication> apps) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (AdoptApplication app : apps) {
            Map<String, Object> map = new HashMap<>();
            map.put("application", app);
            map.put("cat", petCatService.getById(app.getCatId()));
            SysUser user = sysUserService.getById(app.getUserId());
            if (user != null) user.setPassword(null);
            map.put("user", user);
            resultList.add(map);
        }
        return resultList;
    }
}