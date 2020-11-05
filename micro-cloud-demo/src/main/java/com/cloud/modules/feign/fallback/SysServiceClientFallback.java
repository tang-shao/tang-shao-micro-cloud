package com.cloud.modules.feign.fallback;

import com.cloud.common.api.Result;
import com.cloud.modules.feign.SysServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SysServiceClientFallback implements SysServiceClient {
    @Override
    public Result<?> testFeign() {
        return new Result<>().error500("micro-cloud-system 服务调用异常");
    }
}
