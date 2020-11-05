package com.cloud.modules.system.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloud.annotation.AnonymousAccess;
import com.cloud.bo.SysUserDetails;
import com.cloud.common.api.Result;
import com.cloud.common.constant.CacheConstant;
import com.cloud.common.constant.CommonConstant;
import com.cloud.config.JwtConfig;
import com.cloud.modules.system.service.ISysUserService;
import com.cloud.security.TokenProvider;
import com.cloud.modules.system.entity.LoginUser;
import com.cloud.modules.system.entity.SysUser;
import com.cloud.modules.system.model.SysLoginModel;
import com.cloud.modules.utils.JwtUtil;
import com.cloud.modules.utils.RedisUtils;
import com.cloud.modules.utils.StringUtils;
import com.cloud.modules.utils.encryption.AesEncryptUtil;
import com.cloud.util.JwtTokenUtil;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("sys")
@Api(tags = "Spring Security 登录")
public class LoginController {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;



    @AnonymousAccess
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<JSONObject> login(@RequestBody @Valid SysLoginModel sysLoginModel){
        Result<JSONObject> res = new Result<>();

        String username = sysLoginModel.getUsername();
        String password = sysLoginModel.getPassword();
        
        // 获取到验证码
        String checkCode = (String) redisUtils.get(sysLoginModel.getUuid());

        // 清除验证码
        redisUtils.del(sysLoginModel.getUuid());

        if(StringUtils.isBlank(checkCode)){
            res.error500("验证码不存在或已过期");
            return res;
        }

        if(StringUtils.isBlank(sysLoginModel.getCaptcha()) || !sysLoginModel.getCaptcha().equalsIgnoreCase(checkCode)){
            res.error500("验证码错误");
            return res;
        }

        SysUser user = sysUserService.getUserByName(username);


        // 密码验证
        String encryptPassword = "";
        try {
            // 密码加密
            encryptPassword = AesEncryptUtil.encrypt(password);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!encryptPassword.equals(user.getPassword())){
            res.error500("用户名或者密码错误");
            return res;
        }

        // 保存在线用户
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(user,loginUser);
        SysUserDetails userDetails = new SysUserDetails(loginUser);

        UsernamePasswordAuthenticationToken authentication = new
                UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 生成token
        String token = jwtTokenUtil.generateToken(userDetails);

        // 设置Token缓存有效时间
        redisUtils.set(CommonConstant.PREFIX_USER_TOKEN + token,token);
        redisUtils.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);

        // 保存在线用户

        redisUtils.set(CacheConstant.SYS_USERS_CACHE + ":" + token,loginUser);
        redisUtils.expire(CacheConstant.SYS_USERS_CACHE + ":" + token, JwtUtil.EXPIRE_TIME * 2 / 1000);

        JSONObject obj = new JSONObject();
        obj.put("token", token);
        obj.put("userInfo", loginUser);
        res.setSuccess(true);
        res.setResult(obj);
        return res;
    }


    /**
     * 获取验证码(EasyCaptcha)
     *    请参考：https://gitee.com/whvse/EasyCaptcha
     */
    @AnonymousAccess
    @GetMapping("/getImageCode")
    @ApiOperation("获取图像验证码")
    public Result<Map<String,Object>> getImageCode(){
        Result res = new Result();
        // 验证码为算数类型
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(111, 36);
        // 设置几位数运算  默认是两位
        captcha.setLen(2);
        // 获取到运算结果
        String result = captcha.text();
        System.out.println("-------------验证码----------------" + result);
        // 生成验证码唯一标识
        String uuid = JwtConfig.CODEKEY + IdUtil.simpleUUID();
        // 保存验证码(1分钟)
        redisUtils.set(uuid,result,1L);
        Map<String,Object> map =  new HashMap<String, Object>(2){{
            put("uuid",uuid);
            put("img",captcha.toBase64());
        }};
        res.setResult(map);
        return res;
    }

    @PostMapping("/test1")
    // @AnonymousAccess
    @ApiOperation("测试 Spring Security权限")
    public Result<?> testSecurity(){
        return Result.ok("hello  world.................");
    }


}
