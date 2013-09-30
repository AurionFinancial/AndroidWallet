package com.aurionx.wallet;
	
public final class BalanceItem {
	private String currency;
	private double balance;
	private int    imageNumber;
	
	public String getCurrency() {
		return currency;
	}

	public String getCurrencyName() {
		//return "name of currency";
		return currencyName.get(currency);
	}
	
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public double getBalance() {
		return balance;
	}
	
	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	public int getImageNumber() {
		return imageNumber;
	}
	
	public void setImageNumber(int imageNumber) {
		this.imageNumber = imageNumber;
	}
}

