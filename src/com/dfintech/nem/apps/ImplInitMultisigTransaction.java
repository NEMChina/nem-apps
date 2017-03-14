package com.dfintech.nem.apps;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import com.dfintech.nem.apps.model.InitMultisigTransaction;
import com.dfintech.nem.apps.utils.Constants;
import com.dfintech.nem.apps.utils.HttpClientUtils;
import com.dfintech.nem.apps.utils.OutputMessage;

import net.sf.json.JSONObject;

/** 
 * @Description: Main class - test init multisig transaction
 * @author lu
 * @date 2017.03.07
 */ 
public class ImplInitMultisigTransaction {

	public static void main(String[] args) {
		if(args.length==0){
			OutputMessage.error("please enter json parameter");
			return;
		}
		Map<String, String> params = parseParamsToMap(args);
		if(params==null){
			return;
		}
		String host = params.get("host");
		String port = params.get("port");
		// set host and port
		if(host!=null)
			HttpClientUtils.defaultHost = host;
		if(port!=null)
			HttpClientUtils.defaultPort = port;
		// get publicKey from NIS
		queryPublicKeyFromNIS(params);
		// send transaction
		String multisigPublicKey = params.get("multisigPublicKey");
		String cosignatoryPublicKey = params.get("cosignatoryPublicKey");
		String cosignatoryPrivateKey = params.get("cosignatoryPrivateKey");
		long amount = Long.valueOf(params.get("amount")).longValue();
		String recipient = params.get("recipient");
		String message = params.containsKey("message")?params.get("message"):"";
		InitMultisigTransaction tx = new InitMultisigTransaction(cosignatoryPublicKey, cosignatoryPrivateKey, multisigPublicKey);
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
	 * parse parameters
	 * @param args
	 * @return
	 */
	private static Map<String, String> parseParamsToMap(String[] args){
		Map<String, String> params = new HashMap<String, String>();
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(Option.builder("multisigAddress").hasArg().build());
		options.addOption(Option.builder("cosignatoryAddress").hasArg().build());
		options.addOption(Option.builder("cosignatoryPrivateKey").hasArg().build());
		options.addOption(Option.builder("recipient").hasArg().build());
		options.addOption(Option.builder("amount").hasArg().build());
		options.addOption(Option.builder("message").hasArg().build());
		options.addOption(Option.builder("host").hasArg().build());
		options.addOption(Option.builder("port").hasArg().build());
		CommandLine commandLine = null;
		try{
			commandLine = parser.parse(options, args);
		} catch(Exception ex) {
			OutputMessage.error("invalid parameter");
			return null;
		}
		String multisigAddress = commandLine.getOptionValue("multisigAddress")==null?"":commandLine.getOptionValue("multisigAddress").replaceAll("-", "");
		String cosignatoryAddress = commandLine.getOptionValue("cosignatoryAddress")==null?"":commandLine.getOptionValue("cosignatoryAddress").replaceAll("-", "");
		String cosignatoryPrivateKey = commandLine.getOptionValue("cosignatoryPrivateKey")==null?"":commandLine.getOptionValue("cosignatoryPrivateKey");
		String recipient = commandLine.getOptionValue("recipient")==null?"":commandLine.getOptionValue("recipient").replaceAll("-", "");
		String amount = commandLine.getOptionValue("amount")==null?"":commandLine.getOptionValue("amount");
		String message = commandLine.getOptionValue("message")==null?"":commandLine.getOptionValue("message");
		String host = commandLine.getOptionValue("host");
		String port = commandLine.getOptionValue("port");
		// check multisigAddress
		if(multisigAddress.length()!=40){
			OutputMessage.error("invalid parameter [multisigAddress]");
			return null;
		}
		// check cosignatoryAddress
		if(cosignatoryAddress.length()!=40){
			OutputMessage.error("invalid parameter [cosignatoryAddress]");
			return null;
		}
		// check recipient
		if(recipient.length()!=40){
			OutputMessage.error("invalid parameter [recipient]");
			return null;
		}
		// check amount
		if(!StringUtils.isNumeric(amount) || Long.valueOf(amount).longValue()<0){
			OutputMessage.error("invalid parameter [amount]");
			return null;
		}
		// check message
		if(message.getBytes().length>320){
			OutputMessage.error("invalid parameter [message]");
			return null;
		}
		// check host
		if(host!=null && !host.matches("[0-9a-zA-Z]+(\\.[0-9a-zA-Z]+)+")){
			OutputMessage.error("invalid parameter [host]");
			return null;
		}
		// check port
		if(port!=null && !port.matches("[0-9]{1,5}")){
			OutputMessage.error("invalid parameter [port]");
			return null;
		}
		params.put("multisigAddress", multisigAddress);
		params.put("cosignatoryAddress", cosignatoryAddress);
		params.put("cosignatoryPrivateKey", cosignatoryPrivateKey);
		params.put("recipient", recipient);
		params.put("amount", amount);
		params.put("message", message);
		params.put("host", host);
		params.put("port", port);
		return params;
	}
	
	/**
	 * query public key from NIS
	 * @param params
	 */
	private static void queryPublicKeyFromNIS(Map<String, String> params){
		// query multisig account publicKey
		String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET + "?address=" + params.get("multisigAddress"));
		JSONObject queryAccount = JSONObject.fromObject(queryResult);
		params.put("multisigPublicKey", queryAccount.getJSONObject("account").getString("publicKey"));
		// query cosignatory account publicKey
		queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET + "?address=" + params.get("cosignatoryAddress"));
		queryAccount = JSONObject.fromObject(queryResult);
		params.put("cosignatoryPublicKey", queryAccount.getJSONObject("account").getString("publicKey"));
	}

}
