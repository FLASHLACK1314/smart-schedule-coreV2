package io.github.flashlack1314.smartschedulecorev2.annotation;

import io.github.flashlack1314.smartschedulecorev2.enums.UserType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解
 * 用于标记Controller类或方法，指定允许访问的用户角色
 *
 * @author flash
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 允许访问的用户角色
     * 支持多个角色
     *
     * @return 允许的角色列表
     */
    UserType[] value();
}
