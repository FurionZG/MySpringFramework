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
	// ��ʼ�������ļ�
	private Properties contextConfig = new Properties();
	// ��������ʵ�������������
	private List<String> classList = new ArrayList<String>();
	// ��ʼ��IOC������������������ĸСдΪ�������ʵ��Ϊֵ
	private Map<String, Object> ioc = new HashMap<String, Object>();
	// ��ʼ��HandlerMapping
	private Map<String, Method> handlerMapping = new HashMap<String, Method>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 6.����Get/Post������������ã���������͵������
		try {
			doDispatch(req, resp);
		} catch (Exception e) {
			resp.getWriter().write("500 Exception:" + Arrays.toString(e.getStackTrace()));
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 6.����Get/Post������������ã���������͵������
		try {
			doDispatch(req, resp);
		} catch (Exception e) {
			resp.getWriter().write("500 Exception:" + Arrays.toString(e.getStackTrace()));
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		// 1.���������ļ�
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		// 2.���������ļ�������ɨ����·����ɨ��������ص���
		doScan(contextConfig.getProperty("scanPackege"));
		// 3.��ʼ��������ص��࣬���ұ�����IOC������
		doInstance();
		// 4.�������ע�� DI
		doAutowired();
		// 5.����HandlerMapping����Url��Method������Ӧ��ϵ
		initHandlerMapping();

		System.out.println("MySpringMVC is init");

	}

	/**
	 * ��Url�ַ���HandlerMapping��ӳ��ķ���
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
		// ����·��
		String url = req.getRequestURI();
		// webĿ¼��·��
		String contextPath = req.getContextPath();
		// �õ����·��
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
	 * ��ʼ��HandlerMapping����
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
			// ���ﲻ�ñ������ʣ���Ϊ��Spring��Ҳ���ܵ���˽�еķ���
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
	 * �Զ�ע�뷽��
	 */
	private void doAutowired() {
		if (ioc.isEmpty()) {
			return;
		}
		for (Map.Entry<String, Object> entry : ioc.entrySet()) {
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				// �Զ�ע����Ĵ��룬��IOC�����е�ʵ��ע�뵽��Ӧ���ֶ��С�
				if (!field.isAnnotationPresent(MyAutowired.class)) {
					continue;
				}
				MyAutowired autowired = field.getAnnotation(MyAutowired.class);
				String beanName = autowired.value();
				if ("".equals(beanName)) {
					beanName = field.getType().getName();
				}
				// ��������
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
	 * ʵ�����෽��
	 */
	private void doInstance() {
		if (classList.isEmpty()) {
			return;
		}

		try {
			for (String className : classList) {
				Class<?> clz = Class.forName(className);

				// ֻ������ע�����
				// ע��Controller
				if (clz.isAnnotationPresent(MyController.class)) {
					// ������������������
					String beanName = lowerFirstCase(clz.getSimpleName());
					// ��ŵ�IOC������ȥ
					ioc.put(beanName, clz.newInstance());
					// ע��Service��Ҫ��ʵ����ע����ӿ�
				} else if (clz.isAnnotationPresent(MyService.class)) {

					// 1.ʹ���Զ�������
					// 2.��������ĸСд��ʽ--Ĭ�Ϸ�ʽ
					MyService service = clz.getAnnotation(MyService.class);
					// ʵ����Ϊ�Զ����ֵ
					String beanName = service.value();
					// û���Զ���ʵ���������
					if ("".equals(beanName.trim())) {
						beanName = lowerFirstCase(clz.getSimpleName());
					}
					Object instance = clz.newInstance();
					ioc.put(beanName, instance);
					// 3.�ýӿڵ�ȫ��(������)��ΪKey���ýӿڵ�ʵ�����ʵ����Ϊֵ
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
	 * ����ĸСд����
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
	 * ɨ�������
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
	 * ���������ļ�����
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
