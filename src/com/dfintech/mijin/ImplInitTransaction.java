package com.dfintech.mijin;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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
public class ImplInitTransaction {

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
		String publicKey = params.get("publicKey");
		String privateKey = params.get("privateKey");
		long amount = Long.valueOf(params.get("amount")).longValue();
		String recipient = params.get("recipient");
		String message = params.containsKey("message")?params.get("message"):"";
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
	 * parse parameters
	 * @param args
	 * @return
	 */
	private static Map<String, String> parseParamsToMap(String[] args){
		Map<String, String> params = new HashMap<String, String>();
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(Option.builder("address").hasArg().build());
		options.addOption(Option.builder("privateKey").hasArg().build());
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
		String address = commandLine.getOptionValue("address")==null?"":commandLine.getOptionValue("address").replaceAll("-", "");
		String privateKey = commandLine.getOptionValue("privateKey")==null?"":commandLine.getOptionValue("privateKey");
		String recipient = commandLine.getOptionValue("recipient")==null?"":commandLine.getOptionValue("recipient").replaceAll("-", "");
		String amount = commandLine.getOptionValue("amount")==null?"":commandLine.getOptionValue("amount");
		String message = commandLine.getOptionValue("message")==null?"":commandLine.getOptionValue("message");
		String host = commandLine.getOptionValue("host");
		String port = commandLine.getOptionValue("port");
		// check address
		if(address.length()!=40){
			OutputMessage.error("invalid parameter [address]");
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
		params.put("address", address);
		params.put("privateKey", privateKey);
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
		// query publicKey
		String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET + "?address=" + params.get("address"));
		JSONObject queryAccount = JSONObject.fromObject(queryResult);
		params.put("publicKey", queryAccount.getJSONObject("account").getString("publicKey"));
	}

}
