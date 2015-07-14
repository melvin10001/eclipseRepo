package com.twitter.api;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;


public class TwitterRESTClient {
	
	private static final String API_ENDPOINT_URL = "https://api.twitter.com/1.1/";
	
	
	private final HttpTransport transport;
	private final OAuthPostSignatureParametersProvider parametersProvider;
	
	public TwitterRESTClient(OAuthPostSignatureParametersProvider parametersProvider) {
		
		this.transport = new NetHttpTransport();
		this.parametersProvider = parametersProvider;
		
	}
	public String makeRequest(String operation) {
		return makeRequest(operation, "GET");
	}
	public String makeRequest(String operation, String method) {
		return makeRequest(operation, method, true, null);
	}
	public String makeRequest(String operation, Map<String, String> parameters) {
		return makeRequest(operation, "GET", true, parameters);
	}
	public String makeRequest(String operation, String method, 
			Map<String, String> parameters) {
		return makeRequest(operation, method, true, parameters);
	}
	public String makeRequest(String operation, String method, boolean useOAuth,
			Map<String, String> parameters) {
		HttpRequestFactory factory;
		if(useOAuth)
			factory = transport.createRequestFactory(
					parametersProvider.getOAuthPostSignatureParameters());
		else
			factory = transport.createRequestFactory();
		String url = API_ENDPOINT_URL + operation;
		GenericUrl reqUrl = new GenericUrl(url);
		UrlEncodedContent content = null;
		if (parameters != null && method.equals("POST") )
			content = new UrlEncodedContent(parameters);
		if (parameters != null && method.equals("GET"))
			for (Entry<String, String> parameter : parameters.entrySet())
				reqUrl.put(parameter.getKey(),parameter.getValue() );
		
		HttpRequest req = null;
		try {
			req = factory.buildRequest(method, reqUrl, content);
			HttpResponse resp = req.execute();
			if (resp.isSuccessStatusCode()) {
				return resp.parseAsString();
			} else {
				System.err.println("Request failed with status code: "
						+ resp.getStatusCode());
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
