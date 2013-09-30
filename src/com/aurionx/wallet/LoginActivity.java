package com.aurionx.wallet;

import org.json.JSONObject;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ripple.Account;
import com.ripple.Blobvault;

public class LoginActivity extends Activity implements OnClickListener {
	private Button signIn;
	private EditText walletName, passphrase;
	private Blobvault blobvault;
	private GlobalState state;
	private TextView messageBox;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        messageBox = (TextView) this.findViewById(R.id.loginMessage);
        blobvault  = new Blobvault(this);
        state      = ((GlobalState) getApplicationContext());
        signIn     = (Button) this.findViewById(R.id.signIn);
        signIn.setOnClickListener(this);      
    }

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		walletName = (EditText) this.findViewById(R.id.walletName);
		passphrase = (EditText) this.findViewById(R.id.passphrase);
		String name = walletName.getText().toString();
		String pass = passphrase.getText().toString();

		if (name.length() == 0 || pass.length() == 0) {
			showMessage("Wallet name and passphrase are required.");
		} else {
			showMessage("Fetching wallet...");
			blobvault.importWallet(name, pass);	
		}
	}
	
	public void showMessage (String message) 
	{
	/*	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
			.setCancelable(true)
			.setNeutralButton("OK",new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	*/	
		
		messageBox.setText(message);
		System.out.println(message);
	}
	
	public void setBlob (JSONObject blob)
	{
		
		state.setBlob(blob);
		state.loadWallet();
		
		startActivity(new Intent(this, WalletActivity.class));
		finish();
	}
}