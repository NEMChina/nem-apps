package com.dfintech.nem.apps.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/** 
 * @Description: file utils
 * @author lu
 * @date 2017.03.15
 */ 
public class FileUtils {

	/**
	 * read file content from file url
	 * @param fileUrl
	 * @return
	 */
	public static String readFromFileUrl(URL fileUrl){
		StringBuffer fileContent = new StringBuffer();
		InputStream in = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferReader = null;
		try {
			in = fileUrl.openStream();
			inputStreamReader = new InputStreamReader(fileUrl.openStream(), "utf-8");
			bufferReader = new BufferedReader(inputStreamReader);
	    	String lineTxt = null;
	    	while ((lineTxt = bufferReader.readLine()) != null) {
	    		fileContent.append(lineTxt).append("\n");
	    	}
		} catch (Exception ex) {
			// do nothing
		} finally {
			if(bufferReader!=null){
				try {
					bufferReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(inputStreamReader!=null){
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileContent.toString();
	}
	
	/**
	 * read file content from file
	 * @param path
	 * @return
	 */
	public static String readFromFile(String path){
		StringBuffer fileContent = new StringBuffer();
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferReader = null;
		try {
			fileInputStream = new FileInputStream(path);
			inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");
			bufferReader = new BufferedReader(inputStreamReader);
	    	String lineTxt = null;
	    	while ((lineTxt = bufferReader.readLine()) != null) {
	    		fileContent.append(lineTxt).append("\n");
	    	}
		} catch (Exception ex) {
			// do nothing
		} finally {
			if(bufferReader!=null){
				try {
					bufferReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(inputStreamReader!=null){
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fileInputStream!=null){
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileContent.toString();
	}
}
