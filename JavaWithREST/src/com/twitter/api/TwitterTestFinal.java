package com.twitter.api;


import java.util.HashMap;



public class TwitterTestFinal {

	
	private static final String CONSUMER_KEY = "PDZJjSIhwpEU4PPi5bYDLlFEZ";
	private static final String CONSUMER_SECRET = "BWe1aIJXlIfb5RI2cUQZGrFGLfTziVRbXucVh7oDEAnAsJ7wfO";
	
	private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
	private static final String AUTHORIZE_URL ="https://api.twitter.com/oauth/authorize";
	private static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
	
	private static final String API_ENDPOINT_URL = "https://api.twitter.com/1.1/statuses/home_timeline.json";
	
	public static void main(String[] args) throws Exception {
		OAuthPostSignatureParametersProvider parametersProvider = 
				new OAuthPostSignatureParametersProvider("twitter",CONSUMER_KEY, CONSUMER_SECRET, REQUEST_TOKEN_URL
						, AUTHORIZE_URL, ACCESS_TOKEN_URL, true);
		TwitterRESTClient client = new TwitterRESTClient(parametersProvider);
		String jsonResponse;
		
		System.out.println("----- Get user time line -----");
		jsonResponse = client.makeRequest("statuses/home_timeline.json");
		System.out.println(jsonResponse);
		
		System.out.println("----- Get user time line: 5 tweets -----");
		jsonResponse = client.makeRequest("statuses/home_timeline.json",
		new HashMap<String, String>() {{
		put("count", "5");
		}});
		System.out.println(jsonResponse);
		
		System.out.println("----- Get user WileyTech's timeline: 5 tweets -----");
		jsonResponse = client.makeRequest("statuses/user_timeline.json",
		new HashMap<String, String>() {{
		put("screen_name", "WileyTech");
		put("count", "5");
		}});
		System.out.println(jsonResponse);
		System.out.println("----- Post a tweet -----");
		jsonResponse = client.makeRequest("statuses/update.json", "POST",
		new HashMap<String, String>() {{
		put("status", "Posting this Tweet from Java!" +
		" [" + System.currentTimeMillis() + "]");
		}});
		System.out.println(jsonResponse);
	}
}
