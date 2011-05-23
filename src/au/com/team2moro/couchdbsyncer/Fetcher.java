package au.com.team2moro.couchdbsyncer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class Fetcher {
	
	public static final String TAG = "Fetcher";
	public static final int BUFFER_SIZE = 8196;
	private String username, password;
	
	public Fetcher() {
	}

	public Fetcher(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public byte[] fetchBytes(URL url) throws IOException {
		return fetchBytes(url, null);
	}

	public byte[] fetchBytes(URL url, String body) throws IOException {

		Log.d(TAG, "fetching URL: " + url);
		URLConnection urlConnection = url.openConnection();
		
		if(username != null && password != null) {
			String userpass = username + ":" + password;
			String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
			urlConnection.setRequestProperty ("Authorization", basicAuth);
		}
		
		if(body != null) {
			Log.d(TAG, "body: " + body);
			urlConnection.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
			out.write(body);
			out.close();
		}
		
		//String contentType = urlConnection.getContentType();
	    InputStream raw = urlConnection.getInputStream();
	    InputStream in = new BufferedInputStream(raw, BUFFER_SIZE);
	    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	    
	    int bytesRead = 0;
	    int offset = 0;
	    byte[] data = new byte[BUFFER_SIZE];
	    while (true) {
	      bytesRead = in.read(data, 0, BUFFER_SIZE);
	      if (bytesRead == -1)
	        break;
	      
	      //Log.d(TAG, "read " + bytesRead + " bytes");
	      byteStream.write(data, 0, bytesRead);
	      offset += bytesRead;
	    }
	    in.close();
	    
	    //Log.d(TAG, "returning byte array");
	    return byteStream.toByteArray();
	}
	
	public String fetchString(URL url, String body) throws IOException {
		byte[] data = fetchBytes(url, body);
		return new String(data, "UTF-8");
	}
	public String fetchString(URL url) throws IOException {
		return fetchString(url, null);
	}

	public Map<String, Object> fetchJSON(URL url, String body) throws IOException {
		String json = fetchString(url, body);
		try {
			return parseJSON(json);
		} catch(Exception e) {
			Log.d(TAG, "exception parsing JSON: " + e);
		}

		return null;
	}
	
	public Map<String, Object> fetchJSON(URL url) throws IOException {
		return fetchJSON(url, null);
	}
		
	@SuppressWarnings("unchecked")
	private Map<String, Object> parseJSON(String json) throws JSONException {
		//Log.d(TAG, "parsing json: " + json);
		JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
		return (Map<String, Object>) parseJSONObject(object);
	}
	
	@SuppressWarnings("unchecked")
	private Object parseJSONObject(Object object) throws JSONException {
		Object result = null;
		
		if(object instanceof JSONArray) {
			// list of values
			//Log.d(TAG, "parsing array");
			JSONArray input = (JSONArray) object;
			List<Object> list = new ArrayList<Object>();
			for(int i = 0; i < input.length(); i++) {
				Object listval = parseJSONObject(input.get(i));
				list.add(listval);
			}
			result = list;
		}
		else if(object instanceof JSONObject) {
			// mapping of key -> value
			//Log.d(TAG, "parsing hash");
			JSONObject input = (JSONObject) object;
			Iterator<String> keys = input.keys();
			Map<String, Object> map = new HashMap<String, Object>();
			while(keys.hasNext()) {
				String key = (String) keys.next();
				Object hashval = parseJSONObject(input.get(key));
				map.put(key, hashval);
			}
			result = map;
		}
		else {
			//Log.d(TAG, "parsing type: " + object.getClass().getName());
			result = object;
		}
		
		//Log.d(TAG, "result: " + result);

		return result;
	}

}
