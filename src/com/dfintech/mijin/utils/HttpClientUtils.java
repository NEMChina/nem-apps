package com.dfintech.mijin.utils;

import java.io.IOException;
import java.net.URLDecoder;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


/** 
 * @Description: Http Utils
 * @author lu
 * @date 2017.03.07
 */ 
public class HttpClientUtils {
	
	public static String defaultHost = "127.0.0.1";
	public static String defaultPort = "7895";

	/**
	 * Http Post
	 * @param requestUrl
	 * @param params
	 * @return
	 */
	public static String post(String requestUrl, String params){
		String result = "";
		CloseableHttpClient httpClient = HttpClients.createDefault();
        String url = "http://" + defaultHost + ":" + defaultPort + requestUrl;
        HttpPost method = new HttpPost(url);
        CloseableHttpResponse response = null;
        try {
            if (params!=null) {
                StringEntity entity = new StringEntity(params);
                entity.setContentType(Constants.CONTENT_TYPE_TEXT_JSON);
                method.setEntity(entity);
            }
            response = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            result = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	try {
        		if(response!=null){
        			response.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return result;
	}
	
	/**
	 * Http Get
	 * @param requestUrl
	 * @return
	 */
	public static String get(String requestUrl){
		String result = "";
		CloseableHttpClient httpClient = HttpClients.createDefault();
        String url = "http://" + defaultHost + ":" + defaultPort + requestUrl;
        HttpGet method = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            result = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	try {
        		if(response!=null){
        			response.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return result;
	}
}
