package com.cloud.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.modules.system.mapper.SysLogMapper;
import com.cloud.modules.system.service.ISysLogService;
import com.cloud.modules.system.entity.SysLog;
import org.springframework.stereotype.Service;

@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements ISysLogService {
}
