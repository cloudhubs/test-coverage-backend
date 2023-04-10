package com.groupfour.testcoveragetool.group.elasticsearch;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.groupfour.testcoveragetool.group.APIType;

public class ElasticSearchReader {

	private boolean debugMode;
	
	
	@SuppressWarnings("deprecation")
	private RestHighLevelClient rhlc = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200)));
	private SearchRequest request = new SearchRequest("*"); //match all indices
	private SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public void ElasticSearch(boolean debug) {
		ElasticSearch();
		debugMode = debug;
	}
	
	public void ElasticSearch() {
		debugMode = false;
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); //set up for central timezone
	}
	
	public boolean isDebug() {
		return debugMode;
	}
	
	public ElasticSearchReader setDebugMode(boolean debug) {
		debugMode = debug;
		
		return this;
	}
	
	public List<String> getLogsInTimeRange(Date start, Date stop, String field, List<String> regexList) throws IOException, ParseException {
		List<String> logs = new ArrayList<String>();
		for(String regex:regexList) {
			logs.addAll(getLogs(start, stop, field, regex));
		}
		
		return logs;
	}
	
	public HashSet<String> getEndpointsHit(Date from, Date to, String field, List<String> regexList) throws IOException, ParseException {
		List<String> logs = getLogsInTimeRange(from, to, field, regexList);
		return getEndpointsFromLogs(logs);
	}

	@SuppressWarnings("deprecation")
	private List<String> getLogs(Date start, Date stop, String field, String regex) throws IOException {
		List<String> restLogs = new ArrayList<String>();
		String startStr = dateFormat.format(start);
		String stopStr = dateFormat.format(stop);

		//build the query
		searchSourceBuilder.query(QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("@timestamp") //query based on timestamp
						.gte(startStr) //get all greater than
						.lte(stopStr)) //get all less than
				.must(QueryBuilders.queryStringQuery(regex).field(field)) //get all that contain GET, PUT, POST, etc.
				//.must(QueryBuilders.regexpQuery(field, regex))
		);

		//retrieve the maximum number of logs
		searchSourceBuilder.size(10000);
		request.source(searchSourceBuilder);

		SearchResponse searchResponse = rhlc.search(request, RequestOptions.DEFAULT); //perform the request
		SearchHits hits = searchResponse.getHits();
		
		for(SearchHit hit:hits.getHits()) {
			String source = hit.getSourceAsString();
			restLogs.add(source);
			if(isDebug()) {
				System.out.println(source);
			}
		}
		
		if(isDebug()) {
			System.out.println("Total Hits: " + hits.getTotalHits());
			
			for(String s:restLogs) {
				System.out.println(s);
			}
			
			System.out.println();
		}
		
		
		rhlc.close();
		return restLogs;
	}
	
	public HashSet<String> getEndpointsFromLogs(List<String> logs) {
		HashSet<String> endpoints = new HashSet<String>();
		
		for(String log:logs) {
			String endpoint = extractEndpoint(log);
			
			if(isDebug()) {
				System.out.println(endpoint);
			}
			
			endpoints.add(endpoint);
		}
		
		return endpoints;
	}
	
	public static String extractEndpoint(String str) {
		Objects.requireNonNull(str, "String is null");
		String endpoint = "";
		endpoint += findRequestType(str);
		endpoint += " ";
		endpoint += findMapping(str);
		
		return endpoint;
	}
	
	public static String findRequestType(String log) {
		for(APIType request: APIType.values()) {
			if(log.contains(request.toString())) {
				return request.toString();
			}
		}
		
		return null;
	}
	
	public static String findMapping(String log) {
		Objects.requireNonNull(log, "Log is null");
		if(log.isEmpty()) {
			throw new IllegalArgumentException("Log is empty");
		}
		String requestType = findRequestType(log);
		
		int start = log.indexOf("/", log.indexOf(requestType));
		int stop = log.indexOf(" ", start);
		
		if(stop == -1 && start != -1) {
			stop = log.length() - 1;
		}
		
		if(start == -1 || stop == -1) {
			throw new IllegalArgumentException("Mapping not formatted properly");
		}
		
		return log.substring(start, stop);
	}
}
