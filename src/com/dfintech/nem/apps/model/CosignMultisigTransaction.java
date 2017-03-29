package com.dfintech.nem.apps.model;

import java.util.Date;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.nem.core.crypto.Hash;
import org.nem.core.crypto.KeyPair;
import org.nem.core.crypto.PrivateKey;
import org.nem.core.crypto.PublicKey;
import org.nem.core.model.Account;
import org.nem.core.model.Address;
import org.nem.core.model.MultisigSignatureTransaction;
import org.nem.core.model.TransactionFeeCalculatorAfterForkForApp;
import org.nem.core.model.primitive.Amount;
import org.nem.core.serialization.BinarySerializer;
import org.nem.core.time.SystemTimeProvider;
import org.nem.core.time.TimeInstant;

import com.dfintech.nem.apps.utils.Constants;
import com.dfintech.nem.apps.utils.HttpClientUtils;

import net.sf.json.JSONObject;

/** 
 * @Description: Cosign multisig transaction
 * @author lu
 * @date 2017.03.05
 */ 
public class CosignMultisigTransaction {

	private String publicKey = null;
	private String privateKey = null;
	private String multisigAddress = null;
	private String multisigPublicKey = null;
	private String innerTransactionHash = null;
	
	public CosignMultisigTransaction(String publicKey, String privateKey, String multisigAddress, String multisigPublicKey, String innerTransactionHash){
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.multisigAddress = multisigAddress;
		this.multisigPublicKey = multisigPublicKey;
		this.innerTransactionHash = innerTransactionHash;
	}
	
	public String send_v1(){
		JSONObject params = new JSONObject();
		// otherHash object
		JSONObject otherHash = new JSONObject();
		otherHash.put("data", this.innerTransactionHash);
		// transaction object
		JSONObject transaction = new JSONObject();
		long nowTime = new Date().getTime();
		transaction.put("timeStamp", Double.valueOf(nowTime/1000).intValue() - Constants.NEMSISTIME);
		transaction.put("fee", 0);
		transaction.put("type", 4098);
		transaction.put("deadline", Double.valueOf(nowTime/1000).intValue() - Constants.NEMSISTIME + 60*60);
		transaction.put("version", 1610612737);
		transaction.put("signer", this.publicKey);
		transaction.put("otherHash", otherHash);
		transaction.put("otherAccount", this.multisigAddress);
		params.put("transaction", transaction);
		params.put("privateKey", this.privateKey);
		System.out.println(params.toString());
		return HttpClientUtils.post(Constants.URL_INIT_TRANSACTION, params.toString());
	}
	
	public String send_v2(String fee){
		// collect parameters
		TimeInstant timeInstant = new SystemTimeProvider().getCurrentTime();
		KeyPair senderKeyPair = new KeyPair(PrivateKey.fromHexString(this.privateKey));
		Account senderAccount = new Account(senderKeyPair);
		Account multisigAccount = new Account(Address.fromPublicKey(PublicKey.fromHexString(this.multisigPublicKey)));
		Hash otherTransactionHash = Hash.fromHexString(this.innerTransactionHash);
		// create multisig signature transaction
		MultisigSignatureTransaction multisigSignatureTransaction = new MultisigSignatureTransaction(
				timeInstant, senderAccount, multisigAccount, otherTransactionHash);
		if(fee==null){
			TransactionFeeCalculatorAfterForkForApp feeCalculator = new TransactionFeeCalculatorAfterForkForApp();
			multisigSignatureTransaction.setFee(feeCalculator.calculateMinimumFee(multisigSignatureTransaction));
		} else {
			multisigSignatureTransaction.setFee(Amount.fromNem(0));
		}
		multisigSignatureTransaction.setDeadline(timeInstant.addHours(23));
		multisigSignatureTransaction.sign();
		JSONObject params = new JSONObject();
		final byte[] data = BinarySerializer.serializeToBytes(multisigSignatureTransaction.asNonVerifiable());
		params.put("data", ByteUtils.toHexString(data));
		params.put("signature", multisigSignatureTransaction.getSignature().toString());
		return HttpClientUtils.post(Constants.URL_TRANSACTION_ANNOUNCE, params.toString());
	}
}
