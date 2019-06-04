package com.liuyan.context;

import com.liuyan.annotation.Autowired;
import com.liuyan.annotation.LyController;
import com.liuyan.annotation.Service;
import com.liuyan.beans.BeanDefinition;
import com.liuyan.beans.BeanPostProcessor;
import com.liuyan.beans.BeanWrapper;
import com.liuyan.core.BeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liuyan
 * @date 2018/10/16 14:22
 */
public class LyApplicationContext implements BeanFactory{

    private String[] configLocations;

    private BeanDefinitionReader reader;
    //保存配置信息
    private Map<String ,BeanDefinition>  beanDefinitionMap=new ConcurrentHashMap<>();
    //用来保证注册式单利
    private Map<String,Object> beanCacheMap= new HashMap<>();
    //存储所有被带离过的对象
    private Map<String ,BeanWrapper> beanWrapperMap=new ConcurrentHashMap<>();



    public LyApplicationContext(String ...configLocations) {
        this.configLocations = configLocations;
        this.refresh();
    }

    public void  refresh(){
        this.reader=new BeanDefinitionReader(configLocations);
        //加载
        List<String> strings = reader.loadBeanDefinitions();

        doRegisty(strings);

        doAutowried();

        Object demoAction = getBean("demoAction");

        System.out.println(demoAction);

    }

    private void doAutowried() {
        for (Map.Entry<String,BeanDefinition> beanDefinitionEntry :this.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();
            if(!beanDefinitionEntry.getValue().isLazyInit()){
                getBean(beanName);
            }
        }
        for (Map.Entry<String,BeanWrapper> beanDefinitionEntry :this.beanWrapperMap.entrySet()){
            populateBean(beanDefinitionEntry.getKey(),beanDefinitionEntry.getValue().getWrappedInstance());
        }

    }

    public void populateBean(String beanName,Object instance){
        Class<?> aClass = instance.getClass();
        if(!(aClass.isAnnotationPresent(LyController.class))||aClass.isAnnotationPresent(Service.class)){
            return ;
        }
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            if(!field.isAnnotationPresent(Autowired.class)){
                continue;
            }
            Autowired annotation = field.getAnnotation(Autowired.class);
            String trim = annotation.value().trim();
            if("".equals(trim)){
                trim=field.getType().getName();
            }
            field.setAccessible(true);
            try{
                field.set(instance,this.beanWrapperMap.get(trim).getWrappedInstance());
            }catch (Exception e){

            }
        }

    }

    //这里不会吧最原始的对象放出去，会用一个beanwrapper来进行一次包装
    //保留原来的oop关系
    //需要对他进行扩展，增强（为aop打基础）
    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        String className = beanDefinition.getBeanClassName();

        try {
            BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
            //在实例初始化以前调用一次
            Object o = instantionBean(beanDefinition);
            beanPostProcessor.postProcessBeforInitialization(o,beanName);
            if(o==null){
                return null;
            }
            BeanWrapper beanWrapper = new BeanWrapper(o);
            this.beanWrapperMap.put(beanName,beanWrapper);
            beanPostProcessor.postProcessAfterInitialization(o,beanName);

            return this.beanWrapperMap.get(beanName).getWrappedInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    //传benadefinition 返回一个实例
    private Object instantionBean(BeanDefinition beanDefinition){
        Object instance=null;
        String className= beanDefinition.getBeanClassName();
        try {
            if(beanDefinition.isSimple()){
                if(this.beanCacheMap.containsKey(className)){
                    instance=this.beanCacheMap.get(className);
                }else{
                    synchronized (this){
                        if(!beanCacheMap.containsKey(className)){
                            Class<?> aClass = Class.forName(className);
                            instance=aClass.newInstance();
                            this.beanCacheMap.put(className,instance);
                        }
                     }
                 }
             }

        }catch (Exception e){

        }
        return instance;
    }

    private void  doRegisty(List<String> beandefinations){
        try {
        for (String className:beandefinations){
            //beanName 有三种情况1.默认类首字母小写2自定义3接口注入
            Class<?> beanClass=Class.forName(className);
            if (beanClass.isInterface()){continue;}
            BeanDefinition beanDefinition = reader.registerBeanDefinition(className);
            if(beanDefinition!=null){
                this.beanDefinitionMap.put(beanDefinition.getFactoryBeanname(),beanDefinition);
            }
            Class<?>[] interfaces = beanClass.getInterfaces();
            for (Class<?> i:interfaces){
                //如果是多个实现类，只能覆盖
                // spring 没那么智能，可以自定义名字
                this.beanDefinitionMap.put(i.getName(),beanDefinition);
            }

        }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


}
