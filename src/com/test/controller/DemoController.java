package com.test.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.myspringframework.core.annotation.MyAutowired;
import com.myspringframework.core.annotation.MyController;
import com.myspringframework.core.annotation.MyRequestMapping;
import com.myspringframework.core.annotation.MyRequestParam;
import com.test.service.IDemoService;

@MyController
@MyRequestMapping("/demo")
public class DemoController {
	@MyAutowired
	private IDemoService demoService;
	@MyRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("name") String name) {
		String result = demoService.get(name);
		try {
			resp.getWriter().write(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@MyRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("name") String name) {
		String result = demoService.add(name);
		try {
			resp.getWriter().write(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
