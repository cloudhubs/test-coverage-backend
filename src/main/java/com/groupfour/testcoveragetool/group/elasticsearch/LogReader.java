package com.groupfour.testcoveragetool.group.elasticsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class LogReader {

	public static void elasticServiceThing() throws IOException {
		URL url = new URL("http://localhost:9200/logstash-2023.02.27-000001/_search");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;

		StringBuffer response = new StringBuffer();

		while(((inputLine = in.readLine()) != null)) {
			response.append(inputLine);
		}
		in.close();
		System.out.println(response.toString());
	}

}
