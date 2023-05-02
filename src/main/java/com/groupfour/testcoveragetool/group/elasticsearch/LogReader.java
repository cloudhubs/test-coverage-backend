package com.groupfour.testcoveragetool.group.elasticsearch;

import com.fasterxml.jackson.annotation.JsonValue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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


public class LogReader {

	public static void main(String[] args) throws Exception {
		RestHighLevelClient rhlc = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.3.122", 9200)));
		SearchRequest request = new SearchRequest("jaeger-span-2023-05-02"); //match all indices
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		List<String> restLogs = new ArrayList<String>();

		//build the query
		//long startTime = Instant.parse("2023-05-02T00:00:00Z").toEpochMilli();
		//long endTime = Instant.parse("2023-05-03T00:00:00Z").toEpochMilli();

		long startTime = Instant.now().toEpochMilli();
		System.out.println(startTime);
		Thread.sleep(10000);

		long endTime = Instant.now().toEpochMilli();
		System.out.println(endTime);

		QueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.nestedQuery("tags", QueryBuilders.termQuery("tags.key", "http.method"), ScoreMode.None))
				.must(QueryBuilders.nestedQuery("tags", QueryBuilders.termQuery("tags.key", "http.url"), ScoreMode.None))
				.filter(QueryBuilders.rangeQuery("startTimeMillis").gte(startTime).lte(endTime));

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


		for(String s:restLogs) {
			System.out.println(s);
		}

		System.out.println(restLogs.size());





		rhlc.close();
	}

	public static String shortenURL(String s) throws Exception {
		String[] parts = s.split("\\s+");
		String method = parts[0];
		String url = parts[1].replaceAll(" ", "%20");
		URI uri = new URI(url);
		String path = uri.getPath();
		return method + " " + path;
	}



}
