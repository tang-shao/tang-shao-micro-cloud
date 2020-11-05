package com.cloud.modules.feign;

import com.cloud.common.api.Result;
import com.cloud.modules.feign.fallback.SysServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@Component
@FeignClient(contextId = "sysServiceClient",value = "micro-cloud-system",fallback = SysServiceClientFallback.class)
public interface SysServiceClient {

    @GetMapping("/sys/test")
    public Result<?> testFeign();

}
