package com.aurionx.wallet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class currencyName {
    private static final Map<String, String> map;
    
    static {
        final HashMap<String, String> m = new HashMap<String, String>();
        
        m.put("XRP", "Ripples");
        m.put("USD", "U.S. Dollar");
        m.put("BTC", "Bitcoin");
        m.put("GDW", "Goodwill");
        m.put("EUR", "Euro");
        m.put("JPY", "Japanese Yen");
        m.put("CNY", "Chinese Yuan");
        m.put("INR", "Indian Rupee");
        m.put("RUB", "Russian Ruble");
        m.put("GBP", "British Pound");
        m.put("CAD", "Canadian Dollar");
        m.put("BRL", "Brazilian Real");
        m.put("CHF", "Swiss Franc");
        m.put("DKK", "Danish Krone");
        m.put("NOK", "Norwegian Krone");
        m.put("SEK", "Swedish Krona");
        m.put("CZK", "Czech Koruna");
        m.put("PLN", "Polish Zloty");
        m.put("AUD", "Australian Dollar");
        m.put("MXN", "Mexican Peso");
        m.put("KRW", "South Korean Won");
        m.put("TWD", "New Taiwan Dollar");
        m.put("HKD", "Hong Kong Dollar");
        m.put("YTL", "Turkish Lira");
        m.put("XAU", "Gold (troy oz.)");
        m.put("XAG", "Silver (troy oz.)");
        
        map = Collections.unmodifiableMap(m);
    }
    
	public static String get(String currency) {
		String name = map.containsKey(currency) ? map.get(currency) : "";
		return name;
	}
	
}