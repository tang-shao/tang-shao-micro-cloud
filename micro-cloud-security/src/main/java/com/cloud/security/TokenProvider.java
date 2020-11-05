package com.cloud.security;

import cn.hutool.core.util.ObjectUtil;
import com.cloud.common.constant.CacheConstant;
import com.cloud.common.constant.CommonConstant;
import com.cloud.common.exception.MicroCloudException;
import com.cloud.config.JwtConfig;
import com.cloud.modules.system.entity.LoginUser;
import com.cloud.modules.utils.JwtUtil;
import com.cloud.modules.utils.RedisUtils;
import com.cloud.modules.utils.StringUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

/**
 *  token的生成和验证
 *
 * InitializingBean:
 *      1:spring在初始化bean的时候,如果bena实现了接口InitializingBean
 *          则会自动调用afterPropertiesSet()方法
 */
@Slf4j
@Component
public class TokenProvider implements InitializingBean {

    private static final String AUTHORITIES_KEY = "auth";

    @Autowired
    private RedisUtils redisUtils;
    private Key key;

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(JwtConfig.BASE64SECRET);
        this.key = Keys.hmacShaKeyFor(keyBytes);

    }

    /**
     * 创建token
     */
    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        long now = (new Date()).getTime();
        Date validity = new Date(now + JwtConfig.TOKENVALIDITYINSECONDS);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY,authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)                  // 设置token过去时间
                .compact();

    }

    /**
     * 从token中获取到认证
     */
    Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();

        Object authoritiesStr = claims.get(AUTHORITIES_KEY);
        Collection<? extends GrantedAuthority> authorities =
                ObjectUtil.isNotEmpty(authoritiesStr) ?
                        Arrays.stream(authoritiesStr.toString().split(","))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList()) : Collections.emptyList();

        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * token验证
     */
    public boolean validateToken(String token){
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            return true;
        }catch (MicroCloudException e) {
            throw new MicroCloudException("token已过期");
        }
    }

    /**
     * 获取请求中的token
     */
    public String getToken(HttpServletRequest request){
        final String requestHeader = request.getHeader(JwtConfig.HEADER);
        if (requestHeader != null && requestHeader.startsWith(JwtConfig.getTokenStartWith())) {
            return requestHeader.substring(7);
        }
        return null;
    }


    /**
     * 验证Token是否有效
     */
    public Boolean checkUserTokenIsEffect(String token) throws MicroCloudException {
        // 根据token 获取到账号
        String username = JwtUtil.getUsername(token);
        if(StringUtils.isBlank(username)){
            throw new MicroCloudException("非法token");
        }
        // 获取到缓存中的用户
        LoginUser loginUser = (LoginUser) redisUtils.get(CacheConstant.SYS_USERS_CACHE + ":" + token);
        if(loginUser == null){
            throw new MicroCloudException("用户不存在");
        }

        // 判断当前用户的状态
        if(loginUser.getStatus() != 1){
            throw new MicroCloudException("账号已被锁定,请联系管理员");
        }
        // 验证token是否超时 & 账号和密码是否正确  & 刷新token 刷新token(用户在线操作不掉线功能)
        if(!this.jwtTokenRefresh(token,username,loginUser.getPassword())){
            throw new MicroCloudException("Token失效请,重新登录");
        }
        return true;
    }

    /**
     * 刷新Token(保证在线用户操作不掉线功能)
     *      1:登录成功将JWT生层的token 作为key value 保存到cache缓存里面,缓存的有效时间设置为JWT有效时间2倍
     *      2:当用户在请求是,通过自定义过滤器层层校验后进入到身份验证(doGetAuthenticationInfo)
     *      3:当用户再次请求时,如果JWT生成token已超时,但token对应的缓存还存在,则表示用户在一直操作,只是JWT的token过期了
     *          则程序会给给token对应key映射的value重新生成JWTToken并且覆盖value,缓存生命周期重新计算
     *      4:当用户再次请求时,如果JWT生成token已超时,并cache缓存中的token也超时,则表示该账户已超时,返回信息失效,重新登录
     */
    public boolean jwtTokenRefresh(String token, String userName, String passWord){
        // 从缓存中获取token
        String cacheToken = String.valueOf(redisUtils.get(CommonConstant.PREFIX_USER_TOKEN + token));
        // 如果缓存中token已过期,返回false  账号已过期 重新登录
        if(StringUtils.isNotBlank(cacheToken)){
            // 验证token是否有效 过期
            if(!JwtUtil.verify(token,userName,passWord)){
                log.info("---------------------用户在线操作, 更新token保证不掉线----------------------");
                // 生成新的 token
                String newToken = JwtUtil.sign(userName, passWord);
                // 更新缓存超时时间
                redisUtils.set(CommonConstant.PREFIX_USER_TOKEN + newToken,newToken);
                redisUtils.expire(CommonConstant.PREFIX_USER_TOKEN + newToken,JwtUtil.EXPIRE_TIME * 2 /1000);
            }
            return true;
        }
        return false;
    }






}
