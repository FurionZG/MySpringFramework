# 手写SpringMVC
   ## 概述：
   本项目是为了探究SpringMVC底层原理，在学习之余自己实现了一个乞丐版SpringMVC。
   ## 功能实现：
   1.项目紧依赖于log4j日志包<br/>
   2.实现了RequestMapping、Controller、Service、Autowired注解。<br/>
   3.在配置文件中配置扫描包路径。<br/>
   4.实现了url到method的映射。<br/>
   5.对不存在页面的404处理，页面异常的500处理<br/>
   ## 功能展示：
   ### 这是项目结构：
   
<div align=center><img width="50%" height="50%" src="imgs/QQ截图20190323195659.png"/></div>

   ### web.xml：
   
<div align=center><img width="100%" height="100%" src="imgs/QQ截图20190329140948.png"/></div>

   ### 访问http://localhost/MySpringFramework/demo/query?name=123：
   
<div align=center><img width="100%" height="100%" src="imgs/QQ截图20190329141748.png"/></div>

   ### 后台日志输出：
   
<div align=center><img width="100%" height="100%" src="imgs/QQ截图20190329141007.png"/></div>

 
