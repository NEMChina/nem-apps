package com.dfintech.nem.apps.ws.handlers;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nem.core.model.mosaic.MosaicFeeInformation;
import org.nem.core.model.mosaic.MosaicId;
import org.nem.core.model.namespace.NamespaceId;
import org.nem.core.model.primitive.Amount;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;

import com.dfintech.nem.apps.model.UnconfirmedTransaction;
import com.dfintech.nem.apps.utils.Constants;
import com.dfintech.nem.apps.utils.DateUtils;
import com.dfintech.nem.apps.utils.HexStringUtils;
import com.dfintech.nem.apps.utils.HttpClientUtils;
import com.dfintech.nem.apps.utils.KeyConvertor;
import com.dfintech.nem.apps.utils.NISQuery;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: monitor multisig websocket handler
 * @author lu
 * @date 2017.03.24
 */ 
public class WsMonitorMultisigHandler implements StompSessionHandler {
	
	private String address = null;
	
	private static Map<String, JSONObject> outCosignedMap = new HashMap<String, JSONObject>();
	
	public WsMonitorMultisigHandler(String address) {
		this.address = address;
		this.loadMultisigUnconfirmed();
	}
	
	private void loadMultisigUnconfirmed(){
		UnconfirmedTransaction tx = new UnconfirmedTransaction(this.address);
		String result = tx.query();
		JSONObject json = null;
		try {
			json = JSONObject.fromObject(result);
		} catch (Exception ex) {
			return;
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONArray array = json.getJSONArray("data");
		for(int i=0;i<array.size();i++){
			JSONObject item = array.getJSONObject(i);
			JSONObject meta = item.getJSONObject("meta");
			JSONObject transaction = item.getJSONObject("transaction");
			JSONObject outJSON = new JSONObject();
			// check if multisig transaction
			if(!transaction.containsKey("signatures")){
				continue;
			}
			JSONObject otherTrans = transaction.getJSONObject("otherTrans");
			JSONArray signatures = transaction.getJSONArray("signatures");
			// check if belong this address
			if(!this.address.equals(KeyConvertor.getAddressFromPublicKey(otherTrans.getString("signer")))){
				continue;
			}
			String innerTransactionHash = meta.getString("data");
			// query all cosignatories
			Set<String> allCosignAccount = new HashSet<String>();
			String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET + "?address=" + this.address);
			JSONObject queryAccount = JSONObject.fromObject(queryResult);
			if(queryAccount.containsKey("meta") && queryAccount.getJSONObject("meta").containsKey("cosignatories")){
				JSONArray cosignatories = queryAccount.getJSONObject("meta").getJSONArray("cosignatories");
				for(int j=0;j<cosignatories.size();j++){
					allCosignAccount.add(cosignatories.getJSONObject(j).getString("address"));
				}
			}
			int minCosignatories = queryAccount.getJSONObject("account").getJSONObject("multisigInfo").getInt("minCosignatories");
			if(minCosignatories<=signatures.size()+1){
				continue;
			}
			// cosigned cosignatories
			JSONArray outCosignAccountArray = new JSONArray();
			JSONObject outCosignAccount = new JSONObject();
			String signer = transaction.getString("signer");
			outCosignAccount.put("address", KeyConvertor.getAddressFromPublicKey(signer));
			outCosignAccount.put("date", DateUtils.nemToRealDateStr(transaction.getLong("timeStamp")));
			outCosignAccountArray.add(outCosignAccount);
			allCosignAccount.remove(KeyConvertor.getAddressFromPublicKey(signer));
			for(int j=0;j<signatures.size();j++){
				JSONObject signature = signatures.getJSONObject(j);
				outCosignAccount = new JSONObject();
				signer = signature.getString("signer");
				queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET_FROMPUBLICKEY + "?publicKey=" + signer);
				queryAccount = JSONObject.fromObject(queryResult);
				outCosignAccount.put("address", queryAccount.getJSONObject("account").getString("address"));
				outCosignAccount.put("date", dateFormat.format(new Date((signature.getLong("timeStamp") + Constants.NEMSISTIME)*1000)));
				outCosignAccountArray.add(outCosignAccount);
				allCosignAccount.remove(queryAccount.getJSONObject("account").getString("address"));
			}
			// unsigned cosignatories
			JSONArray outUnsignedAccountArray = new JSONArray();
			for(String unsignedAccount:allCosignAccount){
				JSONObject unsignedAccountItem = new JSONObject();
				unsignedAccountItem.put("address", unsignedAccount);
				outUnsignedAccountArray.add(unsignedAccountItem);
			}
			outJSON.put("status", "pending");
			outJSON.put("innerTransactionHash", innerTransactionHash);
			outJSON.put("cosignAccount", outCosignAccountArray);
			outJSON.put("unsignedAccount", outUnsignedAccountArray);
			outJSON.put("minCosignatories", minCosignatories);
			outJSON.put("recipient", otherTrans.getString("recipient"));
			outJSON.put("amount", Amount.fromMicroNem(otherTrans.getLong("amount")).getNumNem());
			// message 
			if(otherTrans.containsKey("message") && otherTrans.getJSONObject("message").containsKey("type")){
				JSONObject message = otherTrans.getJSONObject("message");
				// if message type is 1, convert to String
				if(message.getInt("type")==1 && HexStringUtils.hex2String(message.getString("payload"))!=null){
					outJSON.put("message", HexStringUtils.hex2String(message.getString("payload")));
				}
			}
			// mosaic
			if(otherTrans.containsKey("mosaics")){
				JSONArray outMosaicArray = new JSONArray();
				JSONArray mosaics = otherTrans.getJSONArray("mosaics");
				for(int j=0;j<mosaics.size();j++){
					JSONObject outMosaic = new JSONObject();
					JSONObject mosaic = mosaics.getJSONObject(j);
					long quantity = mosaic.getLong("quantity");
					String namespace = mosaic.getJSONObject("mosaicId").getString("namespaceId");
					String mosaicName = mosaic.getJSONObject("mosaicId").getString("name");
					MosaicId mosaicId = new MosaicId(new NamespaceId(namespace), mosaicName);
					MosaicFeeInformation m = NISQuery.findMosaicFeeInformationByNIS(mosaicId);
					outMosaic.put("name", mosaicId.toString());
					outMosaic.put("quantity", quantity / Math.pow(10, m.getDivisibility()));
					outMosaicArray.add(outMosaic);
				}
				if(outMosaicArray.size()!=0){
					outJSON.put("mosaics", outMosaicArray);
				}
			}
			outCosignedMap.put(innerTransactionHash, outJSON);
		}
	}
	
	/**
	 * monitor multisig unconfirmed (init)
	 * @param result
	 * @return
	 */
	private void monitorInitMultisigUnconfirmed(String result){
		JSONObject json = null;
		try {
			json = JSONObject.fromObject(result);
		} catch (Exception ex) {
			return;
		}
		JSONObject meta = json.getJSONObject("meta");
		JSONObject transaction = json.getJSONObject("transaction");
		if(!meta.containsKey("innerHash") 
				|| !transaction.containsKey("signatures") 
				|| transaction.getLong("type")!=Constants.TX_TYPE_INIT_MULTISIG){
			return;
		}
		String innerTransactionHash = meta.getJSONObject("innerHash").getString("data");
		JSONObject otherTrans = transaction.getJSONObject("otherTrans");
		JSONObject outJSON = new JSONObject();
		// query all cosignatories
		Set<String> allCosignAccount = new HashSet<String>();
		String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET + "?address=" + this.address);
		JSONObject queryAccount = JSONObject.fromObject(queryResult);
		if(queryAccount.containsKey("meta") && queryAccount.getJSONObject("meta").containsKey("cosignatories")){
			JSONArray cosignatories = queryAccount.getJSONObject("meta").getJSONArray("cosignatories");
			for(int j=0;j<cosignatories.size();j++){
				allCosignAccount.add(cosignatories.getJSONObject(j).getString("address"));
			}
		}
		int minCosignatories = queryAccount.getJSONObject("account").getJSONObject("multisigInfo").getInt("minCosignatories");
		// cosigned cosignatories
		JSONArray outCosignAccountArray = new JSONArray();
		JSONObject outCosignAccount = new JSONObject();
		String signer = transaction.getString("signer");
		outCosignAccount.put("address", KeyConvertor.getAddressFromPublicKey(signer));
		outCosignAccount.put("date", DateUtils.nemToRealDateStr(transaction.getLong("timeStamp")));
		outCosignAccountArray.add(outCosignAccount);
		allCosignAccount.remove(KeyConvertor.getAddressFromPublicKey(signer));
		// unsigned cosignatories
		JSONArray outUnsignedAccountArray = new JSONArray();
		for(String unsignedAccount:allCosignAccount){
			JSONObject unsignedAccountItem = new JSONObject();
			unsignedAccountItem.put("address", unsignedAccount);
			outUnsignedAccountArray.add(unsignedAccountItem);
		}
		outJSON.put("status", "pending");
		outJSON.put("innerTransactionHash", innerTransactionHash);
		outJSON.put("cosignAccount", outCosignAccountArray);
		outJSON.put("unsignedAccount", outUnsignedAccountArray);
		outJSON.put("minCosignatories", minCosignatories);
		outJSON.put("recipient", otherTrans.getString("recipient"));
		outJSON.put("amount", Amount.fromMicroNem(otherTrans.getLong("amount")).getNumNem());
		// message 
		if(otherTrans.containsKey("message") && otherTrans.getJSONObject("message").containsKey("type")){
			JSONObject message = otherTrans.getJSONObject("message");
			// if message type is 1, convert to String
			if(message.getInt("type")==1 && HexStringUtils.hex2String(message.getString("payload"))!=null){
				outJSON.put("message", HexStringUtils.hex2String(message.getString("payload")));
			}
		}
		// mosaic
		if(otherTrans.containsKey("mosaics")){
			JSONArray outMosaicArray = new JSONArray();
			JSONArray mosaics = otherTrans.getJSONArray("mosaics");
			for(int j=0;j<mosaics.size();j++){
				JSONObject outMosaic = new JSONObject();
				JSONObject mosaic = mosaics.getJSONObject(j);
				long quantity = mosaic.getLong("quantity");
				String namespace = mosaic.getJSONObject("mosaicId").getString("namespaceId");
				String mosaicName = mosaic.getJSONObject("mosaicId").getString("name");
				MosaicId mosaicId = new MosaicId(new NamespaceId(namespace), mosaicName);
				MosaicFeeInformation m = NISQuery.findMosaicFeeInformationByNIS(mosaicId);
				outMosaic.put("name", mosaicId.toString());
				outMosaic.put("quantity", quantity / Math.pow(10, m.getDivisibility()));
				outMosaicArray.add(outMosaic);
			}
			if(outMosaicArray.size()!=0){
				outJSON.put("mosaics", outMosaicArray);
			}
		}
		outCosignedMap.put(innerTransactionHash, outJSON);
		System.out.println(outJSON.toString());
	}

	/**
	 * monitor multisig unconfirmed (cosign)
	 * @param result
	 * @return
	 */
	private void monitorCosignUnconfirmed(String result){
		JSONObject json = null;
		try {
			json = JSONObject.fromObject(result);
		} catch (Exception ex) {
			return;
		}
		if(!json.containsKey("otherHash") 
				|| !json.containsKey("otherAccount")
				|| json.getLong("type")!=Constants.TX_TYPE_COSIGN_MULTISIG){
			return;
		}
		long timeStamp = json.getLong("timeStamp");
		String innerTransactionHash = json.getJSONObject("otherHash").getString("data");
		String otherAccount = json.getString("otherAccount");
		String cosignAddress = KeyConvertor.getAddressFromPublicKey(json.getString("signer"));
		if(!otherAccount.equals(this.address)){
			return;
		}
		if(!outCosignedMap.containsKey(innerTransactionHash)){
			return;
		}
		JSONObject outJSON = outCosignedMap.get(innerTransactionHash);
		JSONObject outCosignAccount = new JSONObject();
		JSONArray outCosignAccountArray = outJSON.getJSONArray("cosignAccount");
		outCosignAccount.put("address", cosignAddress);
		outCosignAccount.put("date", DateUtils.nemToRealDateStr(timeStamp));
		outCosignAccountArray.add(outCosignAccount);
		if(outCosignAccountArray.size()>=outJSON.getInt("minCosignatories")){
			outCosignedMap.remove(innerTransactionHash);
			return;
		}
		JSONArray outUnsignedAccountArray = outJSON.getJSONArray("unsignedAccount");
		int removeIndex = -1;
		for(int i=0;i<outUnsignedAccountArray.size();i++){
			if(outUnsignedAccountArray.getJSONObject(i).getString("address").equals(cosignAddress)){
				removeIndex = i;
				break;
			}
		}
		if(removeIndex!=-1) {
			outUnsignedAccountArray.remove(removeIndex);
		}
		outJSON.put("cosignAccount", outCosignAccountArray);
		outJSON.put("unsignedAccount", outUnsignedAccountArray);
		outCosignedMap.put(innerTransactionHash, outJSON);
		System.out.println(outJSON.toString());
	}
	
	/**
	 * monitor outgoing multisig transactions (when the amount of cosignatories >= min amount of cosignatories)
	 * @param result
	 * @return
	 */
	private void monitorOutgoing(String result){
		JSONObject json = null;
		try {
			json = JSONObject.fromObject(result);
		} catch (Exception ex) {
			return;
		}
		JSONObject meta = json.getJSONObject("meta");
		JSONObject transaction = json.getJSONObject("transaction");
		if(transaction.containsKey("signatures")){ //multisig transaction
			JSONObject outJSON = new JSONObject();
			JSONArray outCosignAccountArray = new JSONArray();
			JSONObject otherTrans = transaction.getJSONObject("otherTrans");
			JSONArray signatures = transaction.getJSONArray("signatures");
			JSONObject outCosignAccount = new JSONObject();
			if(!this.address.equals(KeyConvertor.getAddressFromPublicKey(otherTrans.getString("signer")))){
				return;
			}
			//query all cosignatories
			Set<String> allCosignAccount = new HashSet<String>();
			String queryResult = HttpClientUtils.get(Constants.URL_ACCOUNT_GET + "?address=" + this.address);
			JSONObject queryAccount = JSONObject.fromObject(queryResult);
			if(queryAccount.containsKey("meta") && queryAccount.getJSONObject("meta").containsKey("cosignatories")){
				JSONArray cosignatories = queryAccount.getJSONObject("meta").getJSONArray("cosignatories");
				for(int i=0;i<cosignatories.size();i++){
					allCosignAccount.add(cosignatories.getJSONObject(i).getString("address"));
				}
			}
			int minCosignatories = queryAccount.getJSONObject("account").getJSONObject("multisigInfo").getInt("minCosignatories");
			//cosigned account
			String signer = transaction.getString("signer");
			outCosignAccount.put("address", KeyConvertor.getAddressFromPublicKey(signer));
			outCosignAccount.put("date", DateUtils.nemToRealDateStr(transaction.getLong("timeStamp")));
			outCosignAccountArray.add(outCosignAccount);
			allCosignAccount.remove(queryAccount.getJSONObject("account").getString("address"));
			for(int j=0;j<signatures.size();j++){
				JSONObject signature = signatures.getJSONObject(j);
				outCosignAccount = new JSONObject();
				signer = signature.getString("signer");
				outCosignAccount.put("address", KeyConvertor.getAddressFromPublicKey(signer));
				outCosignAccount.put("date", DateUtils.nemToRealDateStr(signature.getLong("timeStamp")));
				outCosignAccountArray.add(outCosignAccount);
				allCosignAccount.remove(KeyConvertor.getAddressFromPublicKey(signer));
			}
			//unsigned account
			JSONArray outUnsignedAccount = new JSONArray();
			for(String unsignedAccount:allCosignAccount){
				JSONObject unsignedAccountItem = new JSONObject();
				unsignedAccountItem.put("address", unsignedAccount);
				outUnsignedAccount.add(unsignedAccountItem);
			}
			outJSON.put("status", "success");
			outJSON.put("innerTransactionHash", meta.getJSONObject("innerHash").getString("data"));
			outJSON.put("cosignAccount", outCosignAccountArray);
			outJSON.put("unsignedAccount", outUnsignedAccount);
			outJSON.put("minCosignatories", minCosignatories);
			outJSON.put("recipient", otherTrans.getString("recipient"));
			outJSON.put("amount", Amount.fromMicroNem(otherTrans.getLong("amount")).getNumNem());
			// message 
			if(otherTrans.containsKey("message") && otherTrans.getJSONObject("message").containsKey("type")){
				JSONObject message = otherTrans.getJSONObject("message");
				// if message type is 1, convert to String
				if(message.getInt("type")==1 && HexStringUtils.hex2String(message.getString("payload"))!=null){
					outJSON.put("message", HexStringUtils.hex2String(message.getString("payload")));
				}
			}
			// mosaic
			if(otherTrans.containsKey("mosaics")){
				JSONArray outMosaicArray = new JSONArray();
				JSONArray mosaics = otherTrans.getJSONArray("mosaics");
				for(int j=0;j<mosaics.size();j++){
					JSONObject outMosaic = new JSONObject();
					JSONObject mosaic = mosaics.getJSONObject(j);
					long quantity = mosaic.getLong("quantity");
					String namespace = mosaic.getJSONObject("mosaicId").getString("namespaceId");
					String mosaicName = mosaic.getJSONObject("mosaicId").getString("name");
					MosaicId mosaicId = new MosaicId(new NamespaceId(namespace), mosaicName);
					MosaicFeeInformation m = NISQuery.findMosaicFeeInformationByNIS(mosaicId);
					outMosaic.put("name", mosaicId.toString());
					outMosaic.put("quantity", quantity / Math.pow(10, m.getDivisibility()));
					outMosaicArray.add(outMosaic);
				}
				if(outMosaicArray.size()!=0){
					outJSON.put("mosaics", outMosaicArray);
				}
			}
			outCosignedMap.remove(meta.getJSONObject("innerHash").getString("data"));
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
		// monitor init multisig transaction
		session.subscribe(Constants.URL_WS_UNCONFIRMED + "/" + this.address, new StompFrameHandler() {
			public Type getPayloadType(StompHeaders stompHeaders) {
				return String.class;
	        }
	        public void handleFrame(StompHeaders stompHeaders, Object result) {
	        	monitorInitMultisigUnconfirmed(result.toString());
	        }
		});
		// monitor cosign multisig transaction
		session.subscribe(Constants.URL_WS_UNCONFIRMED, new StompFrameHandler() {
			public Type getPayloadType(StompHeaders stompHeaders) {
				return String.class;
	        }
	        public void handleFrame(StompHeaders stompHeaders, Object result) {
	        	monitorCosignUnconfirmed(result.toString());
	        }
		});
		// monitor multisig outgoing transaction (when multisig complete)
		session.subscribe(Constants.URL_WS_TRANSACTIONS + "/" + this.address, new StompFrameHandler() {
			public Type getPayloadType(StompHeaders stompHeaders) {
				return String.class;
	        }
	        public void handleFrame(StompHeaders stompHeaders, Object result) {
	        	monitorOutgoing(result.toString());
	        }
		});
	}

	@Override
	public void handleException(StompSession arg0, StompCommand arg1, StompHeaders arg2, byte[] arg3, Throwable arg4) { }

	@Override
	public void handleTransportError(StompSession arg0, Throwable arg1) { }
	
}
