package com.dfintech.nem.apps.ws.handlers;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.nem.core.model.primitive.Amount;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;

import com.dfintech.nem.apps.utils.Constants;
import com.dfintech.nem.apps.utils.HexStringUtils;
import com.dfintech.nem.apps.utils.HttpClientUtils;

import net.sf.json.JSONObject;

/** 
 * @Description: monitor incoming websocket handler
 * @author lu
 * @date 2017.03.24
 */ 
public class WsMonitorImcomingHandler implements StompSessionHandler {
	
	private String address = null;
	
	public WsMonitorImcomingHandler(String address) {
		this.address = address;
	}

	/**
	 * monitor incoming transactions and output the transactions
	 * @param address
	 * @param result
	 * @return
	 */
	private void monitor(String address, String result){
		JSONObject json = null;
		try {
			json = JSONObject.fromObject(result);
		} catch (Exception ex) {
			return;
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONObject transaction = json.getJSONObject("transaction");
		JSONObject outJSON = new JSONObject();
		if(transaction.containsKey("signatures")){ //multisig transaction
			JSONObject otherTrans = transaction.getJSONObject("otherTrans");
			String recipient = otherTrans.getString("recipient");
			if(!address.equals(recipient)){ // if not incoming transaction, return
				return;
			}
			String publicKey = otherTrans.getString("signer");
			String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET_FROMPUBLICKEY + "?publicKey=" + publicKey);
			JSONObject queryAccount = JSONObject.fromObject(queryResult);
			outJSON.put("sender", queryAccount.getJSONObject("account").getString("address"));
			outJSON.put("amount", Amount.fromMicroNem(otherTrans.getLong("amount")).getNumNem());
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
			String recipient = transaction.getString("recipient");
			if(!address.equals(recipient)){ // if not incoming transaction, return
				return;
			}
			String publicKey = transaction.getString("signer");
			String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET_FROMPUBLICKEY + "?publicKey=" + publicKey);
			JSONObject queryAccount = JSONObject.fromObject(queryResult);
			outJSON.put("sender", queryAccount.getJSONObject("account").getString("address"));
			outJSON.put("amount", Amount.fromMicroNem(transaction.getLong("amount")).getNumNem());
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
	
	@Override
	public Type getPayloadType(StompHeaders arg0) {
		return String.class;
	}

	@Override
	public void handleFrame(StompHeaders arg0, Object arg1) { }

	@Override
	public void afterConnected(StompSession session, StompHeaders arg1) {
		String account = "{\"account\":\"" + this.address + "\"}";
		// the address should send to the server before subscribing
		session.send(Constants.URL_WS_W_API_ACCOUNT_SUBSCRIBE, account);
		session.subscribe(Constants.URL_WS_TRANSACTIONS + "/" + address, new StompFrameHandler() {
			public Type getPayloadType(StompHeaders stompHeaders) {
				return String.class;
	        }
	        public void handleFrame(StompHeaders stompHeaders, Object result) {
	        	monitor(address, result.toString());
	        }
		});
	}

	@Override
	public void handleException(StompSession arg0, StompCommand arg1, StompHeaders arg2, byte[] arg3, Throwable arg4) { }

	@Override
	public void handleTransportError(StompSession arg0, Throwable arg1) { }
	
}
