package com.ripple;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NetworkState extends BroadcastReceiver {
	 
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);
        System.out.println(status);
        //Toast.makeText(context, status, Toast.LENGTH_LONG).show();
    }
}