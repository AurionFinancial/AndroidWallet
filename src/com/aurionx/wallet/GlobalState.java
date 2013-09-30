package com.aurionx.wallet;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ripple.Blobvault;
import com.ripple.RippleWS;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class GlobalState extends Application {
	private JSONObject     blob;
	private RippleWS       rippleWS;
	private String         address;
	private ArrayList<BalanceItem> balances;
	private SharedPreferences rippleData;
	private SharedPreferences settings;
	private Blobvault	   blobvault;
	private String         encKey;
	
	@Override
	public void onCreate() {
		super.onCreate();
		encKey	   = "change this";  //change this in production
		blobvault  = new Blobvault();
		settings   = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        rippleData = this.getSharedPreferences("rippleData", MODE_PRIVATE);
		rippleWS   = new RippleWS(this);
        
		
        String encBlob = rippleData.getString("blob", null);
        
        if (encBlob != null) {

        	try {
        		this.blob = blobvault.decryptBlob(encKey, new JSONObject(encBlob));
        		//this.loadWallet();
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
        }
	}
	
	public void setBlob (JSONObject blob) {
		this.blob = blob;

		//encrypt and save blob in the persistent data;
    	JSONObject enc = blobvault.encryptBlob(encKey, blob);
		Editor editor = rippleData.edit();
		editor.putString("blob", enc.toString());
		editor.commit(); // commit changes
		
	}
	
	public void loadWallet()
	{
		System.out.println("loading wallet...");
		address  = getAddress();
		balances = new ArrayList<BalanceItem>(); //reset balances
		
		if (address == null) return;
		if (!rippleWS.isConnected()) rippleWS.connect();
        else {
        	rippleWS.getAccountInfo(address);
        	rippleWS.getAccountLines(address);
        	rippleWS.subscribe(address);
        }
	}
	
	public Boolean walletLoaded ()
	{
		if (address != null && balances != null) return true;
		return false;
	}
	
	public String getAddress() {

		if (this.blob != null) {
			try {
				return blob.getString("account_id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}
		
		return null;
	}
	
	public ArrayList<BalanceItem> getBalances() {
		return balances;
	}
	
	public Boolean rememberWallet() {
		//return settings.getBoolean("rememberWallet", false);
		return true;
	}
	
	public void clearData() {
		Editor editor = rippleData.edit();
		editor.clear();
		editor.commit(); 
		
		editor = settings.edit();
		editor.clear();
		editor.commit();
		
		blob    = null;
		address = null;
	}
	
	public Boolean hasBlob () {
		return (this.blob != null) ? true : false;
	}
	
	public JSONObject getBlob () {
		return this.blob;
	}
	
	public RippleWS getRippleWS () {
		return this.rippleWS;
	}
	
	public Boolean pinRequired () {
		return settings.getBoolean("requirePin", false);
	}
	
	public Boolean hasPin () {
		String pin = settings.getString("pinCode", null);
		return (pin != null && pin.length()>0) ? true : false;
	}
	
	public Boolean isCorrectPin (String compare)
	{
		String pin = settings.getString("pinCode", "");
		return pin.equals(compare) ? true : false;
	}
 
	public void setPin (String pin) 
	{
		Editor editor = settings.edit();
		editor.putString("pinCode", pin);
		editor.commit(); // commit changes
	}

	public void onRWSConnect() {
		System.out.println("rws connect");
		address = getAddress();
    	if (address != null) {
			rippleWS.getAccountInfo(address);
			rippleWS.getAccountLines(address);
			rippleWS.subscribe(address);
    	}
		
	}

	public void onRWSMessage(JSONObject message) {
		try {
			routeMessage (message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void onRWSError(Exception error) {
	}

	public void onRWSDisconnect(int code, String reason) {
	}
	
	public void routeMessage (JSONObject message) throws JSONException {
		JSONObject result;
		String error;
		
		if (message.has("result")) {
			result = message.getJSONObject("result");
		} else if (message.has("error")) {
			error = message.getString("error");
			if (error.equals("actNotFound")) {
				Intent i = new Intent("unfunded"); 
				getApplicationContext().sendBroadcast(i);
			}
			
			System.out.println(error);
			return;
		} else if (message.has("engine_result")) {
			handleTransaction (message);
			return;
		} else {
			System.out.println("unhandled message");
			System.out.println(message);
			return;
		}
		
		if      (result.has("account_data"))  handleAccountInfo (result);
		else if (result.has("lines"))         handleAccountLines (result);
		else {
			System.out.println("unhandled result");
			System.out.println(result);
		}
		return;
	}
	
	public void handleAccountInfo (JSONObject result) throws JSONException {
		JSONObject data = result.getJSONObject("account_data");
		double XRP       = ((double) data.getInt("Balance")/1000000);
		
		if (balances == null || balances.isEmpty()) {
			balances = new ArrayList<BalanceItem>();
			
	    	BalanceItem item = new BalanceItem();
	    	item.setCurrency("XRP");
	    	item.setBalance(XRP);
	    	balances.add(item);

		} else {
			BalanceItem item = balances.get(0);
	    	item.setBalance(XRP);
		} 	
		
		Intent i = new Intent("balances.update"); 
		getApplicationContext().sendBroadcast(i);
	}
	
	public void handleAccountLines (JSONObject result) throws JSONException {
		JSONArray   lines  = result.getJSONArray("lines");
		int         length = lines.length();
		double      balance, total;
		String      currency;
		JSONObject  line;
		BalanceItem item;
		HashMap<String, Double> balanceList = new HashMap<String, Double>();

		for (int z = 0; z<length; z++) {
			line     = (JSONObject) lines.get(z);
			currency = line.getString("currency");
			balance  = Double.parseDouble(line.getString("balance"));
			total    = balanceList.containsKey(currency) ? balanceList.get(currency) : 0;
			balanceList.put(currency, total+balance);
		}
		
		if (balances==null) {
			item = new BalanceItem();
	    	item.setCurrency("XRP");
	    	item.setBalance((double) 0);
	    	
		} else {
			item = balances.get(0);
		}
		
		balances = new ArrayList<BalanceItem>();
		balances.add(item);
		
	    Iterator it = balanceList.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String,Double> pairs = (Map.Entry<String,Double>)it.next();
	    	item = new BalanceItem();
	    	item.setCurrency(pairs.getKey());
	    	item.setBalance(pairs.getValue());
	    	balances.add(item);
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	    
		Intent i = new Intent("balances.updated"); 
		getApplicationContext().sendBroadcast(i);
	}
	
	public void handleTransaction (JSONObject message) {
		String type, balance, account;
		JSONArray nodes;
		JSONObject meta, node, fields;
		int i, length;
		double XRP;
		
		System.out.println("transaction");
		
		if (address != null) {
			//find XRP balance
			try {
				meta   = message.getJSONObject("meta");
				nodes  = meta.getJSONArray("AffectedNodes");
				length = nodes.length();
								
				for (i = 0; i < length; i++) {
					node    = nodes.getJSONObject(i);
					node    = node.getJSONObject("ModifiedNode");
					fields  = node.getJSONObject("FinalFields"); 
					type    = node.getString("LedgerEntryType");
					account = fields.getString("Account");
					
					if (type.equals("AccountRoot")) {
						account = fields.getString("Account");
			
						if (account.equals(address)) {
						
							balance = fields.getString("Balance");
							XRP     = Double.parseDouble(balance)/1000000;
							
							if (balances == null || balances.isEmpty()) {
								balances = new ArrayList<BalanceItem>();
								
						    	BalanceItem item = new BalanceItem();
						    	item.setCurrency("XRP");
						    	item.setBalance(XRP);
						    	balances.add(item);
		
							} else {
								BalanceItem item = balances.get(0);
						    	item.setBalance(XRP);
							}
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
			rippleWS.getAccountLines(address);
		}
	}
}