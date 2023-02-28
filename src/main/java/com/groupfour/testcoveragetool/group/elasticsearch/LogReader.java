package com.groupfour.testcoveragetool.group.elasticsearch;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class LogReader {

	// Create the low-level client
	private static RestClient restClient = RestClient.builder(
	    new HttpHost("localhost", 9200)).build();

	
	// Create the transport with a Jackson mapper
	private static ElasticsearchTransport transport = new RestClientTransport(
	    restClient, new JacksonJsonpMapper());

	// And create the API client
	private static ElasticsearchClient client = new ElasticsearchClient(transport);

	public static void main(String[] args) throws ElasticsearchException, IOException {
		SearchResponse<String> search = client.search(s -> s
			    .index("products")
			    .query(q -> q
			        .term(t -> t
			            .field("message")
			            //.value(v -> v.stringValue("bicycle"))
			        )),
			    String.class);
		
		for(Hit<String> hit:search.hits().hits()) {
			System.out.println(hit.source());
		}
	}

}
