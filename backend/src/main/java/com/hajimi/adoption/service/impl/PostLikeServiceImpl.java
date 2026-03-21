package com.hajimi.adoption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hajimi.adoption.entity.PostLike;
import com.hajimi.adoption.mapper.PostLikeMapper;
import com.hajimi.adoption.service.PostLikeService;
import org.springframework.stereotype.Service;

@Service
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements PostLikeService {
}