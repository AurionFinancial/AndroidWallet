package com.aurionx.wallet;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BalancesListAdapter extends BaseAdapter {
	private static ArrayList<BalanceItem> balancesList;
 
	private Integer[] imgid = {
	};
 
	private LayoutInflater l_Inflater;

	public BalancesListAdapter(Context context, ArrayList<BalanceItem> results) {
		balancesList = results;
		l_Inflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return balancesList.size();
	}

	public Object getItem(int position) {
		return balancesList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		double balance;
		DecimalFormat formatter = new DecimalFormat("#,###.######");
		
		if (convertView == null) {
			convertView = l_Inflater.inflate(R.layout.balance_item, null);
			holder = new ViewHolder();
			holder.currency     = (TextView)  convertView.findViewById(R.id.currency);
			holder.currencyName = (TextView)  convertView.findViewById(R.id.currencyName);
			holder.balance 		= (TextView)  convertView.findViewById(R.id.balance);
			//holder.image 		= (ImageView) convertView.findViewById(R.id.currencyImage);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
  
		balance = balancesList.get(position).getBalance();
		
		holder.currency.setText(balancesList.get(position).getCurrency());
		holder.currencyName.setText(balancesList.get(position).getCurrencyName());
		holder.balance.setText(balance != 0 ? formatter.format(balance) : "0");
		//holder.image.setImageResource(imgid[balancesList.get(position).getImageNumber() - 1]);
		
		return convertView;
	}

	static class ViewHolder {
		TextView  currency;
		TextView  currencyName;
		TextView  balance;
		ImageView image;
	}
}