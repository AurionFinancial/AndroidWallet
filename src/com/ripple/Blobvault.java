package com.ripple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.modes.CCMBlockCipher;
import org.spongycastle.crypto.params.CCMParameters;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.jcajce.provider.digest.SHA256.KeyGenerator;

import com.aurionx.wallet.GlobalState;
import com.aurionx.wallet.LoginActivity;
import com.aurionx.wallet.R;
import com.aurionx.wallet.WalletActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class Blobvault {
	private LoginActivity login;
	private String key;
	
	public Blobvault (LoginActivity activity) {
		this.login = activity;
	}
	
	public Blobvault () {
	}
	
	public void importWallet (String name, String pass) {		
		String hash = this.hash(name.toLowerCase()+pass);
		String url  = login.getApplicationContext().getString(R.string.payward)+"/"+hash;
		key         = ""+name.length()+'|'+name.toLowerCase()+pass;	
		//System.out.println(url);
		if (NetworkUtil.getConnectivityStatus(login.getApplicationContext())==NetworkUtil.TYPE_NOT_CONNECTED) {
			login.showMessage("Network not connected.");
			return;
		} else {
			new GetWalletFromURL().execute(url);
		}
	}
	
	public String hash(String text) {
	    MessageDigest digest;
	    StringBuffer sb = new StringBuffer();
	    
		try {
			digest = MessageDigest.getInstance("SHA-256");
		    digest.reset();
		    byte[] byteData = digest.digest(text.getBytes("UTF-8"));

		    for (int i = 0; i < byteData.length; i++){
		      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		    }
		    
		    
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return sb.toString();
	}
	
	private class GetWalletFromURL extends AsyncTask<String, String, String>{
	
		@Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	
	        try {
	            response = httpclient.execute(new HttpGet(uri[0]));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
	        
	        return responseString;
	    }
	
	    @Override
	    protected void onPostExecute(String result) {
	    	JSONObject json;
	    	String data;
	    	
	        super.onPostExecute(result);
	        //Do anything with response..
	        if (result == null) {
	        	login.showMessage("Unable to retrieve wallet.");
	        } else if (result.length()>0) {
	        	//System.out.println(result);
	        	login.showMessage("wallet found. Decrypting wallet...");
	        	byte[] bytes = Base64.decode(result, Base64.DEFAULT);
	        	
				try {
					data = new String(bytes, "UTF-8");
					json = new JSONObject(data);
					//String adata = Uri.decode(json.getString("adata"));
					//json.put("adata", new JSONObject(adata));
					
					//System.out.println(json);

					JSONObject blob = decryptBlob (key, json);
					if (blob != null) {
						login.showMessage("Wallet decrypted.  Loading wallet...");
						login.setBlob(blob);
						
					} else {
						login.showMessage("Unable to decrypt wallet.");
					}
					
					return;
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				login.showMessage("Unable to decode wallet.");
				
	        } else {	
	        	login.showMessage("Login failed. Wallet name or passphrase is incorrect");
	        }
	    }
	}
	
	public JSONObject encryptBlob (String key, JSONObject blob) {
		JSONObject result   = new JSONObject();
		SecureRandom random = new SecureRandom();
		byte[] salt = Base64.encode("FSMRjkIkKiHReq5Pdjm863".getBytes(), Base64.DEFAULT);
		byte[] iv   = new byte[16];
		int ks      = 256;
		int iter    = 1000;
		int ts      = 64;
		
		random.nextBytes(iv);
		byte[] nonce = Arrays.copyOf(iv, 13); //truncate at 13 bytes
		
		KeyParameter keyParam = this.createKey(key, salt, iter, ks);	
		CCMParameters ccm = new CCMParameters(
                keyParam,
                ts/8-1, //not sure why but it works
                nonce,
                new byte [0]);
		
		CCMBlockCipher aes = new CCMBlockCipher(new AESFastEngine());
		aes.init(true, ccm);
		

		try {
			
			byte [] plainBytes = blob.toString().getBytes("UTF-8");
			byte [] enc        = new byte[aes.getOutputSize(plainBytes.length)];
			
			int res = aes.processBytes(
	        		plainBytes,
	        		0, 
	        		plainBytes.length, 
	        		enc, 
	        		0);
			
			aes.doFinal(enc, res);
	        
	        result.put("ct",    Base64.encodeToString(enc, Base64.DEFAULT));
	        result.put("iv",    Base64.encodeToString(iv, Base64.DEFAULT));
	        result.put("salt",  Base64.encodeToString(salt, Base64.DEFAULT));
	        result.put("adata", Base64.encodeToString("[]".getBytes(), Base64.DEFAULT));
	        result.put("ks",   ks);
	        result.put("iter", iter);
	        result.put("ts",   ts);
	        
	        //System.out.println(result);
	        return result;
			
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCipherTextException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 return result;
		
	}
	
    public JSONObject decryptBlob (String key, JSONObject json) {
		try {

			KeyParameter keyParam = this.createKey(
					key, 
					Base64.decode(json.getString("salt"), Base64.DEFAULT),
					json.getInt("iter"), 
					json.getInt("ks"));
			
			byte[] iv    = Base64.decode(json.getString("iv"), Base64.DEFAULT);
			byte[] nonce = Arrays.copyOf(iv, 13); //truncate at 13 bytes
			
			CCMParameters ccm = new CCMParameters(
                    keyParam,
                    json.getInt("ts")/8-1, //not sure why but it works
                    nonce,
                    new byte [0]);
            
		    CCMBlockCipher aes = new CCMBlockCipher(new AESFastEngine());
		    aes.init(false, ccm);

		    byte[] cipherText = Base64.decode(json.getString("ct"), Base64.DEFAULT);
		    byte[] plainBytes = new byte[aes.getOutputSize(cipherText.length)];
		    
		    //System.out.println(byteArrayToHexString(cipherText));
		    //System.out.println(aes.getAlgorithmName());
            int res = aes.processBytes(
            		cipherText,
            		0, 
            		cipherText.length, 
            		plainBytes, 
            		0);
            
            
			aes.doFinal(plainBytes, res);
            String text = new String(plainBytes, "UTF-8");
            //System.out.println(text);
            return new JSONObject(text);
		    
		    
		    
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataLengthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCipherTextException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		return null;
    }
    
    private KeyParameter createKey(String password, byte[] salt,
    		int iterations, int keySizeInBits) {
    	//System.out.println(keySizeInBits);
    	//System.out.println(iterations);
    	//System.out.println(salt);
    	//System.out.println(password);
    	
    	PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(
    			new SHA256Digest());
    	generator.init(
    			PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password.toCharArray()), 
    			salt, 
    			iterations);
    	KeyParameter key = (KeyParameter) generator
    			.generateDerivedMacParameters(keySizeInBits);
    	return key;
    }
     
    private String byteArrayToHexString(byte[] bytes) {
    	StringBuilder result = new StringBuilder();
    	for (byte b : bytes) {
    		result.append(String.format("%02X", b));
    	}
    	return result.toString().toLowerCase();
    }
}