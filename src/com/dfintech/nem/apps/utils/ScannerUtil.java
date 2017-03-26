package com.dfintech.nem.apps.utils;

import java.util.Scanner;

/** 
 * @Description: Scanner utils
 * @author lu
 * @date 2017.03.24
 */ 
public class ScannerUtil {

	/**
	 * exit when enter string "exit"
	 */
	public static void monitorExit() {
		Scanner scanner = new Scanner(System.in);
		try{
	        while (true) { 
		        String line = scanner.nextLine();
		        if("exit".equals(line)){
		        	break;
		        }
	        }
		} catch (Exception ex) {
			
		} finally {
			scanner.close();
		}
	}
}
