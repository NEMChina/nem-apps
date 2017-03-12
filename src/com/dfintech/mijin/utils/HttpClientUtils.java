package com.dfintech.mijin.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


/** 
 * @Description: Http Utils
 * @author lu
 * @date 2017.03.07
 */ 
@SuppressWarnings("deprecation")
public class HttpClientUtils {

	private static String mijinHost = null;
	private static String mijinPort = null;
	private static final String CONTENT_TYPE_TEXT_JSON = "application/json";
	
	/**
	 * Http Post
	 * @param requestUrl
	 * @param params
	 * @return
	 */
	public static String post(String requestUrl, String params){
		loadProperties();
		String result = "";
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = "http://" + mijinHost + ":" + mijinPort + requestUrl;
        HttpPost method = new HttpPost(url);
        try {
            if (params!=null) {
                StringEntity entity = new StringEntity(params);
                entity.setContentType(CONTENT_TYPE_TEXT_JSON);
                method.setEntity(entity);
            }
            HttpResponse response = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            result = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	httpClient.close();
        }
        return result;
	}
	
	/**
	 * Http Get
	 * @param requestUrl
	 * @return
	 */
	public static String get(String requestUrl){
		loadProperties();
		String result = "";
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = "http://" + mijinHost + ":" + mijinPort + requestUrl;
        HttpGet method = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            result = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	httpClient.close();
        }
        return result;
	}
	
	/**
	 * load some properties from the file
	 */
	private static void loadProperties(){
		if(mijinHost==null || mijinPort==null){
			InputStream in = null;
			FileInputStream fileInputStream = null;
			try {
				String proFilePath = System.getProperty("user.dir") + "/config.properties";
				fileInputStream = new FileInputStream(proFilePath);
				in = new BufferedInputStream(fileInputStream);
				Properties prop = new Properties();
				prop.load(in);
				mijinHost = prop.getProperty("mijin.host");
				mijinPort = prop.getProperty("mijin.port");
			} catch (Exception ex) {
				ex.printStackTrace();
				mijinHost = "127.0.0.1";
				mijinPort = "7895";
			} finally {
				try {
					if(in!=null){
						in.close();
					}
					if(fileInputStream!=null){
						fileInputStream.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
