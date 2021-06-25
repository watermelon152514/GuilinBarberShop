package guet.edu.cn.mvc;

import guet.edu.cn.mvc.annotation.Controller;
import guet.edu.cn.mvc.annotation.RequestMapping;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Configuration {
    public Map<String, ControllerMapping> config() throws URISyntaxException {
        Map<String, ControllerMapping> controllerMapping = new HashMap<String, ControllerMapping>();
        try {

            /*
            读取config.properties文件，获取到包名称
             */
            ResourceBundle bundle = ResourceBundle.getBundle("config");
            String controllerPackageName = bundle.getString("controller.package");
            System.out.println(controllerPackageName);

            /*
            根据包名称构建要扫描的完整路径
             */
            String path = controllerPackageName.replace(".", "/");
            URI uri = Configuration.class.getResource("/" + path).toURI();
            System.out.println(uri);
            File controllerDirectory = new File(uri);
            String controllerFileNames[] = controllerDirectory.list();
            for (String controllerFileName : controllerFileNames) {
                //全限定类名=包名 + 类名
                String controllerClassName = controllerPackageName + "." + StringUtils.substringBefore(controllerFileName, ".class");
                //动态加载类
                Class<?> controllerClass = Class.forName(controllerClassName);
                //判断哪些类上使用了Controller注解
                if (controllerClass.isAnnotationPresent(Controller.class)) {
//                    继续找出哪些方法上使用了@RequestMapping注解
                    Method[] handleMethods = MethodUtils.getMethodsWithAnnotation(controllerClass, RequestMapping.class);
                    for (Method handleMethod : handleMethods) {
                        RequestMapping annotation = handleMethod.getAnnotation(RequestMapping.class);
                        ControllerMapping mapping = new ControllerMapping(controllerClass, handleMethod);
                        System.out.print("类：" + controllerClass.getSimpleName());
                        System.out.print("\t方法：" + handleMethod.getName());
                        System.out.println("\t注解的值：" + annotation.value());
                        controllerMapping.put(annotation.value(), mapping);

                        /*
                        拿到注解的值（url）以后，提前把url存入mapping中，将来前端控制器(DispatcherServlet收到请求后，把客户端的url去mappping中查找相应的url）
                        如果匹配到了url那么会自动去调用标记了url的那个方法
                         */
                    }
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return controllerMapping;
    }
}
