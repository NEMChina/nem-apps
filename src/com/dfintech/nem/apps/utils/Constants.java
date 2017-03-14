package com.dfintech.nem.apps.utils;

/** 
 * @Description: some contants
 * @author lu
 * @date 2017.03.07
 */ 
public class Constants {

	public static final long NETWORK_TYPE = 2; //0:testnet, 1:mainnet, 2:mijinnet
	public static final long NEMSISTIME = 1427587585; //the first block time
	public static final long MICRONEMS_IN_NEM = 1000000;
	public static final String URL_INIT_TRANSACTION = "/transaction/prepare-announce";
	public static final String URL_ACCOUNT_GET = "/account/get";
	public static final String URL_ACCOUNT_TRANSFERS_INCOMING = "/account/transfers/incoming";
	public static final String URL_ACCOUNT_TRANSFERS_OUTGOING = "/account/transfers/outgoing";
	public static final String URL_ACCOUNT_GET_FROMPUBLICKEY = "/account/get/from-public-key";
	public static final String URL_ACCOUNT_UNCONFIRMEDTRANSACTIONS = "/account/unconfirmedTransactions";
	public static final String CONTENT_TYPE_TEXT_JSON = "application/json";
}
