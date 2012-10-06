package sk.peterjurkovic.dril.de.updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONReciever {
	
		public static int FOR_CHECK_ACTION = 1;
		public static int FOR_UPDATE_ACTION = 2;
		public static final int LANG_EN_INDEX = 1;
		public static final int LANG_DE_INDEX = 2;
		private  String checkServiceURL = "http://dril.peterjurkovic.sk?lang="+
				LANG_DE_INDEX+"&act="+ FOR_CHECK_ACTION+"&ver=";
		private  String updateServiceURL = "http://dril.peterjurkovic.sk?lang="+
				LANG_DE_INDEX+"&act="+FOR_UPDATE_ACTION+"&ver=";
		
	 	static InputStream is = null;
	    static JSONObject jObj = null;
	    static String json = "";
	    
	    
	    public JSONReciever(long version) {
	    	checkServiceURL = checkServiceURL + version;
	    	updateServiceURL = updateServiceURL + version;
	    }
	    
	    public JSONObject getJSONData(int act) throws IllegalArgumentException {
	    	 
	        // Making HTTP request
	        try {
	            // defaultHttpClient
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            
	            String url = "";
	            if(FOR_CHECK_ACTION == act){
	            	url = checkServiceURL;
	            }else if(FOR_UPDATE_ACTION == act){
	            	url = updateServiceURL;
	            }else{
					throw new IllegalArgumentException("JSONRecievers action is not defined");
				}
	             
	            HttpPost httpPost = new HttpPost(url);
	 
	            HttpResponse httpResponse = httpClient.execute(httpPost);
	            HttpEntity httpEntity = httpResponse.getEntity();
	            is = httpEntity.getContent();           
	 
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	 
	        try {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(
	                    is, "iso-8859-1"), 8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "n");
	            }
	            is.close();
	            json = sb.toString();
	        } catch (Exception e) {
	            Log.e("Buffer Error", "Error converting result " + e.toString());
	        }
	 
	        // try parse the string to a JSON object
	        try {
	            jObj = new JSONObject(json);
	        } catch (JSONException e) {
	            Log.e("JSON Parser", "Error parsing data " + e.toString());
	        }
	 
	        // return JSON String
	        return jObj;
	 
	    }
	    
	    
	    
	    
	    
	    
	    
	    
	 
}
