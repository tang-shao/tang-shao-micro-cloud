package com.cloud.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

/**
 * 网关全局 拦截器
 * Tang Can
 */
@Slf4j
@Component
public class GlobalAccessTokenFilter implements GlobalFilter, Ordered {

    public final static String X_ACCESS_TOKEN = "X-Access-Token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求路径
        String url = exchange.getRequest().getURI().getPath();
        log.info("=======access url =======" + url);
        // 获取真实路径
        addOriginalRequestUrl(exchange, exchange.getRequest().getURI());
        String rawPath = exchange.getRequest().getURI().getRawPath();
        String newPath = "/" +
                Arrays.stream(StringUtils.tokenizeToStringArray(rawPath, "/")).
                        skip(1L).collect(Collectors.joining("/"));

        ServerHttpRequest newRequest = exchange.getRequest().mutate().path(newPath).build();
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());

        ServerHttpRequest mutableReq = exchange.getRequest().mutate().header("Authorization-UserName", "").build();
        ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();
        return chain.filter(mutableExchange);

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
