package com.liuyan.beans;

import com.liuyan.core.FactoryBean;

/**
 * @author zhaoxiuhuan
 * @date 2018/10/16 15:46
 */
public class BeanWrapper extends FactoryBean {
    //还会用到观察者模式
    //支持时间响应会有一个监听
    private BeanPostProcessor beanPostProcessor;

    private Object wrapperInstance;

    private Object originalInstance;

    public BeanWrapper(Object instance) {
        this.wrapperInstance = instance;
        this.originalInstance=instance;
    }

    public Object  getWrappedInstance(){
        return this.wrapperInstance;
    }
    //返回代理以后的class
    public Class<?> getWrapperClass(){
        return this.wrapperInstance.getClass();
    }
}
