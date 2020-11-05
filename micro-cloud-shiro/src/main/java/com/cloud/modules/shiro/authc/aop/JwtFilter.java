package com.cloud.modules.shiro.authc.aop;

import com.cloud.annotation.AnonymousAccess;
import com.cloud.common.constant.CacheConstant;
import com.cloud.common.constant.CommonConstant;
import com.cloud.modules.shiro.authc.JwtToken;
import com.cloud.modules.shiro.vo.DefContants;
import com.cloud.modules.system.entity.LoginUser;
import com.cloud.modules.system.entity.SysUser;
import com.cloud.modules.utils.JwtUtil;
import com.cloud.modules.utils.RedisUtils;
import com.cloud.modules.utils.SpringContextUtils;
import com.cloud.modules.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * 鉴权登录鉴权拦截器
 */
@Slf4j
public class JwtFilter extends BasicHttpAuthenticationFilter {

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 执行登录认证
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        try {
            // 获取到请求的URL
            String url = ((HttpServletRequest) request).getRequestURI();

            // 获取匿名标记 url:@AnonymousAccess
            Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = SpringContextUtils.getBean(RequestMappingHandlerMapping.class)
                   .getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> infoEntry : handlerMethodMap.entrySet()) {
                Set<String> requestURIS = infoEntry.getKey().getPatternsCondition().getPatterns();
                for(String requestURI : requestURIS){
                    // 如果当前访问路径  使用 @AnonymousAccess 注解直接放行
                    if(url.equals(requestURI)){
                        HandlerMethod handlerMethod = infoEntry.getValue();
                        AnonymousAccess anonymousAccess = handlerMethod.getMethodAnnotation(AnonymousAccess.class);
                        if (null != anonymousAccess) {
                            log.info("-------使用匿名访问注解 @AnonymousAccess 放行--------");
                            return true;
                        }
                    }
                }
            }
            executeLogin(request, response);
            return true;
        } catch (Exception e) {
            throw new AuthenticationException("Token失效，请重新登录", e);
        }
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getHeader(DefContants.X_ACCESS_TOKEN);
        JwtToken jwtToken = new JwtToken(token);
        // 提交给realm进行登入，如果错误他会抛出异常并被捕获
        getSubject(request, response).login(jwtToken);
        // 如果没有抛出异常则代表登入成功，返回true
        return true;
    }




}
