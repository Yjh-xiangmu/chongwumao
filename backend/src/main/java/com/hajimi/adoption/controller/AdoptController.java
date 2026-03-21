package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.AdoptApplication;
import com.hajimi.adoption.entity.PetCat;
import com.hajimi.adoption.entity.SysUser;
import com.hajimi.adoption.service.AdoptApplicationService;
import com.hajimi.adoption.service.NotificationService;
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

    @Autowired private AdoptApplicationService adoptService;
    @Autowired private PetCatService petCatService;
    @Autowired private SysUserService sysUserService;
    @Autowired private NotificationService notificationService;

    // 1. 提交申请
    @PostMapping("/apply")
    public Result<String> apply(@RequestBody AdoptApplication app) {
        long count = adoptService.count(new QueryWrapper<AdoptApplication>()
                .eq("user_id", app.getUserId())
                .eq("cat_id", app.getCatId())
                .in("status", 0, 1));
        if (count > 0) return Result.error("您已有该猫咪的进度在处理中啦喵~");

        app.setStatus(0);
        app.setApplyTime(LocalDateTime.now());
        adoptService.save(app);

        PetCat cat = petCatService.getById(app.getCatId());
        if (cat != null && cat.getStatus() == 0) {
            cat.setStatus(1);
            petCatService.updateById(cat);
        }

        // 通知：申请已提交
        notificationService.send(
                app.getUserId(), 0, "APPLY_SUBMIT",
                "领养申请已提交",
                "您对猫咪「" + (cat != null ? cat.getNickname() : "") + "」的领养申请已提交，请耐心等待救助员审核~",
                app.getId()
        );

        return Result.success(null, "申请提交成功！");
    }

    // 2. 获取所有申请（救助员用）
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getList() {
        List<AdoptApplication> apps = adoptService.list(
                new QueryWrapper<AdoptApplication>().orderByDesc("apply_time"));
        return Result.success(buildAppList(apps), "获取成功");
    }

    // 3. 获取个人申请记录（领养人用）
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
        String catName = cat != null ? cat.getNickname() : "";

        if (cat != null && auditData.getStatus() == 2) {
            cat.setStatus(0);
            petCatService.updateById(cat);
        }

        // 通知：审核结果
        if (auditData.getStatus() == 1) {
            notificationService.send(
                    app.getUserId(), 0, "APPLY_APPROVED",
                    "🎉 领养申请已通过审核",
                    "您对猫咪「" + catName + "」的申请已通过审核！请前往「我的领养记录」签署领养协议。",
                    app.getId()
            );
        } else if (auditData.getStatus() == 2) {
            notificationService.send(
                    app.getUserId(), 0, "APPLY_REJECTED",
                    "领养申请未通过",
                    "您对猫咪「" + catName + "」的申请未通过审核。原因：" + auditData.getReviewRemark(),
                    app.getId()
            );
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
        app.setStatus(3);
        adoptService.updateById(app);

        PetCat cat = petCatService.getById(app.getCatId());
        if (cat != null) {
            cat.setStatus(2);
            petCatService.updateById(cat);
        }

        // 通知：签约完成
        notificationService.send(
                app.getUserId(), 0, "SIGN_SUCCESS",
                "🐱 签约完成，欢迎新家人！",
                "恭喜！您已成功领养猫咪「" + (cat != null ? cat.getNickname() : "") + "」，请记得每15天提交一次生活记录哦~",
                app.getId()
        );

        return Result.success(null, "签约成功！恭喜您拥有了新的家人喵！");
    }

    // 6. 领养人取消申请（仅限待审核状态）
    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id) {
        AdoptApplication app = adoptService.getById(id);
        if (app == null) return Result.error("申请记录不存在");
        if (app.getStatus() != 0) return Result.error("当前状态无法取消，仅待审核中的申请可以取消");

        app.setStatus(4);
        adoptService.updateById(app);

        PetCat cat = petCatService.getById(app.getCatId());
        if (cat != null && cat.getStatus() == 1) {
            cat.setStatus(0);
            petCatService.updateById(cat);
        }
        return Result.success(null, "申请已取消");
    }

    // 封装返回数据
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