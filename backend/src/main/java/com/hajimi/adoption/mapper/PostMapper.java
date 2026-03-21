package com.hajimi.adoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hajimi.adoption.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}