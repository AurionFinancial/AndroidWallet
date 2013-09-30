package com.aurionx.wallet;

import android.R.color;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class PinActivity extends Activity implements OnClickListener {
	private GlobalState state;
	private String compare;
	private TextView pin1;
	private TextView pin2;
	private TextView pin3;
	private TextView pin4;
	private TextView message;
	private Boolean  pinAuth;
	private String   first;
	private int		 color;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        state   = ((GlobalState) getApplicationContext());
        message = (TextView) findViewById(R.id.pinMessage);
        
        pin1 = (TextView) findViewById(R.id.pinBox0);
        pin2 = (TextView) findViewById(R.id.pinBox1);
        pin3 = (TextView) findViewById(R.id.pinBox2);
        pin4 = (TextView) findViewById(R.id.pinBox3);
        
        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.button3);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.button4);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.button5);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.button6);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.button7);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.button8);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.button9);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.button0);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.buttonExit);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.buttonDelete);
        button.setOnClickListener(this);
        compare  = "";
        
        Bundle extras = getIntent().getExtras(); 
        pinAuth       = extras != null ? extras.getBoolean("pinAuth") : false;
        if (!pinAuth) {
        	color = Color.rgb(60, 120, 220);
        	message.setTextColor(color);
        	message.setText("enter new pin code");
        }
    }
    
    @Override
	public void onClick(View clicked) {
		// TODO Auto-generated method stub	
    	System.out.println(clicked);
    	Button button = (Button) clicked;
        
        if (button.getId()==R.id.buttonExit) {
        	setResult(RESULT_CANCELED);
			finish();
        } else if (button.getId()==R.id.buttonDelete) {
        	if (compare.length()>0) 
        		compare = compare.substring(0, compare.length()-1);
        	this.showPins();
        } else {
        	
        	if (compare.length()>3) return;
        	else {
        		compare += button.getText().toString();
        		this.showPins();
        		if (compare.length()==4) {
        			if (!pinAuth) {
        				if (first == null) {
        					first   = compare;
        					compare = "";
        					message.setTextColor(color);
        					message.setText("re-enter pin code");
        					this.showPins();
        				} else if (!first.equals(compare)) {
        					first   = null;
        					compare = "";
        					message.setTextColor(Color.RED);
        					message.setText("pin codes do not match - enter new pin code");
        					this.showPins();
        				} else {
        					state.setPin(compare);
        					message.setTextColor(color);
        					message.setText("pin code set");
        					setResult(RESULT_OK);
        			        final Handler handler = new Handler();
        			        handler.postDelayed(finishRunnable, 600);
        				}
        				
        				
        				
        			} else if (state.isCorrectPin(compare)) {
        				System.out.println("correct pin");
        				message.setText("");
        				setResult(RESULT_OK);
    			        final Handler handler = new Handler();
    			        handler.postDelayed(finishRunnable, 300);
        				
        			} else {
        				System.out.println("incorrect");
        				message.setText("incorrect pin code");
        				compare = "";
        				this.showPins();
        			}
        		} 
        	}
        }
        
        System.out.println(compare);
	}
    
    private void showPins()
    {
    	// circle-dot 	\u25c9
    	// circle		\u2b55
    	// large dot 	\u2b24
    	// bullet 		\u2022
    	//CharSequence symbol = "\u2022";
    	
    	int length = compare.length();
    	pin1.setBackgroundResource(length>0 ? R.drawable.pin_filled : R.drawable.pin_entry);
    	pin2.setBackgroundResource(length>1 ? R.drawable.pin_filled : R.drawable.pin_entry);
    	pin3.setBackgroundResource(length>2 ? R.drawable.pin_filled : R.drawable.pin_entry);
    	pin4.setBackgroundResource(length>3 ? R.drawable.pin_filled : R.drawable.pin_entry);
    	
    	//pin1.setText(length>0 ? symbol : "");
    	//pin2.setText(length>1 ? symbol : "");
    	//pin3.setText(length>2 ? symbol : "");
    	//pin4.setText(length>3 ? symbol : "");
    }
    
    private Runnable finishRunnable = new Runnable() {

        @Override
        public void run() {
            finish();

        }
    };
}