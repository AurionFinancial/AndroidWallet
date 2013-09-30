package com.ripple;

import android.content.Context;
import org.json.JSONObject;
import com.codebutler.android_websockets.WebSocketClient;
import com.aurionx.wallet.R;
import com.ripple.Blobvault;


public class Account {
	private Context C;
	private JSONObject blob;
	
	public Account (Context Context) {
		this.C = Context;
	}
}
