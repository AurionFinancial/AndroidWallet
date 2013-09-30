package com.aurionx.wallet;

import java.security.Security;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import android.os.Bundle;
import android.view.Menu;
import android.app.Activity;
import android.content.Intent;


public class MainActivity extends Activity {
	private GlobalState state;
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        
        state = ((GlobalState) getApplicationContext());

        Bundle extras  = getIntent().getExtras(); 
        Boolean clear = extras != null ? extras.getBoolean("clearData") : false;
        if (clear) {
        	state.clearData();
        	System.out.println("clear data");
        	Intent intent  = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
        	startActivity (intent);
        	finish();
        	return;
        }
        
        if (state.pinRequired() && state.hasPin()) {
        	Intent i = new Intent(MainActivity.this, PinActivity.class);
        	i.putExtra("pinAuth", true);
        	startActivityForResult (i, 2);
        }
        
        else {
        	chooseActivity();
        }
    }
    
    @Override
    public void onRestart() {
    	super.onRestart();
    	System.out.println("restart main");
    	//this.finish();
    	//chooseActivity();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	System.out.println("resume main");
    	//this.finish();
    	//chooseActivity();
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	System.out.println(requestCode);
    	System.out.println(resultCode);
    	
    	if (requestCode == 2) {
    		if (resultCode == RESULT_OK) {chooseActivity();}	
    		if (resultCode == RESULT_CANCELED) {finish();}
    	}
    }
    
    private void chooseActivity ()
    {
		if (state.hasBlob()) {
			startActivity(new Intent(MainActivity.this, WalletActivity.class));
			finish();
			
    	} else { 
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }    	
    }
    
}
