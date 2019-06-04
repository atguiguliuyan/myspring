package com.liuyan.demo.mvc.action;


import com.liuyan.annotation.Autowired;
import com.liuyan.annotation.LyController;
import com.liuyan.annotation.LyRequestMapping;
import com.liuyan.beans.LyModelAndView;
import com.liuyan.demo.service.IDemoService;

@LyController
public class MyAction {

		@Autowired
		IDemoService demoService;
	
		@LyRequestMapping("/index.html")
		public LyModelAndView query(){

			return null;
		}
	
}
