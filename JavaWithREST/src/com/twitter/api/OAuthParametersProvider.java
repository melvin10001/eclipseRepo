package com.twitter.api;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;


public class OAuthParametersProvider {
	protected final String configurationName;
	
	protected final String consumerKey;
	protected final String consumerSecret;
	
	protected final String requestTokenUrl;
	protected final String authorizeUrl;
	protected final String accessTokenUrl;
	
	protected final boolean requiresVerification;
	
	protected final HttpTransport transport;
	protected final OAuthHmacSigner signer;
	
	public OAuthParametersProvider(String configurationName, String consumerKey, String consumerSecret,

			String requestTokenUrl, String authorizeUrl, String accessTokenUrl, boolean requiresVerification) {
		this.configurationName = configurationName;
		this.consumerKey = consumerKey;
		this.consumerSecret	= consumerSecret;
		this.requestTokenUrl = requestTokenUrl;
		this.authorizeUrl = authorizeUrl;
		this.accessTokenUrl = accessTokenUrl;
		this.requiresVerification = requiresVerification;
		
		this.transport = new NetHttpTransport();
		this.signer = new OAuthHmacSigner();
		this.signer.clientSharedSecret = consumerSecret;
				
	}
	
	public OAuthParameters getOAuthParameters() {
		OAuthParameters parameters = new OAuthParameters();
		parameters.consumerKey = consumerKey;
		OAuthCredentialsResponse accessToken = getAccessToken();
		parameters.token = accessToken.token;
		
		//Construct a new signer
		OAuthHmacSigner requestSigner = new OAuthHmacSigner();
		requestSigner.clientSharedSecret = consumerSecret;
		requestSigner.tokenSharedSecret = accessToken.tokenSecret;
		
		parameters.signer = requestSigner;
		
		return parameters;
	}
	public OAuthCredentialsResponse getAccessToken() {
		return getAccessToken(false);
	}
	public OAuthCredentialsResponse getAccessToken(boolean forceNewToken) {
		
		OAuthCredentialsResponse token = getStoredAccessToken();
		if (!forceNewToken && token != null) {
			return token;
		}
		OAuthCredentialsResponse requestToken = getRequestToken();
		OAuthCredentialsResponse accessToken = getAccessToken(requestToken);
		
		return accessToken;
	}
	
	protected OAuthCredentialsResponse getStoredAccessToken() {
		String path = "tokens\\" + configurationName+".txt";
		if( Files.notExists(Paths.get(path)))
			return null;
		try {
			List<String> lines = Files.readAllLines(Paths.get(path),
					StandardCharsets.UTF_8);
			OAuthCredentialsResponse accessToken = new OAuthCredentialsResponse();
			accessToken.token = lines.get(0);
			accessToken.tokenSecret = lines.get(1);
			
			return accessToken;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected String getStoredVerifier() {
		String path = "tokens\\" + configurationName + ".txt";
		if(!Files.exists(Paths.get(path)))
				return null;
		
		try{
			List<String> lines = Files.readAllLines(Paths.get(path),
					StandardCharsets.UTF_8);
			return lines.get(2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected void storeAccessTokenAndVerifier(OAuthCredentialsResponse accessToken,
			String verifier) {
		String path = "tokens" + "\\" + configurationName + ".txt";
		try(PrintWriter writer = new PrintWriter(path, "UTF-8")) {
			writer.println(accessToken.token);
			writer.println(accessToken.tokenSecret);
			writer.println(verifier);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	protected OAuthCredentialsResponse getRequestToken(){
		signer.tokenSharedSecret = null;
		
		OAuthGetTemporaryToken requestToken =
				new OAuthGetTemporaryToken(requestTokenUrl);
		requestToken.consumerKey = consumerKey;
		requestToken.transport = transport;
		requestToken.signer = signer;
		
		try {
			OAuthCredentialsResponse requestTokenResponse = requestToken.execute();
			return requestTokenResponse;
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	protected OAuthCredentialsResponse getAccessToken(
			OAuthCredentialsResponse requestToken) {
		signer.tokenSharedSecret = requestToken.tokenSecret;
		OAuthAuthorizeTemporaryTokenUrl oAuthAuthorizeUrl = 
				new OAuthAuthorizeTemporaryTokenUrl(authorizeUrl);
		oAuthAuthorizeUrl.temporaryToken = requestToken.token;
		System.out.println("Go to the following link in your browser:\n" + 
		 oAuthAuthorizeUrl.build());
		try {
			//Try to open browser automatically
			Desktop.getDesktop().browse(new URI(oAuthAuthorizeUrl.build()));
		} catch (IOException | URISyntaxException e1) {
			e1.printStackTrace();
		}
		String verifier = null;
		if (requiresVerification)
			verifier = getVerificationCode();
		
		OAuthGetAccessToken accessToken = new OAuthGetAccessToken(accessTokenUrl);
		accessToken.consumerKey = consumerKey;
		accessToken.signer = signer;
		accessToken.transport = transport;
		accessToken.temporaryToken = requestToken.token;
		if(requiresVerification)
			accessToken.verifier = verifier;
		
		OAuthCredentialsResponse accessTokenResponse;
		try{
			accessTokenResponse = accessToken.execute();
			storeAccessTokenAndVerifier(accessTokenResponse, verifier);
			return accessTokenResponse;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	protected String getVerificationCode() {
		String verifier = null;
		try {
			InputStreamReader converter = new InputStreamReader(System.in, "UTF-8");
		
		BufferedReader in = new BufferedReader(converter);
		
		while (verifier == null) {
			System.out.println("Enter the verification code provided by the service:");
			
				verifier = in.readLine();
		}
		} catch(IOException e) {e.printStackTrace();}
		return verifier;
	}
}
