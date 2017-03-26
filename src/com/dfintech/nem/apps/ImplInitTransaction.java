package com.dfintech.nem.apps;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import com.dfintech.nem.apps.model.InitTransaction;
import com.dfintech.nem.apps.utils.Constants;
import com.dfintech.nem.apps.utils.DefaultSetting;
import com.dfintech.nem.apps.utils.HelperUtils;
import com.dfintech.nem.apps.utils.KeyConvertor;
import com.dfintech.nem.apps.utils.OutputMessage;

import net.sf.json.JSONObject;

/** 
 * @Description: Main class - test init transaction
 * @author lu
 * @date 2017.03.10
 */ 
public class ImplInitTransaction {

	public static void main(String[] args) {
		DefaultSetting.setDefaultNetwork();
		if(args.length==0){
			OutputMessage.error("please enter parameter");
			return;
		}
		Map<String, String> params = parseParamsToMap(args);
		if(params==null){
			return;
		}
		// set host and port
		DefaultSetting.setHostAndPort(params.get("host"), params.get("port"), null);
		// send transaction
		String publicKey = params.get("publicKey");
		String privateKey = params.get("privateKey");
		long amount = Long.valueOf(params.get("amount")).longValue();
		String recipient = params.get("recipient");
		String message = params.containsKey("message")?params.get("message"):"";
		InitTransaction tx = new InitTransaction(publicKey, privateKey);
		JSONObject result = JSONObject.fromObject(tx.send_v2(recipient, amount, message));
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
		options.addOption(Option.builder("privateKey").hasArg().build());
		options.addOption(Option.builder("recipient").hasArg().build());
		options.addOption(Option.builder("amount").hasArg().build());
		options.addOption(Option.builder("message").hasArg().build());
		options.addOption(Option.builder("host").hasArg().build());
		options.addOption(Option.builder("port").hasArg().build());
		options.addOption(Option.builder("h").longOpt("help").build());
		CommandLine commandLine = null;
		try{
			commandLine = parser.parse(options, args);
		} catch(Exception ex) {
			OutputMessage.error("invalid parameter");
			return null;
		}
		// print helper
		if(commandLine.hasOption("h")){
			System.out.println(HelperUtils.printHelper(Constants.HELPER_FILE_INIT_TRANSACTION));
			return null;
		}
		String privateKey = commandLine.getOptionValue("privateKey")==null?"":commandLine.getOptionValue("privateKey");
		String recipient = commandLine.getOptionValue("recipient")==null?"":commandLine.getOptionValue("recipient").replaceAll("-", "");
		String amount = commandLine.getOptionValue("amount")==null?"":commandLine.getOptionValue("amount");
		String message = commandLine.getOptionValue("message")==null?"":commandLine.getOptionValue("message");
		String host = commandLine.getOptionValue("host");
		String port = commandLine.getOptionValue("port");
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
		// get address
		String address = KeyConvertor.getAddressFromPrivateKey(privateKey);
		if(StringUtils.isEmpty(address)){
			OutputMessage.error("unable to find address from private key");
			return null;
		}
		// get public key
		String publicKey = KeyConvertor.getPublicFromPrivateKey(privateKey);
		if(StringUtils.isEmpty(publicKey)){
			OutputMessage.error("unable to find public key from private key");
			return null;
		}
		params.put("address", address);
		params.put("publicKey", publicKey);
		params.put("privateKey", privateKey);
		params.put("recipient", recipient);
		params.put("amount", amount);
		params.put("message", message);
		params.put("host", host);
		params.put("port", port);
		return params;
	}
	
}
