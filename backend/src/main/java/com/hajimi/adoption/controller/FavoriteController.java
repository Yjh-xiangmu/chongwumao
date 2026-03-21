package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.Favorite;
import com.hajimi.adoption.entity.PetCat;
import com.hajimi.adoption.service.FavoriteService;
import com.hajimi.adoption.service.PetCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired private FavoriteService favoriteService;
    @Autowired private PetCatService petCatService;

    // 收藏 / 取消收藏（toggle）
    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggle(
            @RequestParam Long userId,
            @RequestParam Long catId) {

        QueryWrapper<Favorite> qw = new QueryWrapper<Favorite>()
                .eq("user_id", userId).eq("cat_id", catId);
        Favorite exist = favoriteService.getOne(qw);

        boolean favored;
        if (exist != null) {
            favoriteService.remove(qw);
            favored = false;
        } else {
            Favorite f = new Favorite();
            f.setUserId(userId);
            f.setCatId(catId);
            f.setCreateTime(LocalDateTime.now());
            favoriteService.save(f);
            favored = true;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("favored", favored);
        return Result.success(data, favored ? "收藏成功" : "已取消收藏");
    }

    // 查询某用户的收藏列表（带猫咪详情）
    @GetMapping("/list/{userId}")
    public Result<List<Map<String, Object>>> listFavorites(@PathVariable Long userId) {
        List<Favorite> favorites = favoriteService.list(
                new QueryWrapper<Favorite>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite fav : favorites) {
            Map<String, Object> map = new HashMap<>();
            map.put("favorite", fav);
            map.put("cat", petCatService.getById(fav.getCatId()));
            result.add(map);
        }
        return Result.success(result, "获取成功");
    }

    // 检查某用户是否收藏了某猫
    @GetMapping("/check")
    public Result<Boolean> check(
            @RequestParam Long userId,
            @RequestParam Long catId) {
        boolean favored = favoriteService.count(
                new QueryWrapper<Favorite>()
                        .eq("user_id", userId).eq("cat_id", catId)) > 0;
        return Result.success(favored, "获取成功");
    }
}