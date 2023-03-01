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


public class LogReader {

	public static void elasticServiceThing() throws IOException, JSONException {
		String index = "logstash-2023.02.27-000001";
		String id = "DJSGm4YBdjksNf-3lgN1";
		URL url = new URL("http://localhost:9200/" + index + "/_search?pretty=true&q=*.*&size=100");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;

		StringBuffer response = new StringBuffer();

		while(((inputLine = in.readLine()) != null)) {

			//JSONObject jsonArray = new JSONObject(inputLine);
			//System.out.println(jsonArray.getString("message"));
			/*for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				System.out.println(object.getString("message"));
			}*/


			for(String x : inputLine.split(",")) {
				if(x.toLowerCase().contains("message") && x.contains("/")){

					String path = x.substring(x.indexOf("/"), x.length() - 1);

					if(x.toLowerCase().contains("get")) {
						System.out.println("GET " + path) ;
					}
					else if(x.toLowerCase().contains("post")) {
						System.out.println("POST " + path);
					}
					else if(x.toLowerCase().contains("put")) {
						System.out.println("PUT " + path);
					}
					else if(x.toLowerCase().contains("delete")) {
						System.out.println("DELETE " + path);
					}
					else if(x.toLowerCase().contains("patch")) {
						System.out.println("PATCH " + path);
					}
					else if(x.toLowerCase().contains("request")) {
						System.out.println("REQUEST " + path);
					}
					//System.out.println(x);
				}
			}


			response.append(inputLine);
		}
		in.close();
		System.out.println(response.toString());
	}

}
