package com.groupfour.testcoveragetool.group.elasticsearch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupfour.testcoveragetool.controller.CoverageController;
import com.groupfour.testcoveragetool.controller.EndpointInfo;
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
	private int timeDeltaSec;
	private static final String ELASTICHOST = "192.168.3.122";
	private static final int ELASTICPORT = 30092;
	private static final String INDEXNAME = "sw_endpoint_relation_server_side*";
	
	@SuppressWarnings("deprecation")
	private RestHighLevelClient rhlc = new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTICHOST, ELASTICPORT)));
	private SearchRequest request = new SearchRequest(INDEXNAME); //match all indices
	private SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


	public void ElasticSearch(boolean debug, int timeDeltaSeconds) {
		ElasticSearch();
		debugMode = debug;
		timeDeltaSec = timeDeltaSeconds;
	}
	
	public void ElasticSearch() {
		debugMode = false;
		timeDeltaSec = 0;
	}

	public boolean isDebug() {
		return debugMode;
	}
	
	public ElasticSearchReader setDebugMode(boolean debug) {
		debugMode = debug;
		
		return this;
	}


	/**
	 * gets the endpoints hit in a specific time window (automatically corrects the window based on timeDeltaSeconds)
	 * calls getLogsInTimeRange,
	 * @param from start time
	 * @param to end time
	 * @return a HashSet of all the endpoints hit in the time window
	 * @throws IOException
	 * @throws Exception
	 */
	public HashSet<String> getEndpointsHit(Date from, Date to) throws IOException, Exception {
		List<String> logs = getLogsInTimeRange(from, to);

		HashSet<String> l = new HashSet<>(logs);

		CoverageController.setSelenium(EndpointInfo.convertFromStrings(formatLogs(l)));
		System.err.println("hit setter");

		CoverageController.setMavenLock(false);

		return l;
	}

	/**
	 * gets all Endpoints within a specific time range, including duplicates, for all regexes provided
	 * @param start this system's start time
	 * @param stop this system's stop time
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	private List<String> getLogsInTimeRange(Date start, Date stop) throws IOException, Exception {
		List<String> logs = new ArrayList<String>();
		logs.addAll(getLogs(start, stop));
		
		return logs;
	}


	/**
	 * gets all logs within a time range for a given regex
	 * @param start
	 * @param stop
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	private List<String> getLogs(Date start, Date stop) throws IOException, Exception {

		//fix the timestamps using the delta

		LocalDateTime startLDT = LocalDateTime.ofInstant(start.toInstant(), ZoneId.of("UTC")).minusMinutes(1);
		LocalDateTime stopLDT = LocalDateTime.ofInstant(stop.toInstant(), ZoneId.of("UTC")).plusMinutes(1);

		long startTime = dateToSWLogTime(startLDT);
		long endTime = dateToSWLogTime(stopLDT);

		List<String> restLogs = queryLogs(startTime, endTime);

		if(isDebug()) {
			for (String s : restLogs) {
				System.out.println(s);
			}

			System.out.println("Number of endpoints: " + restLogs.size());
		}

		rhlc.close();
		return restLogs;
	}

	public static long dateToSWLogTime(LocalDateTime d) {
		String timeStr = "";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMddHHmm", Locale.ENGLISH)
				.withZone(ZoneOffset.UTC);
		var d2 = LocalDateTime.of(2023,05,05,12,10,10);
		var str2 = d2.format(formatter);
		var str = d.format(formatter);


		return Long.valueOf(str);
	}

	private List<String> queryLogs(long start, long stop) throws Exception {
		List<String> restLogs = new ArrayList<String>();
		QueryBuilder queryBuilder = buildQuery(start, stop);

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
			String srcEndpoint = (String) sourceAsMap.get("source_endpoint");
			String destEndpoint = (String) sourceAsMap.get("dest_endpoint");

			restLogs.add(decode(srcEndpoint));
			restLogs.add(decode(destEndpoint));
		}

		return restLogs;
	}

	private String decode(String str) throws UnsupportedEncodingException {
		List<String> decodedMessages = new ArrayList<>();
		List<String> split = splitString(str);
		String decodedValue = "";

		//decode here
		for(String s:split) {
			if(!isInvalid(s)) {
				byte[] decodedBytes = Base64.getDecoder().decode(s);
				decodedMessages.add(new String(decodedBytes, "UTF-8"));
			}
		}


		String fullEndpoint = "";
		String serviceName = "";

		for(String msg:decodedMessages) {
			if(isRequest(msg)) {
				fullEndpoint = msg;
			}
			else if(msg.startsWith("ts-")) {
				serviceName = msg;
			}
		}

		if(isDebug()) {
			System.out.println(serviceName + "#" + fullEndpoint);
		}


		return fullEndpoint;
	}

	private static List<String> splitString(String str) {
		String[] parts = str.split("(\\.\\d+[_-]?|[-_])");
		List<String> splitParts = new ArrayList<>();
		for(String s:parts) {
			splitParts.add(s);
		}


		return splitParts;
	}

	private static Boolean isInvalid(String s) {
		return ((s.equals(".1_")) || (s.equals(".1-")) || (s.equals(".1")) || (s.equals(".0_")) || (s.equals(".0-")) || (s.equals(".0")));
	}


	private static Boolean isRequest(String s) {
		return s.contains("GET") || s.contains("POST") || s.contains("PUT") || s.contains("DELETE");
	}



	private QueryBuilder buildQuery(long startTime, long endTime) {
		QueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.rangeQuery("time_bucket").gte(startTime).lte(endTime));
		return queryBuilder;
	}


	private HashSet<String> formatLogs(HashSet<String> logs) {
		HashSet<String> formatted = new HashSet<>();

		for(String s:logs) {
			formatted.add(s.replace(":", " "));
		}

		return formatted;
	}


}
