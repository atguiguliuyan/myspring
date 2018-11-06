package com.liuyan.beans;

import com.liuyan.core.FactoryBean;

/**
 * @author zhaoxiuhuan
 * @date 2018/10/25 15:48
 */
public class BeanPostProcessor extends FactoryBean{

    public Object postProcessBeforInitialization(Object bean,String beanName) throws Exception{
     return null;
    }

    public Object postProcessAfterInitialization(Object bean,String beanName) throws Exception{
        return null;
    }
}
