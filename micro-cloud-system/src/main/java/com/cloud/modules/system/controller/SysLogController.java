package com.cloud.modules.system.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.common.api.Result;
import com.cloud.modules.system.service.ISysLogService;
import com.cloud.modules.system.entity.SysLog;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api("日志操作类")
@RequestMapping("/sys/log")
public class SysLogController {

    @Autowired
    private ISysLogService sysLogService;

    /**
     * 保存日志:
     *      1: AOP操作 Fegin远程调用, 用户对系统操作一次 程序 会自动调用
     */
    @PostMapping("/save")
    // @ApiOperation("新增日志")   由于是系统自动调用 该接口不需要提供给前端工程师
    public Result<?> saveLog(@RequestBody JSONObject jsonObject){
        Result<?> res = new Result<>();
        // JSON字符串转对象
        SysLog sysLog = JSON.parseObject(jsonObject.toJSONString(), SysLog.class);
        sysLogService.save(sysLog);
        return res;
    }


}
