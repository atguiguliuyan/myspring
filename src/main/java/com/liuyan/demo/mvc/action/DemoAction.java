package com.liuyan.demo.mvc.action;



import com.liuyan.annotation.Autowired;
import com.liuyan.annotation.Controller;
import com.liuyan.annotation.RequestMapping;
import com.liuyan.annotation.RequestParam;
import com.liuyan.beans.LyModelAndView;
import com.liuyan.demo.service.IDemoService;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/demo")
public class DemoAction {
	
	@Autowired
	private IDemoService demoService;
	
	@RequestMapping("/query.json")
	public LyModelAndView query(HttpServletRequest req, HttpServletResponse resp,
								@RequestParam("name") String name){
		String result = demoService.get(name);
		System.out.println(result);
//		try {
//			resp.getWriter().write(result);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return null;
	}
	
	@RequestMapping("/edit.json")
	public LyModelAndView edit(HttpServletRequest req,HttpServletResponse resp,Integer id){
		return null;

	}
	
}
