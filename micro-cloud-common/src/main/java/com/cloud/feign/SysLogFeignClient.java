package com.cloud.feign;

import com.alibaba.fastjson.JSONObject;
import com.cloud.common.api.Result;
import com.cloud.feign.fallback.SysLogFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(contextId = "sysLogFeignClient",value = "micro-cloud-system",fallback = SysLogFeignClientFallback.class)
public interface SysLogFeignClient {

    /**
     * 保存日志
     */
    @PostMapping("/sys/log/save")
    Result<?> saveSysLog(@RequestBody JSONObject jsonObject);

}
