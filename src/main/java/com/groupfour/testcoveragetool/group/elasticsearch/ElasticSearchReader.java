package com.groupfour.testcoveragetool.group.elasticsearch;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class ElasticSearchReader {

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public static void main(String[] args) throws ParseException, IOException, InterruptedException {

		//Date start = dateFormat.parse("2023-03-22T18:21:00.000Z"); //start time to query
		//Date stop = dateFormat.parse("2023-03-22T18:59:59.999Z"); //stop time to query
		Date start = Date.from(Instant.now());
		
		Thread.sleep(5000);
		
		Date stop = Date.from(Instant.now());
		
		getLogsInTimeRange(start, stop);
	}
	
	public static void getLogsInTimeRange(Date start, Date stop) throws IOException, ParseException {
		init(); //refactor eventually


		getLogs(start, stop);
	}

	public static void init() {
		dateFormat.setTimeZone(TimeZone.getTimeZone("US/Central")); //set up for central timezone
	}


	@SuppressWarnings("deprecation")
	public static void getLogs(Date start, Date stop) throws IOException {
		String startStr = dateFormat.format(start);
		String stopStr = dateFormat.format(stop);

		RestHighLevelClient rhlc = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200)));
		SearchRequest request = new SearchRequest("*"); //match all indices
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		//the actual query lol
		searchSourceBuilder.query(QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("@timestamp") //query based on timestamp
						.gte(startStr) //get all greater than
						.lte(stopStr)) //get all less than
				.must(QueryBuilders.queryStringQuery("*GET* OR *POST* OR *PUT* OR *DELETE*").field("message")) //get all that contain GET, PUT, POST, etc.
		);


		searchSourceBuilder.size(10000);
		request.source(searchSourceBuilder);


		SearchResponse searchResponse = rhlc.search(request, RequestOptions.DEFAULT); //perform the request
		SearchHits hits = searchResponse.getHits();



		for(SearchHit hit:hits.getHits()) {
			String source = hit.getSourceAsString();
			System.out.println(source);
		}

		System.out.println("\nTotal hits: " + hits.getTotalHits() + '\n');


		rhlc.close();
	}

}
