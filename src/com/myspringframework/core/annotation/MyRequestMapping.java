package com.myspringframework.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//声明注解的作用域：方法、类或接口上
@Target({ElementType.TYPE,ElementType.METHOD})
//声明注解的作用域：运行时起作用
@Retention(RetentionPolicy.RUNTIME)
//声明注解为可见的
@Documented
/**
 * RequestMapping注解
 * @author 四爷
 *
 */
public @interface MyRequestMapping {
	String value() default "";
}
