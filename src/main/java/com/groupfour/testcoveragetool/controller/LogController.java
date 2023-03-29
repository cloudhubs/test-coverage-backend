package com.groupfour.testcoveragetool.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.groupfour.testcoveragetool.group.elasticsearch.ElasticSearchReader;

@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH}, allowedHeaders = "*")
@RestController
@RequestMapping("/requests/logs")
public class LogController {
	private ElasticSearchReader logReader = new ElasticSearchReader();
	
	@GetMapping("/endpoints")
	public List<String> getAllEndpoints() {
		List<String> endpointsTested = null;
		
		/* Maybe we want to make a test class where we pass the test file, and just call a method to run it and return the enpoints??? */
		//for all tests
			//start timer
			//run test
			//stop timer
			//append result of call to parseLogsForEndpoints
		
		
		return endpointsTested;
	}
	
	
	private List<String> parseLogsForEndpoints(Date from, Date to) throws IOException, ParseException {
		String start = "2023-03-22T18:21:00.000Z"; //start time to query
		String stop = "2023-03-22T18:59:59.999Z";
		
		return logReader.getEndpointsHit(from, to);
	}
}
