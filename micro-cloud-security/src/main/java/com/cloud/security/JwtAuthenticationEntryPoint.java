package com.cloud.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AuthenticationEntryPoint：未登陆或者登陆过期
 *      1:对匿名用户进行拦截(不带token访问,未授权)
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e) throws IOException, ServletException {
        // 当不提供任何凭证式访问安全的REST安全资源 调用此方法
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"token已过期或不存在");
    }
}
