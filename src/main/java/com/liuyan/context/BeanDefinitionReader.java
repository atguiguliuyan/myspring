package com.liuyan.context;

import com.liuyan.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author liuyan
 * @date 2018/10/16 15:43
 */
//对配置文件进行查找，读取，解析
public class BeanDefinitionReader {

    private Properties config=new Properties();

    private final String SCAN_PACKAGE="scanPackage";

    private List<String> registyBeanClasses=new ArrayList<>();
    public BeanDefinitionReader(String ...locations) {
        String path=locations[0].replace("classpath:","");
        InputStream is=this.getClass().getClassLoader().getResourceAsStream(path);
        try {
            config.load(is);
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
        doScanner(config.getProperty("scanPackage"));
    }
    //注册返回beandefinition
    public BeanDefinition registerBeanDefinition(String className){
        if(this.registyBeanClasses.contains(className)){
           BeanDefinition beanDefinition=new BeanDefinition();
           beanDefinition.setBeanClassName(className);
           beanDefinition.setFactoryBeanname(lowerFirestCase(className.substring(className.lastIndexOf(".")+1)));
           return beanDefinition;
        }
        return null;
    }

    public List<String> loadBeanDefinitions(){
        return registyBeanClasses;
    }


    public Properties getConfig(){
        return this.config;
    }

    //地柜扫描所有的相关联class
    private void doScanner(String packageName) {
        URL resource = this.getClass().getClassLoader().getResource("/"+packageName.replace(".","/"));
        File classDIr=new File(resource.getFile());
        for (File file:classDIr.listFiles()){
            if(file.isDirectory()){
                doScanner(packageName+"."+file.getName());
            }else{
                registyBeanClasses.add(packageName+"."+file.getName().replace(".class",""));
            }
        }
    }
    private String lowerFirestCase(String str){
        char[] chars = str.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }
}
