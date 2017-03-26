package com.dfintech.nem.apps.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/** 
 * @Description: date utils  
 * @author lu
 * @date 2017.03.24
 */ 
public class DateUtils {

	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String nemToRealDateStr(long timeStamp) {
		return dateFormat.format(new Date((timeStamp + Constants.NEMSISTIME)*1000));
	}
}
