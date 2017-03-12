package com.dfintech.mijin;

import com.dfintech.mijin.model.CosignMultisigTransaction;
import com.dfintech.mijin.utils.Constants;
import com.dfintech.mijin.utils.HttpClientUtils;
import com.dfintech.mijin.utils.OutputMessage;

import net.sf.json.JSONObject;

/** 
 * @Description: Main class - test cosign multisig transaction
 * @author lu
 * @date 2017.03.07
 */ 
public class TestCosignMultisigTransaction {

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
		String innerTransactionHash = params.getString("innerTransactionHash");
		String publicKey = params.getJSONObject("cosignatoryAccount").getString("publicKey");
		String privateKey = params.getJSONObject("cosignatoryAccount").getString("privateKey");
		String multisigAddress = params.getJSONObject("multisigAccount").getString("address");
		CosignMultisigTransaction tx = new CosignMultisigTransaction(publicKey, privateKey, multisigAddress, innerTransactionHash);
		JSONObject result = JSONObject.fromObject(tx.send());
		if(result.containsKey("message") && "SUCCESS".equals(result.getString("message"))){
			String transactionHash = result.getJSONObject("transactionHash").getString("data");
			OutputMessage.initCosignTransactionMessage("success", transactionHash);
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
			OutputMessage.error("invalid parameter [cosignatoryAccount - privateKey]");
			return null;
		}
		// check aminnerTransactionHashount
		if(!params.containsKey("innerTransactionHash")){
			OutputMessage.error("invalid parameter [innerTransactionHash]");
			return null;
		}
		return params;
	}
	
	/**
	 * query public key from NIS
	 * @param params
	 */
	private static void queryPublicKeyFromNIS(JSONObject params){
		// query cosignatory account publicKey
		JSONObject cosignatoryAccount = params.getJSONObject("cosignatoryAccount");
		String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET + "?address=" + cosignatoryAccount.getString("address"));
		JSONObject queryAccount = JSONObject.fromObject(queryResult);
		cosignatoryAccount.put("publicKey", queryAccount.getJSONObject("account").getString("publicKey"));
	}

}
