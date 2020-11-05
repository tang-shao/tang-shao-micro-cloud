package com.cloud.modules.utils;

import com.cloud.common.constant.CacheConstant;
import com.cloud.common.constant.CommonConstant;
import com.cloud.common.exception.MicroCloudException;
import com.cloud.modules.system.entity.LoginUser;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class TokenUtils {

    /**
     * 获取 request 中传递的Token
     */
    public static String getTokenByRequest(HttpServletRequest request){
        String token = request.getParameter("token");
        if(StringUtils.isBlank(token)){
            token = request.getHeader("X-Access-Token");
        }
        return token;
    }

    /**
     * token验证
     */
    public static Boolean verifyToken(HttpServletRequest request, RedisUtils redisUtil) {
        log.info("---url---" + request.getRequestURI());
        String token = getTokenByRequest(request);
        if(StringUtils.isBlank(token)){
            throw new MicroCloudException("token不能为空!");
        }

        // 解密获得username，用于和数据库进行对比
        String username = JwtUtil.getUsername(token);
        if(StringUtils.isBlank(username)){
            throw new MicroCloudException("token非法无效!");
        }

        // 根据用户名获取用户信息
        LoginUser loginUser = (LoginUser)redisUtil.get(CacheConstant.SYS_USERS_CACHE_JWT + token);
        // 判断用户状态
        if(loginUser == null)
            throw new MicroCloudException("用户不存在!");

        // 判断用户状态
        if (loginUser.getStatus() != 1) {
            throw new MicroCloudException("账号已被锁定,请联系管理员!");
        }

        // 验证token是否超时 & 用户名 或者密码错误
        if (!jwtTokenRefresh(token, username, loginUser.getPassword(), redisUtil)) {
            throw new MicroCloudException("Token失效，请重新登录!");
        }
        return true;
    }

    /**
     * 刷新token（保证用户在线操作不掉线）
     */
    private static boolean jwtTokenRefresh(String token, String username, String password, RedisUtils redisUtils){
        String cacheToken = String.valueOf(redisUtils.get(CommonConstant.PREFIX_USER_TOKEN + token));
        // 如果token 不为空
        if(StringUtils.isNotBlank(cacheToken)){
            // 验证token有消息
            if(!JwtUtil.verify(token, username, password)){
                // 生成新的验证签名
                String newAuthorization = JwtUtil.sign(username, password);
                // 设置 token有效时间
                redisUtils.set(CommonConstant.PREFIX_USER_TOKEN + token, newAuthorization);
                redisUtils.expire(CommonConstant.PREFIX_USER_TOKEN + token,JwtUtil.EXPIRE_TIME * 2 /1000);
                return true;
            }
        }
        return false;
    }

}
