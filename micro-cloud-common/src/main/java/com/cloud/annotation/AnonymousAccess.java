package com.cloud.annotation;

import java.lang.annotation.*;

/**
 * 自定义匿名注解：使用该注解的接口 不进行拦截
 */

@Inherited
@Documented
@Target({ElementType.METHOD,ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnonymousAccess {
}
