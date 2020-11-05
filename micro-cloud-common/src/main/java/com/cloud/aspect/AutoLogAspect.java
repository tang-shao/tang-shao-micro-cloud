package com.cloud.aspect;

import com.alibaba.fastjson.JSONObject;
import com.cloud.annotation.AutoLog;
import com.cloud.common.constant.CacheConstant;
import com.cloud.common.constant.CommonConstant;
import com.cloud.feign.SysLogFeignClient;
import com.cloud.modules.system.entity.SysLog;
import com.cloud.modules.system.entity.LoginUser;
import com.cloud.modules.system.model.DefContants;
import com.cloud.modules.utils.IPUtils;
import com.cloud.modules.utils.RedisUtils;
import com.cloud.modules.utils.RequestHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;


/**
 * 系统日志 切面处理类
 */
@Slf4j
@Aspect
@Component
public class AutoLogAspect {

    @Autowired
    private SysLogFeignClient sysLogFeignClient;

    @Autowired
    private RedisUtils redisUtils;

    // 切点
    @Pointcut("@annotation(com.cloud.annotation.AutoLog)")
    public void logPointCut() {

    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable{
        System.out.println("apo ================================");
        long beginTime = System.currentTimeMillis();
        // 执行方法
        Object result = point.proceed();
        // 执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        // 保存日志
        saveSysLog(point, time);
        return result;
    }

    // 保存系统日志
    private void saveSysLog(ProceedingJoinPoint point, long time){
        MethodSignature signature = (MethodSignature) point.getSignature();
        // 获取注解标记的方法对象
        Method method = signature.getMethod();
        SysLog sysLog = new SysLog();
        // 获取到注解
        AutoLog syslog = method.getAnnotation(AutoLog.class);
        if(syslog != null){
            // 注解上的描述,操作日志内容
            sysLog.setLogContent(syslog.value());
            sysLog.setLogType(syslog.logType());
        }

        // 获取类的完全限定名
        String className = point.getTarget().getClass().getName();
        // 获取方法名
        String methodName = signature.getName();
        sysLog.setMethod(className + "." + methodName);
        // 设置日志操作类型
        if(sysLog.getLogType() == CommonConstant.LOG_TYPE_2){
            sysLog.setOperateType(getOperateType(methodName, syslog.operateType()));
        }

        HttpServletRequest request = RequestHolder.getHttpServletRequest();

        // 获取请求参数
        sysLog.setRequestParam(getReqestParams(request, point));

        //设置IP地址
        sysLog.setIp(IPUtils.getIpAddr(request));
        String token = request.getHeader(DefContants.X_ACCESS_TOKEN);
        // 获取登录用户信息
        LoginUser sysUser = (LoginUser)redisUtils.get(CacheConstant.SYS_USERS_CACHE + ":" + token);
        // LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        if(sysUser!=null){
            sysLog.setUserid(sysUser.getUsername());
            sysLog.setUsername(sysUser.getRealname());
        }
        //耗时
        sysLog.setCostTime(time);
        sysLog.setCreateTime(new Date());

        // 保存日志
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(sysLog);
        sysLogFeignClient.saveSysLog(jsonObject);



    }

    // 获取到操作类型
    private int getOperateType(String methodName,int operateType){
        if(operateType > 0){
            return operateType;
        }
        if (methodName.startsWith("list")) {
            return CommonConstant.OPERATE_TYPE_1;
        }
        if (methodName.startsWith("add")) {
            return CommonConstant.OPERATE_TYPE_2;
        }
        if (methodName.startsWith("edit")) {
            return CommonConstant.OPERATE_TYPE_3;
        }
        if (methodName.startsWith("delete")) {
            return CommonConstant.OPERATE_TYPE_4;
        }
        if (methodName.startsWith("import")) {
            return CommonConstant.OPERATE_TYPE_5;
        }
        if (methodName.startsWith("export")) {
            return CommonConstant.OPERATE_TYPE_6;
        }
        return CommonConstant.OPERATE_TYPE_1;
    }

    /**
     * 获取请求中的参数
     */
    private String getReqestParams(HttpServletRequest request, JoinPoint joinPoint){
        // 获取到请求的类型(POST请求  GET请求,PUT,PATCH)
        String httpMethod = request.getMethod();
        String params = "";
        if("POST".equals(httpMethod) || "PUT".equals(httpMethod) || "PATCH".equals(httpMethod)){
            // 得到请求方法参数值
            Object[] paramsArray = joinPoint.getArgs();
            // 把请求参数转换成JSON字符串
            params = JSONObject.toJSONString(paramsArray);
        }else {
            // 如果是除 POST,PUT,PATCH 请求
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] paramArgs = joinPoint.getArgs();
            // 请求参数方法名称
            LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
            String[] paramNames = u.getParameterNames(method);
            if (paramArgs != null && paramNames != null) {
                for (int i = 0; i < paramArgs.length; i++) {
                    params += "  " + paramNames[i] + ": " + paramArgs[i];
                }
            }
        }
        return params;

    }

}
