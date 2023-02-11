package com.groupfour.testcoveragetool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
@SpringBootApplication
public class TestCoverageToolApplication {

	@RequestMapping("/")
	String ping() {
		return "pong";
	}
	
	public static void main(String[] args) {
		SpringApplication.run(TestCoverageToolApplication.class, args);
	}

}
