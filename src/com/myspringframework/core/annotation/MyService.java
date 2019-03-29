package com.myspringframework.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//����ע������������ӿ���
@Target(ElementType.TYPE)
//����ע�������������ʱ������
@Retention(RetentionPolicy.RUNTIME)
//����ע��Ϊ�ɼ���
@Documented
/**
 * Serviceע��
 * @author ��ү
 *
 */
public @interface MyService {
	String value() default "";
}
