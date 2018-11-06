package com.liuyan.demo.mvc.action;


import com.liuyan.annotation.Autowired;
import com.liuyan.annotation.Controller;
import com.liuyan.annotation.RequestMapping;
import com.liuyan.beans.LyModelAndView;
import com.liuyan.demo.service.IDemoService;

@Controller
public class MyAction {

		@Autowired
		IDemoService demoService;
	
		@RequestMapping("/index.html")
		public LyModelAndView query(){

			return null;
		}
	
}
