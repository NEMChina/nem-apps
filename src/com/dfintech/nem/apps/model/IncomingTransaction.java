package com.dfintech.nem.apps.model;

import com.dfintech.nem.apps.utils.Constants;
import com.dfintech.nem.apps.utils.HttpClientUtils;

/** 
 * @Description: Query incoming transactions
 * @author lu
 * @date 2017.03.06
 */ 
public class IncomingTransaction {

	private String address = null;
	
	public IncomingTransaction(String address){
		this.address = address;
	}
	
	public String query(){
		return HttpClientUtils.get(Constants.URL_ACCOUNT_TRANSFERS_INCOMING + "?address=" + this.address);
	}
	
	public String query(long id){
		if(id==0){
			return HttpClientUtils.get(Constants.URL_ACCOUNT_TRANSFERS_INCOMING + "?address=" + this.address);
		} else {
			return HttpClientUtils.get(Constants.URL_ACCOUNT_TRANSFERS_INCOMING + "?address=" + this.address + "&id=" + id);
		}
	}
}
