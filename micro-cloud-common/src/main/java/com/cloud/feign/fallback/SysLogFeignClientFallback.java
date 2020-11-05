package com.cloud.feign.fallback;

import com.alibaba.fastjson.JSONObject;
import com.cloud.common.api.Result;
import com.cloud.feign.SysLogFeignClient;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 服务熔断
 */
@Slf4j
@Component
public class SysLogFeignClientFallback implements SysLogFeignClient {

    @Setter
    private Throwable cause;

    @Override
    public Result<?> saveSysLog(JSONObject jsonObject) {
        Result<Object> res = new Result<>();
        res.error500("micro-cloud-system  服务调用调用异常");
        return res;
    }
}
