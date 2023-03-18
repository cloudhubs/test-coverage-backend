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

public class ElasticSearchReader {


	// Create the low-level client
	public static RestClient restClient = RestClient.builder(
	    new HttpHost("localhost", 9200)).build();

	// Create the transport with a Jackson mapper
	public static ElasticsearchTransport transport = new RestClientTransport(
	    restClient, new JacksonJsonpMapper());

	// And create the API client
	public static ElasticsearchClient client = new ElasticsearchClient(transport);

	public static void main(String[] args) throws ElasticsearchException, IOException {
		String index = "";
		
		SearchResponse<String> search = client.search(s -> s
		    .index(index)
		    .query(q -> q
		        .term(t -> t
		            .field("")
		            .value(v -> v.stringValue(""))
		        )),
		    String.class);
		
		for (Hit<String> hit: search.hits().hits()) {
		    System.out.println(hit.source());
		}


	}

}
