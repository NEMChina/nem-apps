package com.dfintech.nem.apps.utils;

/** 
 * @Description: output message utils
 * @author lu
 * @date 2017.03.10
 */ 
public class OutputMessage {

	public static void success(String message) {
		StringBuffer out = new StringBuffer();
		out.append("{");
		out.append("\"flag\":\"success\"");
		if(message!=null){
			out.append(",");
			out.append(message);
		}
		out.append("}");
		System.out.println(out.toString());
	}
	
	public static void error(String message) {
		StringBuffer out = new StringBuffer();
		out.append("{");
		out.append("\"flag\":\"error\"");
		out.append(",\"message\":\"");
		out.append(message);
		out.append("\"}");
		System.out.println(out.toString());
	}
	
	public static void initTransactionMessage(String flag, String transactionHash) {
		StringBuffer out = new StringBuffer();
		out.append("{");
		out.append("\"flag\":\"");
		out.append(flag);
		out.append("\",");
		out.append("\"transactionHash\":\"");
		out.append(transactionHash);
		out.append("\"}");
		System.out.println(out.toString());
	}
	
	public static void initMultisigTransactionMessage(String flag, String transactionHash, String innerTransactionHash) {
		StringBuffer out = new StringBuffer();
		out.append("{");
		out.append("\"flag\":\"");
		out.append(flag);
		out.append("\",");
		out.append("\"transactionHash\":\"");
		out.append(transactionHash);
		out.append("\",");
		out.append("\"innerTransactionHash\":\"");
		out.append(innerTransactionHash);
		out.append("\"}");
		System.out.println(out.toString());
	}
	
	public static void initCosignTransactionMessage(String flag, String transactionHash) {
		StringBuffer out = new StringBuffer();
		out.append("{");
		out.append("\"flag\":\"");
		out.append(flag);
		out.append("\",");
		out.append("\"transactionHash\":\"");
		out.append(transactionHash);
		out.append("\"}");
		System.out.println(out.toString());
	}
	
}
