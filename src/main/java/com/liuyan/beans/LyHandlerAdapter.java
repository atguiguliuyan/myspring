package com.liuyan.beans;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse; /**
 * @author liuyan
 * @date 2018/10/30 15:36
 */
public class LyHandlerAdapter {
    /**
     *
     * @param req
     * @param resp
     * @param handler  handler 中包含了controller，method，url
     * @return
     */
    public LyModelAndView handle(HttpServletRequest req, HttpServletResponse resp, LyHandlerMapping handler) {
        //根据用户请求的参数信息，跟method中参数信息进行动态匹配
        //response 传进来的目的，将其值赋值给方法参数
        //z中油当用户传过来的modelandview 不为空在常见
        return null;
    }
}
