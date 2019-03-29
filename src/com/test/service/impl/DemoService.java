package com.test.service.impl;

import com.myspringframework.core.annotation.MyService;
import com.test.service.IDemoService;
@MyService
public class DemoService implements IDemoService {

	public String get(String name) {
		return "My name is:" + name;
	}
	public String add(String name) {
		return "Add name:" + name;
	}
}
