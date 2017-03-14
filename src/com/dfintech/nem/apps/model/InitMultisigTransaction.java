package com.dfintech.nem.apps.model;

import java.util.Date;

import com.dfintech.nem.apps.utils.Constants;
import com.dfintech.nem.apps.utils.FeeCalculateUtils;
import com.dfintech.nem.apps.utils.HexStringUtils;
import com.dfintech.nem.apps.utils.HttpClientUtils;

import net.sf.json.JSONObject;

/** 
 * @Description: Initiate a multisig transaction
 * @author lu
 * @date 2017.03.02
 */ 
public class InitMultisigTransaction {

	private String publicKey = null;
	private String privateKey = null;
	private String multisigPublicKey = null;
	
	public InitMultisigTransaction(String publicKey, String privateKey, String multisigPublicKey){
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.multisigPublicKey = multisigPublicKey;
	}
	
	public String send(String recipient, long amount, String messagePayload){
		JSONObject params = new JSONObject();
		// message object
		JSONObject message = new JSONObject();
		message.put("payload", HexStringUtils.string2Hex(messagePayload));
		message.put("type", 1);
		// otherTrans object
		JSONObject otherTrans = new JSONObject();
		long nowTime = new Date().getTime();
		long fee = FeeCalculateUtils.calculateMinFeeNoMosaic(amount, messagePayload);
		otherTrans.put("timeStamp", Double.valueOf(nowTime/1000).intValue() - Constants.NEMSISTIME);
		otherTrans.put("amount", amount * Constants.MICRONEMS_IN_NEM);
		otherTrans.put("fee", fee * Constants.MICRONEMS_IN_NEM);
		otherTrans.put("recipient", recipient);
		otherTrans.put("type", 257);
		otherTrans.put("deadline", Double.valueOf(nowTime/1000).intValue() - Constants.NEMSISTIME + 60*60 - 1);
		otherTrans.put("version", 1610612737);
		otherTrans.put("signer", this.multisigPublicKey);
		otherTrans.put("message", message);
		// transaction object
		JSONObject transaction = new JSONObject();
		transaction.put("timeStamp", Double.valueOf(nowTime/1000).intValue() - Constants.NEMSISTIME);
		transaction.put("fee", 0);
		transaction.put("type", 4100);
		transaction.put("deadline",  Double.valueOf(nowTime/1000).intValue() - Constants.NEMSISTIME + 60*60 - 1);
		transaction.put("version", 1610612737);
		transaction.put("signer", this.publicKey);
		transaction.put("signatures", "[]");
		transaction.put("otherTrans", otherTrans);
		params.put("transaction", transaction);
		params.put("privateKey", this.privateKey);
		return HttpClientUtils.post(Constants.URL_INIT_TRANSACTION, params.toString());
	}
	
}
