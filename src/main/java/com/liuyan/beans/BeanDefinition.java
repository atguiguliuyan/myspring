package com.liuyan.beans;

import com.liuyan.core.FactoryBean;

/**
 * @author liuyan
 * @date 2018/10/16 15:46
 */
public class BeanDefinition extends FactoryBean{
    private String beanClassName;
    private boolean lazyInit = false;
    private String factoryBeanname;
    private boolean isSimple =true;

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }



    public String getFactoryBeanname() {
        return factoryBeanname;
    }

    public void setFactoryBeanname(String factoryBeanname) {
        this.factoryBeanname = factoryBeanname;
    }

    public boolean isSimple() {
        return isSimple;
    }

    public void setSimple(boolean simple) {
        isSimple = simple;
    }
}

