package com.facebook.graph;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;


public class FacebookTest {
	public static final String CLIENT_ID = "454716711382741";
	public static final String CLIENT_SECRET = "528dfe16ce9a65a209c42a2bd1268301";
	public static final String API_ENDPOINT_URL = "https://graph.facebook.com/";
	public static final String CALLBACK_URL = "http://www.example.com";
	
	public static void main(String[] args) {
		//Step 1: open browser and ask user to allow access: get user access token
		String userGrantAccessUrl = "https://www.facebook.com/dialog/oauth?" + 
				"client_id=" + CLIENT_ID + 
				"&redirect_uri=" + CALLBACK_URL +
				"&response_type=token";
		try {
			Desktop.getDesktop().browse(new URI(userGrantAccessUrl));
		} catch (IOException | URISyntaxException e) {}
		final String userAccessToken = getAccessToken();
		
		System.out.println("User access token: " + userAccessToken);
		
		//Step 2: get an app access Token
		final String appGrantAccessToken = makeRequest("oauth/access_token", "GET", 
				new HashMap<String, String>(){{
					put("client_id",CLIENT_ID);
					put("client_secret", CLIENT_SECRET);
					put("grant_type", "client_credentials");
				}})
				.replace("access_token=", "");
		System.out.println("App access token: " + appGrantAccessToken);
		
		//Step 3: verify the user token with our app token
		String verificationResponse = makeRequest("debug_token", "GET", 
				new HashMap<String, String>(){{
					put("input_token", userAccessToken);
					put("access_token", appGrantAccessToken);
				}});
		System.out.println("Status of token verification: " + verificationResponse);
		
		//Step 4: acccess the API
		//You'll need to provide the user access token with every request
		String userInformation = makeRequest("me", "GET",
				new HashMap<String, String>(){{
					put("access_token", userAccessToken);
				}});
		System.out.println("Here's some information about the user");
		System.out.println(userInformation);
	}
	private static String getAccessToken() {
		String accessToken = null;
		try{
			InputStreamReader converter = new InputStreamReader(System.in, "UTF-8");
			BufferedReader in = new BufferedReader(converter);
		
		
		
		while (accessToken == null) {
			System.out.println("Enter the access token from the callback URL:");
			try{
				accessToken = in.readLine();
			} catch (IOException e) {}
		}
		} catch (IOException e) {e.printStackTrace();}
		return accessToken;
	}
	
	private static String makeRequest(String operation, String method,
			Map<String, String> parameters) {
		NetHttpTransport transport = new NetHttpTransport();
		HttpRequestFactory factory;
		factory = transport.createRequestFactory();
		
		GenericUrl reqUrl = new GenericUrl(API_ENDPOINT_URL + operation);
		UrlEncodedContent content = null;
		if (parameters != null && method.equals("POST")) {
			content = new UrlEncodedContent(parameters);
		}
		if (parameters !=null && method.equals("GET")) {
			for(Entry<String, String> parameter : parameters.entrySet()) {
				reqUrl.put(parameter.getKey(), parameter.getValue());
			}
			
		}
		//If set, the access_token must be provided with GET parameter
		//Even when we're making  a POST request
		if(parameters != null && parameters.get("access_token") != null ) {
			reqUrl.put("access_token", parameters.get("access_token"));
		}
		HttpRequest req = null;
		
		try {
			
			req = factory.buildRequest(method, reqUrl, content);
			HttpResponse resp = req.execute();
			if(resp.isSuccessStatusCode()) {
				return resp.parseAsString();
			} else {
				System.err.println("REquest failed with status code " + resp.getStatusCode());
			}
		} catch( IOException e) {e.printStackTrace();}
		return null;
	
}
	
}
