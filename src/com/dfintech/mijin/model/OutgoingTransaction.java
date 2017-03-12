package com.dfintech.mijin.model;

import com.dfintech.mijin.utils.Constants;
import com.dfintech.mijin.utils.HttpClientUtils;

/** 
 * @Description: Query outgoing transactions
 * @author lu
 * @date 2017.03.06
 */ 
public class OutgoingTransaction {

	private String address = null;
	
	public OutgoingTransaction(String address){
		this.address = address;
	}
	
	public String query(){
		return HttpClientUtils.get(Constants.URL_ACCOUNT_TRANSFERS_OUTGOING + "?address=" + this.address);
	}
	
	public String query(long id){
		if(id==0){
			return HttpClientUtils.get(Constants.URL_ACCOUNT_TRANSFERS_OUTGOING + "?address=" + this.address);
		} else {
			return HttpClientUtils.get(Constants.URL_ACCOUNT_TRANSFERS_OUTGOING + "?address=" + this.address + "&id=" + id);
		}
	}
}
