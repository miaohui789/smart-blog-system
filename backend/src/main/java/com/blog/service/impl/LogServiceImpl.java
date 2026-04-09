package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.LoginLog;
import com.blog.entity.OperationLog;
import com.blog.mapper.LoginLogMapper;
import com.blog.mapper.LogMapper;
import com.blog.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl extends ServiceImpl<LogMapper, OperationLog> implements LogService {

    private final LoginLogMapper loginLogMapper;

    @Override
    public void saveOperationLog(OperationLog operationLog) {
        try {
            save(operationLog);
        } catch (Exception e) {
            log.warn("保存操作日志失败: {}", e.getMessage());
        }
    }

    @Override
    public void saveLoginLog(LoginLog loginLog) {
        try {
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.warn("保存登录日志失败: {}", e.getMessage());
        }
    }
}
