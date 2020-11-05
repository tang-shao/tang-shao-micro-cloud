package com.cloud.modules.controller;

import com.cloud.annotation.AnonymousAccess;
import com.cloud.common.api.Result;
import com.cloud.modules.feign.SysServiceClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/demo")
@Api(tags = "测试Feign调用")
public class DemoController {

    @Autowired
    private SysServiceClient sysServiceClient;

    @GetMapping("/test")
    @AnonymousAccess
    @ApiOperation("Feign调用 熔断测试")
    public Result<?> testFeign(){
        return sysServiceClient.testFeign();
    }


    @GetMapping("/test1")
    // @AnonymousAccess
    @ApiOperation("测试 Spring Security权限")
    public Result<?> testSecurity(){
        return Result.ok("hello  world.................");
    }


}
