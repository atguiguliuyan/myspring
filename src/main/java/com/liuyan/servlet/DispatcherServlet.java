package com.liuyan.servlet;

import com.liuyan.annotation.*;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author liuyan
 * @date 2018/10/11 11:54
 */
public class DispatcherServlet extends HttpServlet {

    private Properties contexConfig=new Properties();

    private Map<String,Object> ioc=new  ConcurrentHashMap<String,Object>();

    private List<String> beanName=new ArrayList<>();

    private List<HandlerMapping> handlerMapping=new ArrayList<>();

    private List<LyHandlerAdapter> handlerAdapters=new ArrayList<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispatcher(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500"+e.getMessage());
        }

        return;


    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws  Exception {
        HandlerMapping handler = getHandler(req);
        if(handler == null){
            //如果没有匹配上，返回404错误
            resp.getWriter().write("404 Not Found");
            return;
        }


        //获取方法的参数列表
        Class<?> [] paramTypes = handler.method.getParameterTypes();

        //保存所有需要自动赋值的参数值
        Object [] paramValues = new Object[paramTypes.length];


        Map<String,String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");

            //如果找到匹配的对象，则开始填充参数值
            if(!handler.paramIndexMapping.containsKey(param.getKey())){continue;}
            int index = handler.paramIndexMapping.get(param.getKey());
            paramValues[index] = convert(paramTypes[index],value);
        }


        //设置方法中的request和response对象
        int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
        paramValues[reqIndex] = req;
        int respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
        paramValues[respIndex] = resp;

        handler.method.invoke(handler.controller, paramValues);
    }

    private void processDispatcherResult(HttpServletResponse resp, LyModelAndView lyModelAndView) {
        //调用viewResolver的Resolveview方法
    }

    private LyHandlerAdapter getHandlerAdapter(LyHandlerMapping handler) {
        return null;
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        //版本1
        //定位
        doLocalConfig(config.getInitParameter("contextConfigLocation"));
        //加载
        doScanner(contexConfig.getProperty("scanPackage"));
        //注册
        doInstance();
        //自动注入
        doAutoWired();
        //springmvc会多一个handlermapping
        //以便于从浏览器获得用户输入的url以后，能够找到具体的method，通过反射去调用
        initHanderMapping();

        System.out.println("完成");


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
                String url=baseUlr+annotation.value();
                handlerMapping.add(new HandlerMapping(url,entry.getValue(),method));

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

    private Object convert(Class<?> type,String value){
        if(Integer.class==type){
            return Integer.valueOf(value);
        }
        return value;
    }
    private HandlerMapping getHandler(HttpServletRequest req) throws Exception{
        if(handlerMapping.isEmpty()){ return null; }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (HandlerMapping handler : handlerMapping) {
            try{
                //如果没有匹配上继续下一个匹配
                if(!handler.url.equals(url)){ continue; }

                return handler;
            }catch(Exception e){
                throw e;
            }
        }
        return null;
    }


}
