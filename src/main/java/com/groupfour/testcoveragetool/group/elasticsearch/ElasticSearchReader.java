package com.groupfour.testcoveragetool.group.elasticsearch;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.groupfour.testcoveragetool.group.APIType;

public class ElasticSearchReader {

	private boolean debugMode;
	
	
	@SuppressWarnings("deprecation")
	private RestHighLevelClient rhlc = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.3.122", 9200)));
	private SearchRequest request = new SearchRequest("jaeger-span-*"); //match all indices
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
	
	public List<String> getLogsInTimeRange(Date start, Date stop, String field, List<String> regexList) throws IOException, Exception {
		List<String> logs = new ArrayList<String>();
		for(String regex:regexList) {
			logs.addAll(getLogs(start, stop, field, regex));
		}
		
		return logs;
	}
	
	public HashSet<String> getEndpointsHit(Date from, Date to, String field, List<String> regexList) throws IOException, Exception {
		List<String> logs = getLogsInTimeRange(from, to, field, regexList);
		//return getEndpointsFromLogs(logs);
		return new HashSet<String>(logs);
	}

	@SuppressWarnings("deprecation")
	private List<String> getLogs(Date start, Date stop, String field, String regex) throws IOException, Exception {
		List<String> restLogs = new ArrayList<String>();

		//build the query

		QueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.nestedQuery("tags", QueryBuilders.termQuery("tags.key", "http.method"), ScoreMode.None))
				.must(QueryBuilders.nestedQuery("tags", QueryBuilders.termQuery("tags.key", "http.url"), ScoreMode.None))
				.filter(QueryBuilders.rangeQuery("startTimeMillis").gte(start.toInstant().toEpochMilli()).lte(stop.toInstant().toEpochMilli()));



		searchSourceBuilder.query(queryBuilder);
		//retrieve the maximum number of logs
		searchSourceBuilder.size(10000);
		request.source(searchSourceBuilder);

		SearchResponse searchResponse = rhlc.search(request, RequestOptions.DEFAULT); //perform the request
		SearchHits hits = searchResponse.getHits();

		for (SearchHit hit : hits) {
			String sourceAsString = hit.getSourceAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> sourceAsMap = objectMapper.readValue(sourceAsString, new TypeReference<Map<String, Object>>() {});
			List<Map<String, Object>> tags = (List<Map<String, Object>>) sourceAsMap.get("tags");

			String method = null;
			String url = null;

			for (Map<String, Object> tag : tags) {
				String key = (String) tag.get("key");
				if (key.equals("http.method")) {
					method = (String) tag.get("value");
				} else if (key.equals("http.url")) {
					url = (String) tag.get("value");
				}

				if (method != null && url != null) {
					break;
				}
			}

			if (method != null && url != null) {
				String methodUrl = method + " " + url;
				restLogs.add(shortenURL(methodUrl));
			}
		}

		rhlc.close();
		return restLogs;
	}


	public String shortenURL(String s) throws Exception {
		String[] parts = s.split("\\s+");
		String method = parts[0];
		String url = parts[1].replaceAll(" ", "%20");
		URI uri = new URI(url);
		String path = uri.getPath();
		return method + " " + path;
	}

	/*
	//Unused?
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
	*/

}
