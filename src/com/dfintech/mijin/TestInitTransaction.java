package com.dfintech.mijin;

import org.apache.commons.lang.StringUtils;

import com.dfintech.mijin.model.InitTransaction;
import com.dfintech.mijin.utils.Constants;
import com.dfintech.mijin.utils.HttpClientUtils;
import com.dfintech.mijin.utils.OutputMessage;

import net.sf.json.JSONObject;

/** 
 * @Description: Main class - test init transaction
 * @author lu
 * @date 2017.03.10
 */ 
public class TestInitTransaction {

	public static void main(String[] args) {
		if(args.length==0){
			OutputMessage.error("please enter json parameter");
			return;
		}
		JSONObject params = convertString2JSON(args[0]);
		if(params==null){
			return;
		}
		// get publicKey from NIS
		queryPublicKeyFromNIS(params);
		// send transaction
		String publicKey = params.getString("publicKey");
		String privateKey = params.getString("privateKey");
		int amount = params.getInt("amount");
		String recipient = params.getString("recipient");
		String message = params.containsKey("message")?params.getString("message"):"";
		InitTransaction tx = new InitTransaction(publicKey, privateKey);
		JSONObject result = JSONObject.fromObject(tx.send(recipient, amount, message));
		if(result.containsKey("message") && "SUCCESS".equals(result.getString("message"))){
			String transactionHash = result.getJSONObject("transactionHash").getString("data");
			OutputMessage.initTransactionMessage("success", transactionHash);
		} else {
			OutputMessage.error(result.getString("message"));
		}
	}
	
	/**
	 * convert string to JSON object
	 * @param input
	 * @return
	 */
	private static JSONObject convertString2JSON(String input){
		JSONObject params = null;
		try {
			params = JSONObject.fromObject(input);
		} catch (Exception ex) {
			OutputMessage.error("invalid parameter");
			return null;
		}
		// check address
		if(!params.containsKey("address")){
			OutputMessage.error("invalid parameter [address]");
			return null;
		}
		params.put("address", params.getString("address").replaceAll("-", ""));
		if(params.getString("address").length()!=40){
			OutputMessage.error("invalid parameter [address]");
			return null;
		}
		// check privateKey
		if(!params.containsKey("privateKey")){
			OutputMessage.error("invalid parameter [privateKey]");
			return null;
		}
		// check recipient
		if(!params.containsKey("recipient")){
			OutputMessage.error("invalid parameter [recipient]");
			return null;
		}
		params.put("recipient", params.getString("recipient").replaceAll("-", ""));
		if(params.getString("recipient").length()!=40){
			OutputMessage.error("invalid parameter [recipient]");
			return null;
		}
		// check amount
		if(!params.containsKey("amount") || !StringUtils.isNumeric(params.getString("amount")) || params.getLong("amount")<0){
			OutputMessage.error("invalid parameter [amount]");
			return null;
		}
		// check message
		if(params.containsKey("message") && params.getString("message").getBytes().length>320){
			OutputMessage.error("invalid parameter [message]");
			return null;
		}
		return params;
	}
	
	/**
	 * query public key from NIS
	 * @param params
	 */
	private static void queryPublicKeyFromNIS(JSONObject params){
		// query publicKey
		String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET + "?address=" + params.getString("address"));
		JSONObject queryAccount = JSONObject.fromObject(queryResult);
		params.put("publicKey", queryAccount.getJSONObject("account").getString("publicKey"));
	}

}
