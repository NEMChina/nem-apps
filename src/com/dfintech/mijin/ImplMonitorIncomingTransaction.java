package com.dfintech.mijin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.dfintech.mijin.model.IncomingTransaction;
import com.dfintech.mijin.utils.Constants;
import com.dfintech.mijin.utils.HexStringUtils;
import com.dfintech.mijin.utils.HttpClientUtils;
import com.dfintech.mijin.utils.OutputMessage;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Main class - test monitor incoming transactions
 * @author lu
 * @date 2017.03.07
 */
public class ImplMonitorIncomingTransaction {

	private static long lastID = 0;
	
	private static String address = null;
	
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
		address = params.get("address");
		ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
		pool.scheduleWithFixedDelay(new Runnable(){
			public void run() {
				lastID = monitor(address, lastID);
			};
		}, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
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
		String host = commandLine.getOptionValue("host");
		String port = commandLine.getOptionValue("port");
		// check address
		if(address.length()!=40){
			OutputMessage.error("invalid parameter [address]");
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
		params.put("host", host);
		params.put("port", port);
		return params;
	}
	
	/**
	 * monitor incoming transactions and output the transactions
	 * @param address
	 * @param lastID
	 * @return
	 */
	private static long monitor(String address, long lastID){
		long newLastID = 0;
		long queryID = 0;
		while(true){
			IncomingTransaction tx = new IncomingTransaction(address);
			String result = tx.query(queryID);
			JSONObject json = null;
			try {
				json = JSONObject.fromObject(result);
			} catch (Exception ex) {
				return lastID;
			}
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			JSONArray array = json.getJSONArray("data");
			if(array.size()==0){
				return newLastID;
			}
			for(int i=0;i<array.size();i++){
				JSONObject item = array.getJSONObject(i);
				JSONObject meta = item.getJSONObject("meta");
				JSONObject transaction = item.getJSONObject("transaction");
				if(lastID==0){ //init (first time)
					return meta.getLong("id");
				} else { //monitor
					if(newLastID==0){
						newLastID = meta.getLong("id");
					}
					if(meta.getLong("id")<=lastID){
						return newLastID;
					}
					queryID = meta.getLong("id");
					JSONObject outJSON = new JSONObject();
					if(transaction.containsKey("signatures")){ //multisig transaction
						JSONObject otherTrans = transaction.getJSONObject("otherTrans");
						String publicKey = otherTrans.getString("signer");
						String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET_FROMPUBLICKEY + "?publicKey=" + publicKey);
						JSONObject queryAccount = JSONObject.fromObject(queryResult);
						outJSON.put("sender", queryAccount.getJSONObject("account").getString("address"));
						outJSON.put("amount", Math.round((otherTrans.getLong("amount") / Math.pow(10, 6))));
						outJSON.put("date", dateFormat.format(new Date((otherTrans.getLong("timeStamp") + Constants.NEMSISTIME)*1000)));
						// message 
						if(otherTrans.containsKey("message") && otherTrans.getJSONObject("message").containsKey("type")){
							JSONObject message = otherTrans.getJSONObject("message");
							// if message type is 1, convert to String
							if(message.getInt("type")==1 && HexStringUtils.hex2String(message.getString("payload"))!=null){
								outJSON.put("message", HexStringUtils.hex2String(message.getString("payload")));
							}
						}
						outJSON.put("isMultisig", "1");
						System.out.println(outJSON.toString());
					} else { //normal transaction
						String publicKey = transaction.getString("signer");
						String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET_FROMPUBLICKEY + "?publicKey=" + publicKey);
						JSONObject queryAccount = JSONObject.fromObject(queryResult);
						outJSON.put("sender", queryAccount.getJSONObject("account").getString("address"));
						outJSON.put("amount", Math.round((transaction.getLong("amount") / Math.pow(10, 6))));
						outJSON.put("date", dateFormat.format(new Date((transaction.getLong("timeStamp") + Constants.NEMSISTIME)*1000)));
						// message 
						if(transaction.containsKey("message") && transaction.getJSONObject("message").containsKey("type")){
							JSONObject message = transaction.getJSONObject("message");
							// if message type is 1, convert to String
							if(message.getInt("type")==1 && HexStringUtils.hex2String(message.getString("payload"))!=null){
								outJSON.put("message", HexStringUtils.hex2String(message.getString("payload")));
							}
						}
						outJSON.put("isMultisig", "0");
						System.out.println(outJSON.toString());
					}
					
				}
			}
		}
	}
}
