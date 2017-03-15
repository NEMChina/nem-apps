package com.dfintech.nem.apps.utils;

import java.net.URL;

import com.dfintech.nem.apps.ImplInitTransaction;

/** 
 * @Description: helper utils
 * @author lu
 * @date 2017.03.15
 */ 
public class HelperUtils {

	/**
	 * print helper 
	 * @return
	 */
	public static String printHelper(String fileName){
		String filePath = System.getProperty("user.dir") + "/docs/" + fileName;
		String fielContent = FileUtils.readFromFile(filePath);
		if(!"".equals(fielContent)){
			return fielContent;
		}
		URL fileUrl = ImplInitTransaction.class.getClassLoader().getResource(fileName);
		if(fileUrl!=null){
			filePath = fileUrl.getPath();
			fielContent = FileUtils.readFromFileUrl(fileUrl);
		}
		return fielContent;
	}
	
}
