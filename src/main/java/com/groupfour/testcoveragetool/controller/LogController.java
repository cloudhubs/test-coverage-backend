package com.groupfour.testcoveragetool.controller;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.groupfour.testcoveragetool.group.selenium.SeleniumEndpointEnumerator;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.groupfour.testcoveragetool.group.elasticsearch.ElasticSearchReader;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH}, allowedHeaders = "*")
@RestController
@RequestMapping("/requests/logs")
public class LogController {
	private ElasticSearchReader logReader = new ElasticSearchReader();
	private String field;
	private List<String> regexList;
	private boolean fieldLock = true;
	private boolean regexListLock = true;

	@GetMapping("/endpoints")
	public List<String> getAllEndpoints(@RequestParam("file") MultipartFile file) throws IOException, ParseException, ZipException {
		while (this.fieldLock || this.regexListLock);

		this.fieldLock = true;
		this.regexListLock = true;

		HashSet<String> endpointsTested = null;

		File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
		file.transferTo(tempFile);

		ArrayList<TimeBounds> timeChunks = SeleniumEndpointEnumerator.seleniumTestRunner(tempFile);

		//return endpointsTested;
		for(TimeBounds t : timeChunks) {
			parseLogsForEndpoints(t.getStartTime(), t.getEndTime());
		}
		
		return new ArrayList<String>(endpointsTested);
	}

	@PostMapping("/field")
	public void getField(@RequestBody String field) {
		if (field.charAt(field.length() - 1) == '=') {
			this.field = field.substring(0, field.length() - 1);
		} else {
			this.field = field;
		}

		this.field = this.field.substring(this.field.indexOf(":") + 2, this.field.lastIndexOf("\""));

		this.fieldLock = false;
	}

	@PostMapping("/regexList")
	public void getRegexList(@RequestBody List<String> regexList) {
		this.regexList = new ArrayList<>(regexList);

		this.regexListLock = false;
	}
	
	
	private HashSet<String> parseLogsForEndpoints(Date from, Date to) throws IOException, ParseException {
		return logReader.getEndpointsHit(from, to, field, regexList);
	}
}
