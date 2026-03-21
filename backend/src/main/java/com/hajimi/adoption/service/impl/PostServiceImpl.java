package com.hajimi.adoption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hajimi.adoption.entity.Post;
import com.hajimi.adoption.mapper.PostMapper;
import com.hajimi.adoption.service.PostService;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
}