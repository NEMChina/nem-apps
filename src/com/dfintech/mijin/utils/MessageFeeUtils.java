package com.dfintech.mijin.utils;

/** 
 * @Description: Message Fee Utils
 * @author lu
 * @date 2017.03.07
 */ 
public class MessageFeeUtils {

	public static int calculateFee(String message) {
		if(message.length()==0){
			return 0;
		}
		return 2 * Math.max(1, message.length() / 16);
	}
}
