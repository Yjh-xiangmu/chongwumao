package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.PetCat; // <-- 就是少了这个关键的引入
import com.hajimi.adoption.entity.PetCatRecord;
import com.hajimi.adoption.service.PetCatRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/record")
public class PetCatRecordController {

    @Autowired
    private PetCatRecordService recordService;

    // 录入新履历
    @PostMapping("/add")
    public Result<String> addRecord(@RequestBody PetCatRecord record) {
        record.setCreateTime(LocalDateTime.now());
        recordService.save(record);
        return Result.success(null, "动态履历记录成功！");
    }

    // 获取某只猫咪的所有履历（按时间倒序，最新的在最上面）
    @GetMapping("/list/{catId}")
    public Result<List<PetCatRecord>> getRecordsByCatId(@PathVariable Long catId) {
        List<PetCatRecord> list = recordService.list(
                new QueryWrapper<PetCatRecord>()
                        .eq("cat_id", catId)
                        .orderByDesc("create_time")
        );
        return Result.success(list, "获取履历成功");
    }
}