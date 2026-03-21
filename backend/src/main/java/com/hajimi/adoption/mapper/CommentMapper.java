package com.hajimi.adoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hajimi.adoption.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}