package com.dfintech.mijin.model;

import java.util.Date;

import com.dfintech.mijin.utils.Constants;
import com.dfintech.mijin.utils.FeeCalculateUtils;
import com.dfintech.mijin.utils.HexStringUtils;
import com.dfintech.mijin.utils.HttpClientUtils;

import net.sf.json.JSONObject;

/** 
 * @Description: Initiate a transaction
 * @author lu
 * @date 2017.03.02
 */ 
public class InitTransaction {

	private String publicKey = null;
	private String privateKey = null;
	
	public InitTransaction(String publicKey, String privateKey){
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	
	public String send(String recipient, long amount, String messagePayload){
		//parameter object
		JSONObject params = new JSONObject();
		//inner message object
		JSONObject message = new JSONObject();
		message.put("payload", HexStringUtils.string2Hex(messagePayload));
		message.put("type", 1);
		//inner transaction object
		JSONObject transaction = new JSONObject();
		long nowTime = new Date().getTime();
		long fee = FeeCalculateUtils.calculateMinFeeNoMosaic(amount, messagePayload);
		transaction.put("timeStamp", Double.valueOf(nowTime/1000).intValue() - Constants.NEMSISTIME);
		transaction.put("amount", amount * Constants.MICRONEMS_IN_NEM);
		transaction.put("fee", fee * Constants.MICRONEMS_IN_NEM);
		transaction.put("recipient", recipient);
		transaction.put("type", 257);
		transaction.put("deadline", Double.valueOf(nowTime/1000).intValue() - Constants.NEMSISTIME + 60*60 - 1);
		transaction.put("version", 1610612737);
		transaction.put("signer", this.publicKey);
		transaction.put("message", message);
		params.put("transaction", transaction);
		params.put("privateKey", this.privateKey);
		return HttpClientUtils.post(Constants.URL_INIT_TRANSACTION, params.toString());
	}
}
