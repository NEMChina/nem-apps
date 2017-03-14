package com.dfintech.nem.apps.utils;

/** 
 * @Description: Transaction Fee calculate Utils
 * @author lu
 * @date 2017.03.14
 */ 
public class FeeCalculateUtils {

	public static long calculateMinFeeNoMosaic(long amount, String message) {
		if(Constants.NETWORK_TYPE==2){ //mijinnet
			return 0;
		} else {
			long messageFee = (null == message) ? 0 : message.length() / 32 + 1;
			long transferFee = Math.min(25, Math.max(1L, amount * Constants.MICRONEMS_IN_NEM / 10000L));
			return messageFee + transferFee;
		}
	}
}
