package com.dfintech.mijin.model;

import java.util.Date;

import com.dfintech.mijin.utils.Constants;
import com.dfintech.mijin.utils.HttpClientUtils;

import net.sf.json.JSONObject;

/** 
 * @Description: Cosign multisig transaction
 * @author lu
 * @date 2017.03.05
 */ 
public class CosignMultisigTransaction {

	private String publicKey = null;
	private String privateKey = null;
	private String multisigAddress = null;
	private String innerTransactionHash = null;
	
	public CosignMultisigTransaction(String publicKey, String privateKey, String multisigAddress, String innerTransactionHash){
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.multisigAddress = multisigAddress;
		this.innerTransactionHash = innerTransactionHash;
	}
	
	public String send(){
		JSONObject params = new JSONObject();
		// otherHash object
		JSONObject otherHash = new JSONObject();
		otherHash.put("data", this.innerTransactionHash);
		// transaction object
		JSONObject transaction = new JSONObject();
		long nowTime = new Date().getTime();
		transaction.put("timeStamp", Double.valueOf(nowTime/1000).intValue() - Constants.NEMSISTIME);
		transaction.put("fee", 0);
		transaction.put("type", 4098);
		transaction.put("deadline", Double.valueOf(nowTime/1000).intValue() - Constants.NEMSISTIME + 60*60);
		transaction.put("version", 1610612737);
		transaction.put("signer", this.publicKey);
		transaction.put("otherHash", otherHash);
		transaction.put("otherAccount", this.multisigAddress);
		params.put("transaction", transaction);
		params.put("privateKey", this.privateKey);
		System.out.println(params.toString());
		return HttpClientUtils.post(Constants.URL_INIT_TRANSACTION, params.toString());
	}
}
