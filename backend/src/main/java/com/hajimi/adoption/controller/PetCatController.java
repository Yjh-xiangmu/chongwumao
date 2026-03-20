package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.PetCat;
import com.hajimi.adoption.service.PetCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cat")
public class PetCatController {

    @Autowired
    private PetCatService petCatService;

    // 获取猫咪列表 (按创建时间倒序)
    @GetMapping("/list")
    public Result<List<PetCat>> list() {
        List<PetCat> list = petCatService.list(new QueryWrapper<PetCat>().orderByDesc("create_time"));
        return Result.success(list, "获取猫咪档案成功");
    }

    // 救助员新增猫咪档案
    @PostMapping("/add")
    public Result<String> add(@RequestBody PetCat cat) {
        cat.setStatus(0); // 默认待领养状态
        petCatService.save(cat);
        return Result.success(null, "猫咪档案录入成功！");
    }
}