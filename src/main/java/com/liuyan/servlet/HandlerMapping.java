package com.liuyan.servlet;

import com.liuyan.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author liuyan
 * @date 2019/6/10 15:46
 */
public class HandlerMapping {

    protected Object controller;	//保存方法对应的实例
    protected Method method;		//保存映射的方法
    protected String  url;
    protected Map<String,Integer> paramIndexMapping;	//参数顺序

    /**
     * 构造一个Handler基本的参数
     * @param controller
     * @param method
     */
    protected HandlerMapping(String  url,Object controller,Method method){
        this.controller = controller;
        this.method = method;
        this.url = url;

        paramIndexMapping = new HashMap<String,Integer>();
        putParamIndexMapping(method);
    }

    private void putParamIndexMapping(Method method){

        //提取方法中加了注解的参数
        Annotation[] [] pa = method.getParameterAnnotations();
        for (int i = 0; i < pa.length ; i ++) {
            for(Annotation a : pa[i]){
                if(a instanceof RequestParam){
                    String paramName = ((RequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        //提取方法中的request和response参数
        Class<?> [] paramsTypes = method.getParameterTypes();
        for (int i = 0; i < paramsTypes.length ; i ++) {
            Class<?> type = paramsTypes[i];
            if(type == HttpServletRequest.class ||
                    type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(),i);
            }
        }
    }


}
