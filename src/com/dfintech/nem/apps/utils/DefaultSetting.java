package com.dfintech.nem.apps.utils;

import org.nem.core.model.NetworkInfos;

/** 
 * @Description: some settings
 * @author lu
 * @date 2017.03.21
 */ 
public class DefaultSetting {

	/**
	 * set default network
	 */
	public static void setDefaultNetwork() {
		NetworkInfos.setDefault(NetworkInfos.fromFriendlyName(Constants.NETWORK_NAME));
	}
	
	/**
	 * set host, port and websocket port
	 * @param host
	 * @param port
	 * @param websocket port
	 */
	public static void setHostAndPort(String host, String port, String wsPort) {
		if(host!=null)
			HttpClientUtils.defaultHost = host;
		if(port!=null)
			HttpClientUtils.defaultPort = port;
		if(wsPort!=null)
			HttpClientUtils.defaultWsPort = wsPort;
	}
	
	/**
	 * get WebSocket Default URI
	 * @return
	 */
	public static String getWsUri(){
		StringBuilder builder = new StringBuilder();
		builder.append("ws://")
			.append(HttpClientUtils.defaultHost)
			.append(":")
			.append(HttpClientUtils.defaultWsPort)
			.append(Constants.URL_WS_W_MESSAGES);
		return builder.toString();
	}
}
