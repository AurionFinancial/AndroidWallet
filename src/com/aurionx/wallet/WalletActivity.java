package com.aurionx.wallet;

import java.util.ArrayList;

import com.ripple.NetworkUtil;
import com.ripple.RippleWS;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class WalletActivity extends Activity {
	private static final int RESULT_SETTINGS = 1;
	private TextView 	addressView;
	private TextView    messageBox;
	private String 		address;
	private GlobalState state;
	
	ArrayList<BalanceItem> balances;
    BalancesListAdapter    listAdapter;
    ListView               balancesView;  

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_wallet); 
        ActionBar actionBar = getActionBar();
        actionBar.show();

        state        = ((GlobalState) getApplicationContext());
        balancesView = (ListView) findViewById(R.id.balances); 
        addressView  = (TextView) findViewById(R.id.address);
        messageBox   = (TextView) findViewById(R.id.balanceMessage);
        
        if (!state.hasBlob()) finish();
        //else if (!state.walletLoaded()) state.loadWallet();
        
        address = state.getAddress();
        addressView.setText(address);
        IntentFilter filter = new IntentFilter("balances.updated"); 
        registerReceiver(receiver, filter);
        filter = new IntentFilter("unfunded"); 
        registerReceiver(receiver, filter);
        updateBalances();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.action_settings:
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, RESULT_SETTINGS);
            break;
 
        }
 
        return true;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
    
    @Override
    public void onRestart() {
    	super.onRestart();
    	
    	System.out.println("restart wallet");
    	if (!state.hasBlob()) finish();
    }
    
    private BroadcastReceiver receiver = new BroadcastReceiver() { 
        
        @Override
        public void onReceive(Context context, Intent intent) { 
        	String action = intent.getAction();
        	System.out.println(action);
            if (action.equals("balances.updated")) {
            	messageBox.setText("");
            	updateBalances();
            } else if (action.equals("unfunded")) {
            	messageBox.setText("unfunded account");
            }
        } 
    };
    
    public void updateBalances () {
        balances = state.getBalances();
        messageBox.setText("loading balances...");
        if (balances == null) {
        	state.loadWallet();
        	System.out.println("balances are null");
        	return;
        } else if (!balances.isEmpty()) {
        	messageBox.setText("");
        }
        
        balancesView.setAdapter(new BalancesListAdapter(this, balances));    	
        Toast.makeText(getApplicationContext(), "updated balances", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onPause () {
    	super.onPause();
    	System.out.println("paused");
    	
    }
    
    @Override
    protected void onResume () {
    	super.onResume();
    	System.out.println("resume");
    	if (!state.hasBlob()) finish();
    	else {
    		if (balances == null ||
    			balances.isEmpty()) 
    			messageBox.setText("loading balances...");
    		state.loadWallet();
    	}
    }
    
	public class NetworkState extends BroadcastReceiver {
		 
	    @Override
	    public void onReceive(final Context context, final Intent intent) {
	        String status = NetworkUtil.getConnectivityStatusString(context);
	        System.out.println(status);
	        //Toast.makeText(context, status, Toast.LENGTH_LONG).show();
	    	int s = NetworkUtil.getConnectivityStatus(context);
	    	if (s==NetworkUtil.TYPE_WIFI || s==NetworkUtil.TYPE_MOBILE) {
	    		state.loadWallet();
	    	}
	    }
	}
    
}