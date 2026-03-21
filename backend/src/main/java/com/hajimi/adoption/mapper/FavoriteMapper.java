package com.hajimi.adoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hajimi.adoption.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}