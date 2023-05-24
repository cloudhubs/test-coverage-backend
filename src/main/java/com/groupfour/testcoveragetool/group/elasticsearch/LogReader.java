package com.groupfour.testcoveragetool.group.elasticsearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javafx.util.Pair;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


public class LogReader {

	private static RestHighLevelClient rhlc = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.3.122", 30092)));
	private static SearchRequest request = new SearchRequest("sw_endpoint_relation_server_side*"); //match all indices
	private static SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

	private static final int TIMEDELTASEC = -1;

	public static void main(String[] args) throws Exception {

		//timeQuery();
		//diffQuery();

		LocalDateTime startLDT = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")).minusMinutes(1);
		LocalDateTime stopLDT = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")).plusMinutes(1);

		System.out.println(dateToSWLogTime(startLDT));
		System.out.println(dateToSWLogTime(stopLDT));

		timeQueryBounds(202305242000l, 202305242156l);

	}

	public static long dateToSWLogTime(LocalDateTime d) {
		String timeStr = "";


		int year = d.getYear();
		int month = d.getMonthValue();
		//month++; //january is 0 lol
		int day = d.getDayOfMonth();
		int hours = d.getHour();
		int minute = d.getMinute();

		timeStr += year;
		timeStr += month;
		timeStr += day;
		if(hours <= 10) {
			timeStr += 0;
		}
		timeStr += hours;
		if(minute <= 10) {
			timeStr += "0";
		}
		timeStr += minute;

		return Long.valueOf(timeStr);
	}

	public static void timeQueryBounds(long startTime, long endTime) throws Exception {


		List<String> restLogs = queryLogsTime(startTime, endTime);

		Set<String> restSet = new HashSet<>();
		restSet.addAll(restLogs);

		System.out.println(restSet.size());

		for(String s:restSet) {
			System.out.println(s);
		}



		rhlc.close();
	}


	private static List<String> queryLogsTime(long start, long stop) throws Exception {
		List<String> restLogs = new ArrayList<String>();
		QueryBuilder queryBuilder = buildQueryTime(start, stop);

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

	private static String decode(String str) throws UnsupportedEncodingException {
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

		return serviceName + "#" + fullEndpoint;
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

	private static QueryBuilder buildQueryTime(long startTime, long endTime) {
		/*
		QueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.nestedQuery("tags", QueryBuilders.termQuery("tags.key", "http.method"), ScoreMode.None))
				.must(QueryBuilders.nestedQuery("tags", QueryBuilders.termQuery("tags.key", "http.url"), ScoreMode.None))
		.filter(QueryBuilders.rangeQuery("startTimeMillis").gte(startTime).lte(endTime));
		 */

		QueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.rangeQuery("time_bucket").gte(startTime).lte(endTime));

		return queryBuilder;
	}
}
