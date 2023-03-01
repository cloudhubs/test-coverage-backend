package com.groupfour.testcoveragetool;

import com.groupfour.testcoveragetool.controller.SwaggerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groupfour.testcoveragetool.controller.SeleniumController;

@SpringBootApplication
public class TestCoverageToolApplication {
	
	@Autowired
	private SeleniumController seleniumController;

	@Autowired
	private SwaggerController swaggerController;
	
	public static void main(String[] args) {
		SpringApplication.run(TestCoverageToolApplication.class, args);
	}

}
