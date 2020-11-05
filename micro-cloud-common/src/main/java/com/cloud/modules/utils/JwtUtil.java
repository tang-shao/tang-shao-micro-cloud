package com.cloud.modules.utils;

import cn.hutool.crypto.SecureUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cloud.common.constant.CacheConstant;
import com.cloud.common.exception.MicroCloudException;
import com.cloud.modules.system.entity.LoginUser;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
public class JwtUtil {

    // Token过期时间30分钟
    public static final long EXPIRE_TIME = 30 * 60 * 1000;

    /**
     * 校验Token是否正确
     */
    public static boolean verify(String token, String username, String password) {
       try {
           // 根据密码生成JWT验证器
           Algorithm algorithm = Algorithm.HMAC256(SecureUtil.md5("password"));
           JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
           // 校验TOKEN
           DecodedJWT jwt = verifier.verify(token);
           return true;
       }catch (Exception e){
           return false;
       }
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     */
    public static String getUsername(String token){
        try {
            // 对token进行解码
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        }catch (JWTDecodeException e){
            return null;
        }
    }


    /**
     * 生成签名,过期时间为30min
     */
    public static String sign(String username, String password){
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        Algorithm algorithm = Algorithm.HMAC256(SecureUtil.md5(password));
        // 带上用户名
        return JWT.create().withClaim("username", username).withExpiresAt(date).sign(algorithm);

    }


    /**
     * 从request 中的token  获取用户信息
     */
    public static String getUserNameByToken(HttpServletRequest request){
        String accessToken = request.getHeader("X-Access-Token");
        String username = getUsername(accessToken);
        if(StringUtils.isBlank(username)){
            throw new MicroCloudException("未获取到用户");
        }
        return username;
    }

    /**
     *  根据request中的token获取用户信息
     */
    public static LoginUser getLoginUser(){
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        if(request == null){
            log.warn(" 非request方式访问！! ");
            return null;
        }
        String accessToken = request.getHeader("X-Access-Token");
        if(StringUtils.isBlank(accessToken)){
            return null;
        }
        String username = getUsername(accessToken);
        if(StringUtils.isBlank(username)){
            throw new MicroCloudException("未获取到用户");
        }
        RedisUtils redisUtil = SpringContextUtils.getApplicationContext().getBean(RedisUtils.class);
        //  从缓存中获取
        LoginUser sysUser = (LoginUser) redisUtil.get(CacheConstant.SYS_USERS_CACHE_JWT +":" +username);
        return sysUser;
    }

    /**
     *  从session中获取变量
     */
    public static String getSessionData(String key){
        // 得到${} 后面的值
        String moshi = "";
        if(key.indexOf("}") != -1){
            moshi = key.substring(key.indexOf("}")+1);
        }
        String returnValue = null;

        if (key.contains("#{")) {
            key = key.substring(2,key.indexOf("}"));
        }
        if(StringUtils.isNotEmpty(key)){
            HttpSession session = SpringContextUtils.getHttpServletRequest().getSession();
            returnValue = (String) session.getAttribute(key);
        }
        if(returnValue!=null) returnValue = returnValue + moshi;
        return returnValue;
    }

    public static void main(String[] args) {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1NjUzMzY1MTMsInVzZXJuYW1lIjoiYWRtaW4ifQ." +
                "xjhud_tWCNYBOg_aRlMgOdlZoWFFKB_givNElHNw3X0";
        System.out.println(JwtUtil.getUsername(token));
    }

}
