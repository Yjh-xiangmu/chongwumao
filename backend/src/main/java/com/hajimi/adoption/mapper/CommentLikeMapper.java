package com.hajimi.adoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hajimi.adoption.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
}