package com.myspringframework.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//����ע����������ֶ���
@Target(ElementType.PARAMETER)
//����ע�������������ʱ������
@Retention(RetentionPolicy.RUNTIME)
//����ע��Ϊ�ɼ���
@Documented
/**
 * RequestParamע��
 * @author ��ү
 *
 */
public @interface MyRequestParam {
	String value() default "";
}
