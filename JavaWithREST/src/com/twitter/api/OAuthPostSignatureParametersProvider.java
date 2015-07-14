package com.twitter.api;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthHmacSigner;


public class OAuthPostSignatureParametersProvider extends OAuthParametersProvider {
	
	public OAuthPostSignatureParametersProvider(String configurationName, String consumerKey, String consumerSecret,
			String requestTokenUrl, String authorizeUrl, String accessTokenUrl,
			boolean requiresVerification) {
		super(configurationName, consumerKey, consumerSecret, requestTokenUrl, authorizeUrl, accessTokenUrl, requiresVerification);
	}
	
	public OAuthPostSignatureParameters getOAuthPostSignatureParameters() {
		OAuthPostSignatureParameters parameters = new OAuthPostSignatureParameters();
		parameters.consumerKey = consumerKey;
		OAuthCredentialsResponse accessToken = getAccessToken();
		parameters.token = accessToken.token;
		
		
		//twitter is sometimes picky on reqiring a callback in every request
		//As well as the original verifier
		parameters.callback = "http://127.0.0.1";
		if (requiresVerification)
			parameters.verifier = getStoredVerifier();
		
		//Construct a new signer
		OAuthHmacSigner requestSigner = new OAuthHmacSigner();
		requestSigner.clientSharedSecret = consumerSecret;
		requestSigner.tokenSharedSecret = accessToken.tokenSecret;
		
		parameters.signer = requestSigner;
		
		return parameters;
	}
}
