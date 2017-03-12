package com.dfintech.mijin.model;

import com.dfintech.mijin.utils.Constants;
import com.dfintech.mijin.utils.HttpClientUtils;

/** 
 * @Description: Query unconfirmed transactions
 * @author lu
 * @date 2017.03.06
 */ 
public class UnconfirmedTransaction {

	private String address = null;
	
	public UnconfirmedTransaction(String address){
		this.address = address;
	}
	
	public String query(){
		return HttpClientUtils.get(Constants.URL_ACCOUNT_UNCONFIRMEDTRANSACTIONS + "?address=" + this.address);
	}
}
