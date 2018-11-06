package com.liuyan.beans;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @author liuyan
 * @date 2018/10/30 15:36
 */
public class LyHandlerMapping {

    private Object controller;

    private Method method;

    private Pattern patten;

    public LyHandlerMapping(Object controller, Method method, Pattern patten) {
        this.controller = controller;
        this.method = method;
        this.patten = patten;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPatten() {
        return patten;
    }

    public void setPatten(Pattern patten) {
        this.patten = patten;
    }
}
