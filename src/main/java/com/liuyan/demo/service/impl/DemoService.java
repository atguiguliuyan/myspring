package com.liuyan.demo.service.impl;


import com.liuyan.annotation.Service;
import com.liuyan.demo.service.IDemoService;

@Service
public class DemoService implements IDemoService {
	@Override
	public String get(String name) {
		return "My name is " + name;
	}

}
