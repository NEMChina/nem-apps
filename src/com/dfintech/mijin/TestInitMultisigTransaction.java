package com.dfintech.mijin;

import org.apache.commons.lang.StringUtils;

import com.dfintech.mijin.model.InitMultisigTransaction;
import com.dfintech.mijin.utils.Constants;
import com.dfintech.mijin.utils.HttpClientUtils;
import com.dfintech.mijin.utils.OutputMessage;

import net.sf.json.JSONObject;

/** 
 * @Description: Main class - test init multisig transaction
 * @author lu
 * @date 2017.03.07
 */ 
public class TestInitMultisigTransaction {

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
		String publicKey = params.getJSONObject("cosignatoryAccount").getString("publicKey");
		String privateKey = params.getJSONObject("cosignatoryAccount").getString("privateKey");
		String multisigPublicKey = params.getJSONObject("multisigAccount").getString("publicKey");
		int amount = params.getInt("amount");
		String recipient = params.getString("recipient");
		String message = params.containsKey("message")?params.getString("message"):"";
		InitMultisigTransaction tx = new InitMultisigTransaction(publicKey, privateKey, multisigPublicKey);
		JSONObject result = JSONObject.fromObject(tx.send(recipient, amount, message));
		if(result.containsKey("message") && "SUCCESS".equals(result.getString("message"))){
			String transactionHash = result.getJSONObject("transactionHash").getString("data");
			String innerTransactionHash = result.getJSONObject("innerTransactionHash").getString("data");
			OutputMessage.initMultisigTransactionMessage("success", transactionHash, innerTransactionHash);
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
		// check multisigAccount 
		if(!params.containsKey("multisigAccount")){
			OutputMessage.error("invalid parameter [multisigAccount]");
			return null;
		}
		JSONObject multisigAccount = params.getJSONObject("multisigAccount");
		if(!multisigAccount.containsKey("address")){
			OutputMessage.error("invalid parameter [multisigAccount - address]");
			return null;
		}
		multisigAccount.put("address", multisigAccount.getString("address").replaceAll("-", ""));
		if(multisigAccount.getString("address").length()!=40){
			OutputMessage.error("invalid parameter [multisigAccount - address]");
			return null;
		}
		// check cosignatoryAccount
		if(!params.containsKey("cosignatoryAccount")){
			OutputMessage.error("invalid parameter [cosignatoryAccount]");
			return null;
		}
		JSONObject cosignatoryAccount = params.getJSONObject("cosignatoryAccount");
		if(!cosignatoryAccount.containsKey("address")){
			OutputMessage.error("invalid parameter [cosignatoryAccount - address]");
			return null;
		}
		cosignatoryAccount.put("address", cosignatoryAccount.getString("address").replaceAll("-", ""));
		if(cosignatoryAccount.getString("address").length()!=40){
			OutputMessage.error("invalid parameter [cosignatoryAccount - address]");
			return null;
		}
		if(!cosignatoryAccount.containsKey("privateKey")){
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
		// query multisig account publicKey
		JSONObject multisigAccount = params.getJSONObject("multisigAccount");
		String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET + "?address=" + multisigAccount.getString("address"));
		JSONObject queryAccount = JSONObject.fromObject(queryResult);
		multisigAccount.put("publicKey", queryAccount.getJSONObject("account").getString("publicKey"));
		// query cosignatory account publicKey
		JSONObject cosignatoryAccount = params.getJSONObject("cosignatoryAccount");
		queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET + "?address=" + cosignatoryAccount.getString("address"));
		queryAccount = JSONObject.fromObject(queryResult);
		cosignatoryAccount.put("publicKey", queryAccount.getJSONObject("account").getString("publicKey"));
	}

}
