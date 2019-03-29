package com.myspringframework.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//����ע����������ֶ���
@Target(ElementType.FIELD)
//����ע�������������ʱ������
@Retention(RetentionPolicy.RUNTIME)
//����ע��Ϊ�ɼ���
@Documented
/**
 * �Զ�ע��ע��
 * @author ��ү
 *
 */
public @interface MyAutowired {
	String value() default "";
}
