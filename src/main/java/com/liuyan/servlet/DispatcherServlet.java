package com.liuyan.servlet;

import com.liuyan.annotation.Autowired;
import com.liuyan.annotation.LyController;
import com.liuyan.annotation.LyRequestMapping;
import com.liuyan.annotation.Service;
import com.liuyan.beans.LyHandlerAdapter;
import com.liuyan.beans.LyHandlerMapping;
import com.liuyan.beans.LyModelAndView;
import com.liuyan.context.LyApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liuyan
 * @date 2018/10/11 11:54
 */
public class DispatcherServlet extends HttpServlet {

    private Properties contexConfig=new Properties();

    private Map<String,Object> ioc=new  ConcurrentHashMap<String,Object>();

    private List<String> beanName=new ArrayList<>();

    private Map<String,Object> handlerMapping=new ConcurrentHashMap<>();

    private List<LyHandlerAdapter> handlerAdapters=new ArrayList<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url=url.replace(contextPath,"").replaceAll("/+","/");


        //对象要从ioc容器中获取
        doDispatcher(req,resp);


    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) {
      LyHandlerMapping handler= getHandler(req);
      LyHandlerAdapter ha=getHandlerAdapter(handler);
      LyModelAndView lyModelAndView=ha.handle(req,resp,handler);
      processDispatcherResult(resp,lyModelAndView);
    }

    private void processDispatcherResult(HttpServletResponse resp, LyModelAndView lyModelAndView) {
        //调用viewResolver的Resolveview方法
    }

    private LyHandlerAdapter getHandlerAdapter(LyHandlerMapping handler) {
        return null;
    }

    private LyHandlerMapping getHandler(HttpServletRequest req) {
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //版本1
//        //定位
//        doLocalConfig(config.getInitParameter("contextConfigLocation"));
//        //加载
//        doScanner(contexConfig.getProperty("scanPackage"));
//        //注册
//        doInstance();
//        //自动注入
//        doAutoWired();
//        //springmvc会多一个handlermapping
//        //以便于从浏览器获得用户输入的url以后，能够找到具体的method，通过反射去调用
//        initHanderMapping();

        LyApplicationContext lyApplicationContext = new LyApplicationContext(config.getInitParameter("contextConfigLocation"));
        initStrategies(lyApplicationContext);
    }

    private void initStrategies(LyApplicationContext lyApplicationContext) {
        initMultipartResolver(lyApplicationContext);
        initLocaleResolver(lyApplicationContext);
        initThemeResolver(lyApplicationContext);
        //用来保存controller配置的requestMapping和method的对应关系
        initHandlerMappings(lyApplicationContext);
        //动态匹配method参数，包括类转换，动态赋值
        initHandlerAdapters(lyApplicationContext);
        initHandlerExceptionResolvers(lyApplicationContext);
        initRequestToViewNameTranslator(lyApplicationContext);
        //实现动态模板的解析自己解析一套模板语言
        initViewResolvers(lyApplicationContext);
        initFlashMapManager(lyApplicationContext);
    }



    private void initViewResolvers(LyApplicationContext lyApplicationContext) {

    }

    private void initHandlerAdapters(LyApplicationContext lyApplicationContext) {

    }

    private void initHandlerMappings(LyApplicationContext lyApplicationContext) {
        //
    }

    private void initLocaleResolver(LyApplicationContext lyApplicationContext) {
    }
    private void initMultipartResolver(LyApplicationContext lyApplicationContext) {
    }
    private void initFlashMapManager(LyApplicationContext lyApplicationContext) {
    }
    private void initThemeResolver(LyApplicationContext lyApplicationContext) {
    }
    private void initHandlerExceptionResolvers(LyApplicationContext lyApplicationContext) {
    }
    private void initRequestToViewNameTranslator(LyApplicationContext lyApplicationContext) {
    }
    private void doAutoWired() {
        if(ioc.isEmpty()){
            return ;
        }

        for (Map.Entry<String,Object> entry:ioc.entrySet()){
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (Field field:declaredFields){
                if(!field.isAnnotationPresent(Autowired.class)){
                    continue;
                }
                Autowired autowired = field.getAnnotation(Autowired.class);
                String name = autowired.value().trim();
                if("".equals(name)){
                    name=field.getType().getSimpleName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),ioc.get(name));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    private void doScanner(String packageName) {
        URL resource = this.getClass().getClassLoader().getResource("/"+packageName.replace(".","/"));
        File classDIr=new File(resource.getFile());
        for (File file:classDIr.listFiles()){
            if(file.isDirectory()){
                doScanner(packageName+"."+file.getName());
            }else{
                beanName.add(packageName+"."+file.getName().replace(".class",""));
            }
        }
    }
    private void doInstance() {
        if(beanName.isEmpty()){
            return;
        }
        try {
            for (String classname :beanName){
                Class<?> aClass = Class.forName(classname);
                if (aClass.isAnnotationPresent(LyController.class)){
                    String name=lowerFirestCase(aClass.getSimpleName());
                    //String 不会直接putinstance，而是beandefinition
                    ioc.put(name,aClass.newInstance());
                }else if(aClass.isAnnotationPresent(Service.class)){
                    Service annotation = aClass.getAnnotation(Service.class);
                    //默认首字母注入
                    //如果自定义了bean，优先使用自定义
                    //如果是借口，使用接口的类型去自动注入
                    //在spring中同样会调用不同的方法
                    String name= annotation.value();
                    if("".equals(name.trim())){
                        name=lowerFirestCase(aClass.getSimpleName());
                    }
                    Object instance=aClass.newInstance();
                    ioc.put(name,instance);
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (Class<?> i:interfaces){
                        ioc.put(i.getSimpleName(),instance);
                    }
                }else {
                    continue;
                }

            }
        }catch (Exception e){

        }
    }
    private void doLocalConfig(String location) {
        //spring中通过reader去查找定位的
        String path=location.replace("classpath:","");
        InputStream is=this.getClass().getClassLoader().getResourceAsStream(path);
        try {
            contexConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null!=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void initHanderMapping() {
        if(ioc.isEmpty()){
            return ;
        }
        for (Map.Entry<String,Object> entry:ioc.entrySet()){
            Class<?> aClass = entry.getValue().getClass();
            if (!aClass.isAnnotationPresent(LyController.class)){
                continue;
            }
            String baseUlr=null;
            if(aClass.isAnnotationPresent(LyRequestMapping.class)){
                LyRequestMapping requestMapping = aClass.getAnnotation(LyRequestMapping.class);

                baseUlr=requestMapping.value();
            }

            Method[] methods = aClass.getMethods();
            for (Method method :methods){
                if(!method.isAnnotationPresent(LyRequestMapping.class)){
                    continue;
                }
                LyRequestMapping annotation = method.getAnnotation(LyRequestMapping.class);
                String url=baseUlr+"/"+annotation.value().replaceAll("/+","/");
                handlerMapping.put(url,method);

            }
        }
    }


    public static void main(String[] args) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        try {
            dispatcherServlet.init();
            System.out.println(dispatcherServlet);
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    private String lowerFirestCase(String str){
        char[] chars = str.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }
}
