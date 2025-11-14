package org.example.angelbacked.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.angelbacked.entity.AdminLog;
import org.example.angelbacked.mapper.AdminLogMapper;
import org.example.angelbacked.service.AdminLogService;
import org.springframework.stereotype.Service;

@Service
public class AdminLogServiceImpl extends ServiceImpl<AdminLogMapper, AdminLog> implements AdminLogService {
}