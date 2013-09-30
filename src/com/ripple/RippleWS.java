package com.ripple;

import java.net.URI;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;
import com.aurionx.wallet.GlobalState;

public class RippleWS implements WebSocketClient.Listener {
	private GlobalState state;
	private String  ripple_server_uri = "wss://s1.ripple.com:443";
	private WebSocketClient websocket;
	private int messageID = 1;
	
	public RippleWS (GlobalState state) {
		this.state = state;
	}
	
	public boolean isConnected() {
		if (websocket == null) return false;
		return websocket.isConnected();
	}
/*	
	public interface Relay {
		public void onConnect();
		public void onMessage(JSONObject object);
		public void onDisconnect(int code, String reason);
		public void onError(Exception error);
	}
*/
	
	public void connect() {
		if (isNetworkAvailable()) {
			if (websocket == null) {
				List<BasicNameValuePair> extraHeaders = null;
				websocket = new WebSocketClient(
						URI.create(ripple_server_uri),
						this, 
						extraHeaders);
	
			} 
			
			websocket.connect();
			System.out.println("connecting to ripple....");
		} 
	}
	
	public void disconnect() {
		websocket.disconnect();
		System.out.println("disconnecting...");
	}
	
	public int getAccountInfo(String address) {
		JSONObject json = new JSONObject();
		try {
			json.put("id", messageID);
			json.put("command", "account_info");
			json.put("account", address);
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		
		sendMessage(json);
		return messageID++;
	}



	public int getAccountLines(String address) {
		JSONObject json = new JSONObject();
		
		try {
			json.put("id", messageID);
			json.put("command", "account_lines");
			json.put("account", address);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

		sendMessage(json);
		return messageID++;
	}
	
	public int subscribe (String address) {
		JSONObject json    = new JSONObject();
		JSONArray accounts = new JSONArray();
		accounts.put(address);
		
		try {
			json.put("id", messageID);
			json.put("command", "subscribe");
			json.put("accounts", accounts);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

		sendMessage(json);
		return messageID++;
	}
	
	public void sendMessage(String message) {
		if (websocket.isConnected()) {
			System.out.println("sending "+message);
			websocket.send(message);
		} else {
			System.out.println("can't send message: websocket not connected");
			System.out.println(message);
		}
	}

	public void sendMessage(JSONObject json) {
		sendMessage(json.toString());
	}
	
	@Override
	public void onConnect() {
		System.out.println("connected");
		state.onRWSConnect();
/*		
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
		    	relay.onConnect();
		    }
		});
*/		
	}


	@Override
	public void onMessage(String message) {
		final JSONObject json;
		try {
			json = new JSONObject(message);
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}

		
		System.out.println(json);
		state.onRWSMessage(json);
		
/*		
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
		    	relay.onMessage(json);
		    }   
		});
*/		
/*		
		// can we figure out what kind of message this was+
		Log.v("ripplewallet","json: "+jsonobject.toString());
		
		// this is weak as we are using the id to figure out what 
		// *type* of transaction this is, rather than actually
		// using it to identify the request
		int transactionID_temp = 0;
		JSONObject result_temp = new JSONObject();
		try {
			transactionID_temp = jsonobject.getInt("id");
			result_temp = jsonobject.getJSONObject("result");					
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final int transactionID = transactionID_temp;
		final JSONObject result = result_temp;
		
		
		
		// omg this is so messy!
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() { 
		    	

				switch (transactionID) { 
				case ID_ACCOUNT_INFO:
					try {
						JSONObject accountData = result.getJSONObject("account_data");
						bankListener.onRippleAccountRetrieved(RippleAccount.fromJSON(accountData));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				break;
				case ID_ACCOUNT_LINES:
			  		//bankListener.onMessage(jsonobject);
			  		// we are expecting an array called lines
			  		JSONArray wallet_array;
			  		RippleAccount account = getAccount();
					try { 
						if (account == null) account = new RippleAccount();
						wallet_array = result.getJSONArray(ID_LINES);
						Log.v("Wallet","JSONArray: "+wallet_array);
						RippleWallet[] wallets = new RippleWallet[wallet_array.length()];
				  	    for(int i = 0 ; i < wallet_array.length(); i++) {
				  	    	wallets[i] = RippleWallet.fromJSON(wallet_array.getJSONObject(i));
				  	    }
				  	    bankListener.onUserAccountWalletsListRetrieved(wallets);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				break;
				case ID_ACCOUNT_OFFERS:
				break;
				case ID_ACCOUNT_TRANSACTIONS:
				break;
				case ID_SIGN:
					try {
						String tx_blob = result.getString("tx_blob");
						bankListener.onTransactionSigned(transactionID, tx_blob);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				break;
				case ID_SUBMIT:
					bankListener.onTransactionSubmitted();
					
				break;
				default:
				}
				
		    }
		});
		*/
	}

	@Override
	public void onMessage(byte[] data) {
		System.out.println(data);
/*
		bankListener.onMessage(data);
		
		final byte[] databytes = data;
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {  
		  		bankListener.onMessage(databytes);
		    }
		});
*/		
	}

	@Override
	public void onDisconnect(int code, String reason) {
		System.out.println("disconnected");
		
		/*
		bankListener.onDisconnect(code, reason);
		
		final int finalcode = code;
		final String finalreason = reason;
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {  
		  		bankListener.onDisconnect(finalcode, finalreason);
		    }
		});
		*/
	}

	@Override
	public void onError(Exception error) {
		//bankListener.onError(error);
	}
	
	public boolean isNetworkAvailable() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) state.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null
					&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null
						&& netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}
}
