package com.cloud.security;

import cn.hutool.core.util.StrUtil;
import com.cloud.bo.SysUserDetails;
import com.cloud.common.constant.CacheConstant;
import com.cloud.common.constant.CommonConstant;
import com.cloud.common.exception.MicroCloudException;
import com.cloud.config.JwtConfig;
import com.cloud.modules.system.entity.LoginUser;
import com.cloud.modules.system.entity.SysUser;
import com.cloud.modules.utils.JwtUtil;
import com.cloud.modules.utils.RedisUtils;
import com.cloud.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 自定义拦截器   主要验证 请求中的token  是否非法  是否已过期 是否已存在  .....
 * Tang Can
 */
@Slf4j
public class TokenFilter extends GenericFilterBean {

    /**
     * 牛皮 不知道为啥子 换一个接口 注入不进来
     */
    // @Autowired
    // private RedisUtils redisUtils;
    private RedisUtils redisUtils = new RedisUtils();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        String token = ((HttpServletRequest)servletRequest).getHeader(JwtConfig.HEADER);
        if(token != null){
            String username = JwtTokenUtil.getUserNameFromToken(token);
            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                LoginUser loginUser = (LoginUser) redisUtils.get(CacheConstant.SYS_USERS_CACHE + ":" + token);
                SysUserDetails loginDetails = new SysUserDetails(loginUser);
                if(JwtTokenUtil.validateToken(token,loginDetails)){
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(loginDetails, null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        }
        filterChain.doFilter(servletRequest,servletResponse);
    }


}
