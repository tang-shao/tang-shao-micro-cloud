package com.cloud.config;

import com.cloud.annotation.AnonymousAccess;
import com.cloud.security.JwtAccessDeniedHandler;
import com.cloud.security.JwtAuthenticationEntryPoint;
import com.cloud.security.TokenConfigurer;
import com.cloud.modules.utils.SpringContextUtils;
import com.cloud.security.TokenFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Slf4j
@Configuration
@EnableWebSecurity      // 启用Spring Security的Web安全支持
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // @AnonymousAccess 允许匿名访问接口
        // 获取匿名标记 url:@AnonymousAccess
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap =
                SpringContextUtils.getBean(RequestMappingHandlerMapping.class).getHandlerMethods();
        Set<String> anonymousUrls = new HashSet<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> infoEntry : handlerMethodMap.entrySet()) {
            HandlerMethod handlerMethod = infoEntry.getValue();
            AnonymousAccess anonymousAccess = handlerMethod.getMethodAnnotation(AnonymousAccess.class);
            if (null != anonymousAccess) {
                anonymousUrls.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
            }
        }

        // 禁用 CSRF
        httpSecurity
                // https://blog.csdn.net/qq_35226176/article/details/106154774
                // 禁用 CSRF 麻蛋 如果不禁用则无法处理Post请求(既不报错 也不返回东西)
                .csrf().disable()
                // 允许配置异常处理
                .exceptionHandling()
                // 认证
                .authenticationEntryPoint(authenticationEntryPoint)
                // 授权
                .accessDeniedHandler(jwtAccessDeniedHandler)

                // 防止iframe 造成跨域
                .and()
                // 将安全标头添加到响应
                .headers()
                .frameOptions()
                .disable()

                // 不创建会话
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                // 授权配置
                .authorizeRequests()

                .antMatchers("/").permitAll()
                // 静态资源等等
                .antMatchers(
                        HttpMethod.GET,
                        "/*.html",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/webSocket/**"
                ).permitAll()

                // swagger 文档
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/swagger**/**").permitAll()
                .antMatchers("/v2/**").permitAll()

                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/*/api-docs").permitAll()
                // 文件
                .antMatchers("/avatar/**").permitAll()
                .antMatchers("/file/**").permitAll()
                // 阿里巴巴 druid
                .antMatchers("/druid/**").permitAll()
                // 放行OPTIONS请求
                // .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 自定义匿名访问所有url放行
                .antMatchers(anonymousUrls.toArray(new String[0])).permitAll()
                // 所有请求都拦截
                .anyRequest().authenticated()
                // 自定义权限拦截器JWT过滤器
                .and()
                .apply(TokenConfigurer());
    }

    @Bean
    public TokenConfigurer TokenConfigurer() {
        return new TokenConfigurer();
    }

}
