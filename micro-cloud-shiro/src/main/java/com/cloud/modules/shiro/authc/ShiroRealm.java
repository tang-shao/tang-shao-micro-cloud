package com.cloud.modules.shiro.authc;

import com.cloud.common.constant.CacheConstant;
import com.cloud.common.constant.CommonConstant;
import com.cloud.modules.system.entity.LoginUser;
import com.cloud.modules.system.entity.SysUser;
import com.cloud.modules.utils.JwtUtil;
import com.cloud.modules.utils.RedisUtils;
import com.cloud.modules.utils.RequestHolder;
import com.cloud.modules.utils.StringUtils;
import com.cloud.modules.utils.oConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户认证
 */
@Slf4j
@Component
public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 好坑 shiro不重写 这个方法 会报token不能识别
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    /**
     *
     * 用户认证：
     *      1：实在用户登录时进行身份验证
     *      2：验证账户和密码是否正确
     *
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth)
            throws AuthenticationException {
        log.info("====================================开始认证===========================");
        String token = (String) auth.getCredentials();
        if(StringUtils.isBlank(token)){
            // 如果Token 为空 直接抛出异常信息
            log.info("=====身份认证失败====== IP地址为：" + oConvertUtils.getIpAddrByRequest(RequestHolder.getHttpServletRequest()));
            throw new AuthenticationException("token为空");
        }
        // 验证token是否有效
        LoginUser loginUser = this.checkUserTokenIsEffect(token);
        return new SimpleAuthenticationInfo(loginUser,token,getName());
    }


    /**
     * 验证Token是否有效
     */
    public LoginUser checkUserTokenIsEffect(String token) throws AuthenticationException {
        // 根据token 获取到账号
        String username = JwtUtil.getUsername(token);
        if(StringUtils.isBlank(username)){
            throw new AuthenticationException("非法token");
        }
        // 获取到缓存中的用户
        LoginUser loginUser = (LoginUser) redisUtils.get(CacheConstant.SYS_USERS_CACHE + ":" + token);
        if(loginUser == null){
            throw new AuthenticationException("用户不存在");
        }

        // 判断当前用户的状态
        if(loginUser.getStatus() != 1){
            throw new AuthenticationException("账号已被锁定,请联系管理员");
        }
        // 验证token是否超时 & 账号和密码是否正确  & 刷新token 刷新token(用户在线操作不掉线功能)
        if(!this.jwtTokenRefresh(token,username,loginUser.getPassword())){
            throw new AuthenticationException("Token失效请,重新登录");
        }
        return loginUser;
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

    /**
     * 清除当前账户权限 认证缓存
     */
    @Override
    protected void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }
}
