package com.groupfour.testcoveragetool.group.selenium;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.groupfour.testcoveragetool.group.APIType;


public class LogParser {
	private static final String ALLREQUESTS = APIType.BASE + "|" + APIType.DELETE + "|" + APIType.GET + "|" 
								    		+ APIType.PATCH + "|" + APIType.POST + "|" + APIType.PUT + "|"
								    		+ APIType.REQUEST + "|" + APIType.UNDEFINED;
	private static final Pattern GENERALREQUEST = Pattern.compile("^(" + ALLREQUESTS + ") /.*");
	
	private static final Pattern GETREQUEST = requestRegex(APIType.GET);
	private static final Pattern POSTREQUEST = requestRegex(APIType.POST);
	private static final Pattern PUTREQUEST = requestRegex(APIType.PUT);
	private static final Pattern DELETEREQUEST = requestRegex(APIType.DELETE);
	private static final Pattern PATCHREQUEST = requestRegex(APIType.PATCH);
	private static final Pattern UNDEFINEDREQUEST = requestRegex(APIType.UNDEFINED);
	
	private static Pattern requestRegex(APIType requestType) {
		return Pattern.compile("^" + requestType + " /.*");
	}
	
	//this method allows the user to ask for all requests
	public static List<String> parseLogs(Scanner logReader) throws FileNotFoundException {
		ArrayList<String> calls = new ArrayList<String>();
		
		while(logReader.hasNext()) {
			String line = logReader.nextLine();
			
			if(isCall(line)) {
				calls.add(line);
			}
		}
		
		return calls;
	}
	
	//this method allows for the user to ask for only specific types, and in general is better to use
	public static HashMap<APIType, List<String>> parseLogs(Scanner s, HashSet<APIType> types) {
		HashMap<APIType, List<String>> requests = new HashMap<APIType, List<String>>();
		
		while(s.hasNext()) {
			String line = s.nextLine();
			
			if(isCall(line)) {
				APIType t = getRequestType(line);
				addItem(t, requests, line);
			}
		}
		
		return requests;
	}
	
			
	
	
	private static boolean isCall(String line) {
		Matcher matcher = GENERALREQUEST.matcher(line);
		
		return matcher.find();
	}
	
	private static APIType getRequestType(String line) {
		Matcher getMatcher = GETREQUEST.matcher(line);
		Matcher postMatcher = POSTREQUEST.matcher(line);
		Matcher putMatcher = PUTREQUEST.matcher(line);
		Matcher deleteMatcher = DELETEREQUEST.matcher(line);
		Matcher patchMatcher = PATCHREQUEST.matcher(line);
		Matcher undefinedMatcher = UNDEFINEDREQUEST.matcher(line);
		
	
		if(getMatcher.matches()) {
			return APIType.GET;
		}
		else if(postMatcher.matches()) {
			return APIType.POST;
		}
		else if(putMatcher.matches()) {
			return APIType.PUT;
		}
		else if(deleteMatcher.matches()) {
			return APIType.DELETE;
		}
		else if(patchMatcher.matches()) {
			return APIType.PATCH;
		}
		else {
			return APIType.UNDEFINED;
		}
	}
	
	
	private static void addItem(APIType t, HashMap<APIType, List<String>> map, String line) {
		if(!map.containsKey(t)) {
			List<String> requests = new ArrayList<String>();
			requests.add(line);
			map.put(t, requests);
		}
		else {
			List<String> requests = map.get(t);
			requests.add(line);
			map.put(t, requests);
		}
	}

	
}
