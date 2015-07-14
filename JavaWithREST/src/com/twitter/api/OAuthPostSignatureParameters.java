package com.twitter.api;

import com.google.api.client.auth.oauth.OAuthSigner;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.escape.PercentEscaper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public final class OAuthPostSignatureParameters
	 implements HttpExecuteInterceptor, HttpRequestInitializer {

	/*
	 * Due to a limitation in OAuthParameters, form parameters (as those
	 * used by POST requests) are not included in the construction of the
	 * OAuth signature. As such, we build a new class based on the source
	 * code of OAuthParameters to work around this issue.
	 *
	 * Note that, normally, we would write a superclass which extends
	 * OAuthParameters, but since OAuthParameters is declared as a final
	 * class, we cannot do so here.
	 */

	private static final SecureRandom RANDOM = new SecureRandom();
	public OAuthSigner signer;
	public String callback;
	public String consumerKey;
	public String nonce;
	public String realm;
	public String signature;
	public String signatureMethod;
	public String timestamp;
	public String token;
	public String verifier;
	public String version;

	private static final PercentEscaper ESCAPER =
		new PercentEscaper("-_.~", false);

	public void computeNonce() {
		nonce = Long.toHexString(Math.abs(RANDOM.nextLong()));
	}

	public void computeTimestamp() {
		timestamp = Long.toString(System.currentTimeMillis() / 1000);
	}

	public void computeSignature(String requestMethod, GenericUrl requestUrl,
	  HttpContent httpContent)
		  throws GeneralSecurityException {
	  OAuthSigner signer = this.signer;
	  String signatureMethod = this.signatureMethod = signer.getSignatureMethod();

	  TreeMap<String, String> parameters = new TreeMap<String, String>();

	  // Include all OAuth values in the signature
	  putParameterIfValueNotNull(parameters, "oauth_callback", callback);
	  putParameterIfValueNotNull(parameters, "oauth_consumer_key", consumerKey);
	  putParameterIfValueNotNull(parameters, "oauth_nonce", nonce);
	  putParameterIfValueNotNull(parameters, "oauth_signature_method",
		  signatureMethod);
	  putParameterIfValueNotNull(parameters, "oauth_timestamp", timestamp);
	  putParameterIfValueNotNull(parameters, "oauth_token", token);
	  putParameterIfValueNotNull(parameters, "oauth_verifier", verifier);
	  putParameterIfValueNotNull(parameters, "oauth_version", version);

	  // Include URL query parameters
	  for (Map.Entry<String, Object> fieldEntry : requestUrl.entrySet()) {
		  Object value = fieldEntry.getValue();
		  if (value != null) {
			  String name = fieldEntry.getKey();
			  if (value instanceof Collection<?>) {
				  for (Object repeatedValue : (Collection<?>) value) {
					  putParameter(parameters, name, repeatedValue);
				  }
			  } else {
				  putParameter(parameters, name, value);
			  }
		  }
	  }

		// Include postdata parameters (added in our implementation)
		if (httpContent != null && httpContent instanceof UrlEncodedContent) {
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) 
			   ((UrlEncodedContent)httpContent).getData();
			for (Map.Entry<String, Object> dataEntry : data.entrySet()) {
				Object value = dataEntry.getValue();
				if (value != null) {
					String name = dataEntry.getKey();
					if (value instanceof Collection<?>) {
						for (Object repeatedValue : (Collection<?>) value) {
							putParameter(parameters, name, repeatedValue);
						}
					} else {
						putParameter(parameters, name, value);
					}
				}
			}
		}

		// Normalize parameters
		StringBuilder parametersBuf = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (first) {
				first = false;
			} else {
				parametersBuf.append('&');
			}
			parametersBuf.append(entry.getKey());
			String value = entry.getValue();
			if (value != null) {
				parametersBuf.append('=').append(value);
			}
		}
		String normalizedParameters = parametersBuf.toString();

		// Normalize URL
		GenericUrl normalized = new GenericUrl();
		String scheme = requestUrl.getScheme();
		normalized.setScheme(scheme);
		normalized.setHost(requestUrl.getHost());
		normalized.setPathParts(requestUrl.getPathParts());
		int port = requestUrl.getPort();
		if ("http".equals(scheme) && port == 80 || "https".equals(scheme)
				&& port == 443) {
			port = -1;
		}
		normalized.setPort(port);
		String normalizedPath = normalized.build();

		// Construct signature base string
		StringBuilder buf = new StringBuilder();
		buf.append(escape(requestMethod)).append('&');
		buf.append(escape(normalizedPath)).append('&');
		buf.append(escape(normalizedParameters));
		String signatureBaseString = buf.toString();
		signature = signer.computeSignature(signatureBaseString);
	}

	public String getAuthorizationHeader() {
		StringBuilder buf = new StringBuilder("OAuth");
		appendParameter(buf, "realm", realm);
		appendParameter(buf, "oauth_callback", callback);
		appendParameter(buf, "oauth_consumer_key", consumerKey);
		appendParameter(buf, "oauth_nonce", nonce);
		appendParameter(buf, "oauth_signature", signature);
		appendParameter(buf, "oauth_signature_method", signatureMethod);
		appendParameter(buf, "oauth_timestamp", timestamp);
		appendParameter(buf, "oauth_token", token);
		appendParameter(buf, "oauth_verifier", verifier);
		appendParameter(buf, "oauth_version", version);
		return buf.substring(0, buf.length() - 1);
	}

	private void appendParameter(StringBuilder buf, String name, String value) {
		if (value != null) {
			buf.append(' ').append(escape(name)).append("=\"")
					.append(escape(value)).append("\",");
		}
	}

	private void putParameterIfValueNotNull(TreeMap<String, String> parameters,
			String key, String value) {
		if (value != null) {
			putParameter(parameters, key, value);
		}
	}

	private void putParameter(TreeMap<String, String> parameters, String key,
			Object value) {
		parameters.put(escape(key),
				value == null ? null : escape(value.toString()));
	}

	public static String escape(String value) {
		return ESCAPER.escape(value);
	}

	public void initialize(HttpRequest request) throws IOException {
		request.setInterceptor(this);
	}

	public void intercept(HttpRequest request) throws IOException {
		computeNonce();
		computeTimestamp();
		try {
			computeSignature(request.getRequestMethod(), request.getUrl(), 
			   request.getContent());
		} catch (GeneralSecurityException e) {
			IOException io = new IOException();
			io.initCause(e);
			throw io;
		}
		request.getHeaders().setAuthorization(getAuthorizationHeader());
	}
}