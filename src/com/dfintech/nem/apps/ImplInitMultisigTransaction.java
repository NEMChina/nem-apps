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
import com.dfintech.nem.apps.utils.DefaultSetting;
import com.dfintech.nem.apps.utils.HelperUtils;
import com.dfintech.nem.apps.utils.KeyConvertor;
import com.dfintech.nem.apps.utils.OutputMessage;

import net.sf.json.JSONObject;

/** 
 * @Description: Main class - test init multisig transaction
 * @author lu
 * @date 2017.03.07
 */ 
public class ImplInitMultisigTransaction {

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
		// get multisig publick key
		String multisigPublicKey = KeyConvertor.getPublicKeyFromAddress(params.get("multisigAddress"));
		if(StringUtils.isEmpty(multisigPublicKey)){
			OutputMessage.error("unable to find multisig public key from multisig address");
			return;
		}
		// send transaction
		String cosignatoryPublicKey = params.get("cosignatoryPublicKey");
		String cosignatoryPrivateKey = params.get("cosignatoryPrivateKey");
		long amount = Long.valueOf(params.get("amount")).longValue();
		String recipient = params.get("recipient");
		String message = params.containsKey("message")?params.get("message"):"";
		InitMultisigTransaction tx = new InitMultisigTransaction(cosignatoryPublicKey, cosignatoryPrivateKey, multisigPublicKey);
		JSONObject result = JSONObject.fromObject(tx.send_v2(recipient, amount, message));
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
		options.addOption(Option.builder("cosignatoryPrivateKey").hasArg().build());
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
			System.out.println(HelperUtils.printHelper(Constants.HELPER_FILE_INIT_MULTISIG_TRANSACTION));
			return null;
		}
		String multisigAddress = commandLine.getOptionValue("multisigAddress")==null?"":commandLine.getOptionValue("multisigAddress").replaceAll("-", "");
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
		// get cosignatory public key
		String cosignatoryPublicKey = KeyConvertor.getPublicFromPrivateKey(cosignatoryPrivateKey);
		if(StringUtils.isEmpty(cosignatoryPublicKey)){
			OutputMessage.error("unable to find cosignatory public key from private key");
			return null;
		}
		// get cosignatory address
		String cosignatoryAddress = KeyConvertor.getAddressFromPrivateKey(cosignatoryPrivateKey);
		if(StringUtils.isEmpty(cosignatoryAddress)){
			OutputMessage.error("unable to find cosignatory address from private key");
			return null;
		}
		params.put("multisigAddress", multisigAddress);
		params.put("cosignatoryAddress", cosignatoryAddress);
		params.put("cosignatoryPublicKey", cosignatoryPublicKey);
		params.put("cosignatoryPrivateKey", cosignatoryPrivateKey);
		params.put("recipient", recipient);
		params.put("amount", amount);
		params.put("message", message);
		params.put("host", host);
		params.put("port", port);
		return params;
	}
}
