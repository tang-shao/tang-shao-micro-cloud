package com.cloud.feign.factory;

import com.cloud.feign.SysLogFeignClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
public class SysLogFeignClientFallbackFactory implements FallbackFactory<SysLogFeignClient> {

    @Override
    public SysLogFeignClient create(Throwable throwable) {

        return null;
    }
}
