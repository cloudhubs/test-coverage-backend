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

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class LogReader {

	private static RestHighLevelClient rhlc = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.3.122", 9200)));
	private static SearchRequest request = new SearchRequest("jaeger-span-2023-05-03"); //match all indices
	private static SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

	private static final int TIMEDELTASEC = -1;

	public static void main(String[] args) throws Exception {

		timeQuery();
		//diffQuery();


	}




	public static String shortenURL(String s) throws Exception {
		String[] parts = s.split("\\s+");
		String method = parts[0];
		String url = parts[1].replaceAll(" ", "%20");
		URI uri = new URI(url);
		String path = uri.getPath();
		return method + " " + path;
	}


	public static void timeQuery() throws Exception {
		long startTime = Instant.now().toEpochMilli();
		startTime += (TIMEDELTASEC * 1000) ;
		System.out.println(startTime);
		Thread.sleep(5000);

		long endTime = Instant.now().toEpochMilli();
		endTime += (TIMEDELTASEC * 1000);
		System.out.println(endTime);


		List<String> restLogs = queryLogsTime(startTime, endTime);

		for(String s:restLogs) {
			System.out.println(s);
		}


		//System.out.println("Before: " + before.size());
		System.out.println("After: " + restLogs.size());





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

		return restLogs;
	}

	private static QueryBuilder buildQueryTime(long startTime, long endTime) {
		QueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.nestedQuery("tags", QueryBuilders.termQuery("tags.key", "http.method"), ScoreMode.None))
				.must(QueryBuilders.nestedQuery("tags", QueryBuilders.termQuery("tags.key", "http.url"), ScoreMode.None))
		.filter(QueryBuilders.rangeQuery("startTimeMillis").gte(startTime).lte(endTime));

		return queryBuilder;
	}

	public static List<String> diffQuery() throws Exception {

		HashSet<Pair<String, String>> before = queryLogs();
		System.out.println("Starting");
		Thread.sleep(10000);

		HashSet<Pair<String, String>> after = queryLogs();


		System.out.println("Before: " + before.size());
		System.out.println("After: " + after.size());

		diff(before, after);




		rhlc.close();

		return null;
	}

	private static HashSet<Pair<String, String>> queryLogs() throws Exception {
		HashSet<Pair<String, String>> restLogs = new HashSet<Pair<String, String>>();
		QueryBuilder queryBuilder = buildQuery();

		searchSourceBuilder.query(queryBuilder);
		//retrieve the maximum number of logs
		searchSourceBuilder.size(10000);
		request.source(searchSourceBuilder);

		SearchResponse searchResponse = rhlc.search(request, RequestOptions.DEFAULT); //perform the request
		SearchHits hits = searchResponse.getHits();

		for (SearchHit hit : hits) {
			String sourceAsString = hit.getSourceAsString();
			String id = hit.getId();
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
				restLogs.add(new Pair<>(id, shortenURL(methodUrl)));
			}
		}

		return restLogs;
	}

	private static QueryBuilder buildQuery() {
		QueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.nestedQuery("tags", QueryBuilders.termQuery("tags.key", "http.method"), ScoreMode.None))
				.must(QueryBuilders.nestedQuery("tags", QueryBuilders.termQuery("tags.key", "http.url"), ScoreMode.None));

		return queryBuilder;
	}

	private static List<String> diff(HashSet<Pair<String, String>> before, HashSet<Pair<String, String>> after) {
		List<String> diff = new ArrayList<>();
		after.removeAll(before);
		System.out.println("Size: " + after.size());

		for(Pair<String, String> p:after) {
			diff.add(p.getValue());
			System.out.println(p.getValue());
		}


		return diff;
	}
}
