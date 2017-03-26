package com.dfintech.nem.apps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.dfintech.nem.apps.utils.Constants;
import com.dfintech.nem.apps.utils.DefaultSetting;
import com.dfintech.nem.apps.utils.HelperUtils;
import com.dfintech.nem.apps.utils.OutputMessage;
import com.dfintech.nem.apps.utils.ScannerUtil;
import com.dfintech.nem.apps.ws.handlers.WsMonitorImcomingHandler;

/** 
 * @Description: Main class - test monitor incoming transactions
 * @author lu
 * @date 2017.03.07
 */
public class ImplMonitorIncomingTransaction {

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
		// set host, port and websocket port
		DefaultSetting.setHostAndPort(params.get("host"), params.get("port"), params.get("wsPort"));
		final String address = params.get("address");
		final String WS_URI = DefaultSetting.getWsUri();
		// create WebSocket client
		List<Transport> transports = new ArrayList<Transport>(1);
		transports.add(new WebSocketTransport(new StandardWebSocketClient()));
		WebSocketClient transport = new SockJsClient(transports);
		WebSocketStompClient stompClient = new WebSocketStompClient(transport);
		stompClient.setMessageConverter(new StringMessageConverter());
		StompSessionHandler handler = new WsMonitorImcomingHandler(address);
		stompClient.connect(WS_URI, handler);
		//block and monitor exit action
		ScannerUtil.monitorExit();
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
		options.addOption(Option.builder("wsPort").hasArg().build());
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
			System.out.println(HelperUtils.printHelper(Constants.HELPER_FILE_MONITOR_INCOMING_TRANSACTION));
			return null;
		}
		String address = commandLine.getOptionValue("address")==null?"":commandLine.getOptionValue("address").replaceAll("-", "");
		String host = commandLine.getOptionValue("host");
		String port = commandLine.getOptionValue("port");
		String wsPort = commandLine.getOptionValue("wsPort");
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
		// check websocket port
		if(wsPort!=null && !wsPort.matches("[0-9]{1,5}")){
			OutputMessage.error("invalid parameter [wsPort]");
			return null;
		}
		params.put("address", address);
		params.put("host", host);
		params.put("port", port);
		params.put("wsPort", wsPort);
		return params;
	}
}
