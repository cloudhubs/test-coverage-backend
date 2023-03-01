package com.groupfour.testcoveragetool;

import com.groupfour.testcoveragetool.group.elasticsearch.LogReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groupfour.testcoveragetool.controller.SeleniumController;

import java.io.IOException;

@SpringBootApplication
public class TestCoverageToolApplication {
	
	@Autowired
	private SeleniumController seleniumController;
	
	public static void main(String[] args) {
		SpringApplication.run(TestCoverageToolApplication.class, args);

		try {
			LogReader.elasticServiceThing();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
