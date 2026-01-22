package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.SystemVersion;
import com.blog.mapper.SystemVersionMapper;
import com.blog.service.SystemVersionService;
import org.springframework.stereotype.Service;

@Service
public class SystemVersionServiceImpl extends ServiceImpl<SystemVersionMapper, SystemVersion> implements SystemVersionService {
}
