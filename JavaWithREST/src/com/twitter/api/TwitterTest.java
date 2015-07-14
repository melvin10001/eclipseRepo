package com.twitter.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class TwitterTest {
	private static final String CONSUMER_KEY = "PDZJjSIhwpEU4PPi5bYDLlFEZ";
	private static final String CONSUMER_SECRET = "BWe1aIJXlIfb5RI2cUQZGrFGLfTziVRbXucVh7oDEAnAsJ7wfO";
	
	private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
	private static final String AUTHORIZE_URL ="https://api.twitter.com/oauth/authorize";
	private static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
	
	private static final String API_ENDPOINT_URL = "https://api.twitter.com/1.1/statuses/home_timeline.json";
	
	public static void main(String[] args) throws Exception {
		
		//Http transport will be used to handle http requests
		//This is part of the google-http library
		
		HttpTransport transport = new NetHttpTransport();
		
		//The OAuthHmacSigner will be used to create the oauth_method
		//Using HMAC-SHA1 as the oauth_signature_method
		//The signer needs the secret key to sign request
		OAuthHmacSigner signer  = new OAuthHmacSigner();
		signer.clientSharedSecret = CONSUMER_SECRET;
		
		
		//Step 1: get request token:
		//-------------------------
		
		// We need to provide our application key
		//we also need to provide http transport object
		//And the signer which will sign the request
		OAuthGetTemporaryToken requestToken = 
				new OAuthGetTemporaryToken(REQUEST_TOKEN_URL);
		requestToken.consumerKey = CONSUMER_KEY;
		requestToken.transport = transport;
		requestToken.signer = signer;
		
		//Get back our request token
		OAuthCredentialsResponse requestTokenResponse = requestToken.execute();
		
		System.out.println("Request Token: ");
		System.out.println("- oauth_token        = " + requestTokenResponse.token);
		System.out.println("- oauth_token_secret = " + requestTokenResponse.tokenSecret); 
		
		//update signer to also include the request token
		signer.tokenSharedSecret = requestTokenResponse.tokenSecret;
		
		// Step 2 User grants access
		//-------------------------
		
		//Construct an authoriaztion url using the temporrary request token
		OAuthAuthorizeTemporaryTokenUrl  authorizeUrl =
				new OAuthAuthorizeTemporaryTokenUrl(AUTHORIZE_URL);
		authorizeUrl.temporaryToken = requestTokenResponse.token;
		
		//We ask the user to open url and grant acces
		//Twitter includes extra safety measures, asks the user to provide PIN
		String pin = null;
		System.out.println("Go to the following link:\n" + authorizeUrl.build());
		InputStreamReader converter = new InputStreamReader(System.in, "UTF-8");
		BufferedReader in = new BufferedReader(converter);
		while (pin == null){
			System.out.println("Enter the verification Pin provided by twitter: ");
			pin = in.readLine();
			
			
		
		
		
		}
		//Step 3 Request the access token the user has approved
		//------------------------------------------------------------
		
		//Get the acces token
		//We need to provide our application key
		//The signer, the transport objects
		//The temporary request token
		//And a verifier string (the PIN number provided by Twitter)
		OAuthGetAccessToken accessToken = new OAuthGetAccessToken(ACCESS_TOKEN_URL);
		accessToken.consumerKey = CONSUMER_KEY;
		accessToken.signer = signer;
		accessToken.transport = transport;
		accessToken.temporaryToken = requestTokenResponse.token;
		accessToken.verifier = pin;
		
		//get back to our access token
		OAuthCredentialsResponse accessTokenResponse = accessToken.execute();
		
		System.out.println("Access Token:");
		System.out.println("- oauth_token        = " + accessTokenResponse.token);
		System.out.println("- oauth_token_secret = " + accessTokenResponse.tokenSecret);
		
		//Update the signer again
		//We now replace the temporary request token with the final access token
		signer.tokenSharedSecret = accessTokenResponse.tokenSecret;
		
		
		//set up OAuth parameters which can now be used in authenticated requests
		OAuthParameters parameters = new OAuthParameters();
		parameters.consumerKey = CONSUMER_KEY;
		parameters.token = accessTokenResponse.token;
		parameters.signer = signer;
		
		//OAuth steps finished, we can now start accessing the service
		//------------------------------------------------------------
		
		HttpRequestFactory factory = transport.createRequestFactory(parameters);
		GenericUrl url = new GenericUrl(API_ENDPOINT_URL);
		HttpRequest req = factory.buildGetRequest(url);
		HttpResponse resp = req.execute();
		
		
		System.out.println(resp.getStatusCode());
		System.out.println(resp.parseAsString());
		
				
		
		
		
	}
}
