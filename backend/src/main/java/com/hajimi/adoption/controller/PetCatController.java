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

    // 1. 获取猫咪列表
    @GetMapping("/list")
    public Result<List<PetCat>> list() {
        List<PetCat> list = petCatService.list(new QueryWrapper<PetCat>().orderByDesc("create_time"));
        return Result.success(list, "获取猫咪档案成功");
    }

    // 2. 新增猫咪
    @PostMapping("/add")
    public Result<String> add(@RequestBody PetCat cat) {
        cat.setStatus(0); // 默认待领养状态
        petCatService.save(cat);
        return Result.success(null, "猫咪档案录入成功！");
    }

    // 3. 修改/更新猫咪
    // 注意这里必须是 @PutMapping，因为前端用的是 axios.put
    @PutMapping("/update")
    public Result<String> update(@RequestBody PetCat cat) {
        cat.setUpdateTime(java.time.LocalDateTime.now());
        // updateById 是 MyBatis-Plus 自带的方法，会根据传入对象的 id 去更新数据库
        petCatService.updateById(cat);
        return Result.success(null, "猫咪档案更新成功喵！");
    }
}