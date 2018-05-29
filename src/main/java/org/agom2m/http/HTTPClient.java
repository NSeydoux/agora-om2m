package org.agom2m.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HTTPClient {
	private CloseableHttpClient httpclient;
	
	private static String authenticationHeader = "Basic b3BhLWVucmljaGVyOk9QQS1lbnJpY2htZW50LTRsaWZl";
	private static Map<String, String> defaultHeaders;
	
	private static final String prefixesURL = "http://localhost:80/prefixes";
	private static final String discoveryURL = "http://localhost:80/discover?strict&mean";
	
	
	static {
		defaultHeaders = new HashMap<String, String>();
		defaultHeaders.put("Authorization", authenticationHeader);
	}
	
	public HTTPClient(){
		this.httpclient = HttpClients.createDefault();
	}
	
	public String get(String url) throws ClientProtocolException, IOException{
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("Accept", "application/json");
//		httpGet.addHeader("Authorization", authenticationHeader);
		CloseableHttpResponse response = httpclient.execute(httpGet);
		HttpEntity entity = null;
		entity = response.getEntity();
	    return EntityUtils.toString(entity);
	}
	
	public StatusLine post(String url, String body, Map<String, String> headers) throws ClientProtocolException, IOException{
		HttpPost httpPost = new HttpPost(url);
		for(Entry<String, String> header:headers.entrySet()){
			httpPost.addHeader(header.getKey(), header.getValue());
		}
		httpPost.setEntity(new StringEntity(body));
		CloseableHttpResponse response = httpclient.execute(httpPost);
		HttpEntity entity = null;
	    try {
	        entity = response.getEntity();
	    }
	    finally {
	        response.close();
	    }
	    return response.getStatusLine();
	}
	
	public String executeDiscoveryQuery(String query){
		HttpPost httpPost = new HttpPost(discoveryURL);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/ld+json");
		for(Entry<String, String> header:headers.entrySet()){
			httpPost.addHeader(header.getKey(), header.getValue());
		}
		CloseableHttpResponse response = null;
		String discoveryResult=null;
		try {
			httpPost.setEntity(new StringEntity(query));
			response = httpclient.execute(httpPost);
			discoveryResult = null;
			discoveryResult = Util.convertStreamToString(response.getEntity().getContent());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	        try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    return discoveryResult;
	}
	
	public StatusLine post(String url, String body) throws ClientProtocolException, IOException{
	    return this.post(url, body, defaultHeaders);
	}
	
	public String retrievePrefixes(){
		try {
			return this.get(prefixesURL);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}