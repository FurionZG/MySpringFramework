package com.myspringframework.core.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.myspringframework.core.annotation.MyAutowired;
import com.myspringframework.core.annotation.MyController;
import com.myspringframework.core.annotation.MyRequestMapping;
import com.myspringframework.core.annotation.MyService;

public class MyDispatcherServlet extends HttpServlet {
	// 初始化配置文件
	private Properties contextConfig = new Properties();
	// 保存所有实例化的类的类名
	private List<String> classList = new ArrayList<String>();
	// 初始化IOC容器，以类名的首字母小写为键，类的实例为值
	private Map<String, Object> ioc = new HashMap<String, Object>();
	// 初始化HandlerMapping
	private Map<String, Method> handlerMapping = new HashMap<String, Method>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 6.调用Get/Post方法，反射调用，将结果发送到浏览器
		try {
			doDispatch(req, resp);
		} catch (Exception e) {
			resp.getWriter().write("500 Exception:" + Arrays.toString(e.getStackTrace()));
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 6.调用Get/Post方法，反射调用，将结果发送到浏览器
		try {
			doDispatch(req, resp);
		} catch (Exception e) {
			resp.getWriter().write("500 Exception:" + Arrays.toString(e.getStackTrace()));
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		// 1.加载配置文件
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		// 2.解析配置文件，设置扫描类路径，扫描所有相关的类
		doScan(contextConfig.getProperty("scanPackege"));
		// 3.初始化所有相关的类，并且保存在IOC容器中
		doInstance();
		// 4.完成依赖注入 DI
		doAutowired();
		// 5.创建HandlerMapping，将Url和Method建立对应关系
		initHandlerMapping();

		System.out.println("MySpringMVC is init");

	}

	/**
	 * 将Url分发到HandlerMapping中映射的方法
	 * 
	 * @param req
	 * @param resp
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private void doDispatch(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (handlerMapping.isEmpty()) {
			return;
		}
		// 绝对路径
		String url = req.getRequestURI();
		// web目录根路径
		String contextPath = req.getContextPath();
		// 拿到相对路径
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		if (!this.handlerMapping.containsKey(url)) {
			resp.getWriter().write("404 Not Found!");
			return;
		}
		Method method = this.handlerMapping.get(url);

		Map<String, String[]> params = req.getParameterMap();
		String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());
		method.invoke(ioc.get(beanName), new Object[] { req, resp, params.get("name")[0] });

	}

	/**
	 * 初始化HandlerMapping方法
	 */

	private void initHandlerMapping() {
		if (ioc.isEmpty()) {
			return;
		}
		for (Map.Entry<String, Object> entry : ioc.entrySet()) {
			Class<?> clz = entry.getValue().getClass();
			if (!clz.isAnnotationPresent(MyController.class)) {
				continue;
			}

			String baseUrl = "";
			if (clz.isAnnotationPresent(MyRequestMapping.class)) {
				MyRequestMapping requestMapping = clz.getAnnotation(MyRequestMapping.class);
				baseUrl = requestMapping.value();
			}
			// 这里不用暴力访问，因为在Spring中也不能调用私有的方法
			Method[] methods = clz.getMethods();
			for (Method method : methods) {
				if (!method.isAnnotationPresent(MyRequestMapping.class)) {
					continue;
				}
				MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
				String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
				handlerMapping.put(url, method);
				System.out.println("Mapped:" + url + ",Method:" + method);
			}

		}
	}

	/**
	 * 自动注入方法
	 */
	private void doAutowired() {
		if (ioc.isEmpty()) {
			return;
		}
		for (Map.Entry<String, Object> entry : ioc.entrySet()) {
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				// 自动注入核心代码，将IOC容器中的实例注入到对应的字段中。
				if (!field.isAnnotationPresent(MyAutowired.class)) {
					continue;
				}
				MyAutowired autowired = field.getAnnotation(MyAutowired.class);
				String beanName = autowired.value();
				if ("".equals(beanName)) {
					beanName = field.getType().getName();
				}
				// 暴力访问
				field.setAccessible(true);

				try {
					field.set(entry.getValue(), ioc.get(beanName));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
	}

	/**
	 * 实例化类方法
	 */
	private void doInstance() {
		if (classList.isEmpty()) {
			return;
		}

		try {
			for (String className : classList) {
				Class<?> clz = Class.forName(className);

				// 只加载有注解的类
				// 注入Controller
				if (clz.isAnnotationPresent(MyController.class)) {
					// 简单类名，不包含包名
					String beanName = lowerFirstCase(clz.getSimpleName());
					// 存放到IOC容器中去
					ioc.put(beanName, clz.newInstance());
					// 注入Service，要将实现类注入给接口
				} else if (clz.isAnnotationPresent(MyService.class)) {

					// 1.使用自定义命名
					// 2.类名首字母小写方式--默认方式
					MyService service = clz.getAnnotation(MyService.class);
					// 实例名为自定义的值
					String beanName = service.value();
					// 没有自定义实例名的情况
					if ("".equals(beanName.trim())) {
						beanName = lowerFirstCase(clz.getSimpleName());
					}
					Object instance = clz.newInstance();
					ioc.put(beanName, instance);
					// 3.用接口的全称(带包名)作为Key，用接口的实现类的实例作为值
					Class<?>[] interfaces = clz.getInterfaces();
					for (Class<?> i : interfaces) {
						if (ioc.containsKey(i.getName())) {
							throw new Exception("This beanName is exists:" + i.getName());
						}
						ioc.put(i.getName(), instance);
					}

				} else {
					continue;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 首字母小写方法
	 * 
	 * @param simpleName
	 * @return
	 */
	private String lowerFirstCase(String simpleName) {
		char[] chars = simpleName.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}

	/**
	 * 扫描包方法
	 * 
	 * @param scanPackage
	 */
	private void doScan(String scanPackage) {
		URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
		File classDir = new File(url.getFile());
		for (File file : classDir.listFiles()) {
			if (file.isDirectory()) {
				doScan(scanPackage + "." + file.getName());
			} else {
				if (!file.getName().endsWith(".class")) {
					continue;
				}
				String className = scanPackage + "." + file.getName().replace(".class", "").trim();
				classList.add(className);
			}
		}
	}

	/**
	 * 加载配置文件方法
	 * 
	 * @param contextConfigLocation
	 */
	private void doLoadConfig(String contextConfigLocation) {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
		try {
			contextConfig.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
