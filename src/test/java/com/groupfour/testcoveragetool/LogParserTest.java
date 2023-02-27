package com.groupfour.testcoveragetool;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import com.groupfour.testcoveragetool.group.selenium.LogParser;

class LogParserTest {

	@Test
	void testGET() throws FileNotFoundException {
		String testData = """
				Get /person/abc
				GET /abc
				get /def
				get
				GET
				g et
				abcd
				GET /r/
				""";
		Scanner s = new Scanner(testData);
		
		assertEquals(3, LogParser.parseLogs(s).size());
	}
	
	@Test 
	void testPOST() {
		String testData = """
				post /person/abc
				POST /abc
				post /def
				post
				POST
				po st
				abcd
				POST /r/
				""";
	}

}
