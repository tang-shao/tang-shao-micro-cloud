package com.cloud.modules.system.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloud.annotation.AnonymousAccess;
import com.cloud.annotation.AutoLog;
import com.cloud.common.api.Result;
import com.cloud.common.constant.CacheConstant;
import com.cloud.common.constant.CommonConstant;
import com.cloud.modules.shiro.vo.DefContants;
import com.cloud.modules.system.entity.SysUser;
import com.cloud.modules.system.model.SysLoginModel;
import com.cloud.modules.system.service.ISysUserService;
import com.cloud.modules.system.entity.LoginUser;
import com.cloud.modules.utils.JwtUtil;
import com.cloud.modules.utils.RedisUtils;
import com.cloud.modules.utils.StringUtils;
import com.cloud.modules.utils.encryption.AesEncryptUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("sys")
@Api(tags = "用户登录")
public class LoginController {

    /**
     * 验证码key  用于生成UUID
     */
    @Value("${jwt.code-key}")
    private String codeKey;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ISysUserService userService;

    private static final String BASE_CHECK_CODES = "qwertyuiplkjhgfdsazxcvbnmQWERTYUPLKJHGFDSAZXCVBNM1234567890";

    /**
     * 登录接口
     */
    @PostMapping("/login")
    @ApiOperation("登录接口")
    public Result<JSONObject> login(@RequestBody @Valid SysLoginModel sysLoginModel){
        Result<JSONObject> res = new Result<>();
        String username = sysLoginModel.getUsername();
        String password = sysLoginModel.getPassword();
        // 前端对密码进行加密 后端进行解密(先忽略)
        String captcha = sysLoginModel.getCaptcha();
        String checkCode = (String) redisUtils.get(sysLoginModel.getUuid());
        // 清除验证码, 保证一个请求 只有一个验证码
        redisUtils.del(sysLoginModel.getUuid());
        // Assert.isNull(checkCode,"验证码不存在或者已过期");
        if(StringUtils.isBlank(checkCode)){
            res.error500("验证码不存在或者已过期");
            return res;
        }
        // 忽略大小写
        if(!captcha.equalsIgnoreCase(checkCode)){
            res.error500("验证码错误");
            return res;
        }
        // 检查账户是否有效
        SysUser sysUser = userService.getUserByName(username);
        Result result = userService.checkUserIsEffective(sysUser);
        if(!result.isSuccess()){
            return result;
        }

        // 检查账户 & 密码是否正确
        String encryptPassword = "";
        try {
            encryptPassword = AesEncryptUtil.encrypt(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // String encryptPassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
        if(!encryptPassword.equals(sysUser.getPassword())){
            res.error500("用户名或者密码错误");
            return res;
        }
        userInfo(sysUser,res);
        return res;

    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    @ApiOperation("退出登录")
    public Result<Object> logout(HttpServletRequest request, HttpServletResponse response){
        // 获取到token
        String token = request.getHeader(DefContants.X_ACCESS_TOKEN);
        if(StringUtils.isBlank(token)){
            return Result.error("退出失败");
        }
        // 获取到token中的用户名
        String username = JwtUtil.getUsername(token);
        SysUser user = userService.getUserByName(username);
        if(user != null){
            // 清空当前登录人token
            redisUtils.del(CommonConstant.PREFIX_USER_TOKEN + token);
            redisUtils.del(CacheConstant.SYS_USERS_CACHE + ":" + token);
            // 调用shiro退出
            SecurityUtils.getSubject().logout();
            return Result.error("退出成功");
        }else {
            return Result.error("Token 无效 退出失败");
        }
    }



    /**
     * 后台生成图像验证码  ResponseEntity
     */
    @ApiOperation("获取验证码")
    @GetMapping("/randomImage")
    public Result<Map<String,Object>> randomImage(HttpServletRequest request, HttpServletResponse response){
        Result res = new Result();
        try {
            String code = RandomUtil.randomString(BASE_CHECK_CODES,4);
            // 讲字符串转小写
            String lowerCaseCode = code.toLowerCase();
            // 使用 uuid 作为验证码的唯一标识
            String uuid = codeKey + IdUtil.simpleUUID();
            // String realKey = MD5Util.MD5Encode(lowerCaseCode + key, "utf-8");
            // 验证码60S过期
            redisUtils.set(uuid, lowerCaseCode, 60);

            HashMap<String, Object> map = new HashMap<String, Object>(2) {{
                put("img", code);
                put("uuid",uuid);
            }};
            res.setSuccess(true);
            res.setResult(map);
        }catch (Exception e){
            e.printStackTrace();
            res.error500("获取验证码错误" + e.getMessage());
        }
        return res;
    }


    /**
     * 用户信息 以及Token生成
     */
    private Result<JSONObject> userInfo(SysUser sysUser, Result<JSONObject> res){
        // 根据用户名密码生成Token
        String token = JwtUtil.sign(sysUser.getUsername(), sysUser.getPassword());
        // 设置Token缓存有效时间
        redisUtils.set(CommonConstant.PREFIX_USER_TOKEN + token,token);
        redisUtils.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);

        // 缓存当前在线用户
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(sysUser,loginUser);
        redisUtils.set(CacheConstant.SYS_USERS_CACHE + ":" + token,loginUser);
        redisUtils.expire(CacheConstant.SYS_USERS_CACHE + ":" + token, JwtUtil.EXPIRE_TIME * 2 / 1000);

        JSONObject obj = new JSONObject();
        obj.put("token", token);
        obj.put("userInfo", loginUser);

        res.setResult(obj);
        res.setMessage("登录成功");
        return res;

    }

    @AnonymousAccess
    @GetMapping("/add")
    @ApiOperation("测试匿名访问 访问接口")
    public Result<?> testAnonymousAccess(){
        Result<Object> res = new Result<>();
        res.setSuccess(true);
        res.setResult("测试匿名访问 访问接口");
        return res;
    }
    @GetMapping("/test111")
    @ApiOperation("sdfdafdaf")
    public Result<?> testRedis(HttpServletRequest request){
        Result<Object> res = new Result<>();
        String token = request.getHeader(DefContants.X_ACCESS_TOKEN);
        return res;
    }


}
