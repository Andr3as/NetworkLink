package de.andrano.networklink;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

public class GeneralMethod {

	final static public String ERROR_TAG = "NetworkLink_ERROR";
	
	private Resources ressource;
	public static Activity activity;
	private Context context;
	
	public Boolean isDualPane;
	public String exportString = "";
	//Identifier
	public int code_view;
	public int code_new;
	public int code_settings;
	public int code_about;
	public int code_count;
	
	private String fragment_key;
	
	final int READ_CODE = 1;
	final int SAVE_CODE = 2;
	
	public GeneralMethod(Activity fragment) {
		activity 	= fragment;
		context 	= fragment.getApplicationContext();
		ressource 	= activity.getResources();
		
		isDualPane 	= ressource.getBoolean(R.bool.has_two_panes);
		
		//Get resources
		code_view 	= ressource.getInteger(R.integer.code_view);
		code_new 	= ressource.getInteger(R.integer.code_new);
		code_settings = ressource.getInteger(R.integer.code_settings);
		code_about 	= ressource.getInteger(R.integer.code_about);
		code_count 	= ressource.getInteger(R.integer.code_count);
		
		fragment_key = ressource.getString(R.string.settings_extra);
	}
	
	public void updateFirstFragment() {
		FragmentManager manager = activity.getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		Fragment fragment = new OverviewFragment();
		if (isDualPane) {
			transaction.replace(R.id.settings_twoPanes_category, fragment);
			transaction.commit();
		}
	}
	
	public void handleSecondFragment(int item) {
    	handleSecondFragment(item, null, null);
    }
	
	public void handleSecondFragment(int item, String key, String id) {
		if (isDualPane) {
    		FragmentManager manager = activity.getFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			Fragment fragment;
			if (item == code_view) {
				fragment = new DetailsFragment();
			} else if (item == code_new) {
				fragment = new NewFragment();
			} else if (item == code_settings) {
				fragment = new SettingsFragment();
			} else if (item == code_count) {
				fragment = new CountFragment();
			} else {
				return;
			}
			//Put arguments
			if (key != null) {
				Bundle arguments = new Bundle();
	            arguments.putString(key, id);
	            fragment.setArguments(arguments);
			}
			transaction.replace(R.id.settings_twoPanes_area, fragment);
			transaction.commit();
		} else {
			/* start a separate activity */
	        Intent intent = new Intent(context, AreaActivity.class);
	        intent.putExtra(fragment_key, item);
	        //Put arguments
	        if (key != null) {
	        	intent.putExtra(key, id);
	        }
	        activity.startActivity(intent);
		}
	}
	
	public void closeSecondFragment() {
		if (isDualPane) {
			handleSecondFragment(code_view);
		} else {
			activity.finish();
		}
	}
	
	public void showToast(Integer id) {
		Toast.makeText(context, id, Toast.LENGTH_LONG).show();
	}
	
	
	public void openLink(String ssid, String nlink, String dlink) {
		String address = "";
		if (ssid.equals(getCurrentSSID())) {
			address = nlink;
		} else {
			address = dlink;
		}		
		Intent i = new Intent(Intent.ACTION_VIEW);  
        i.setData(Uri.parse(address));  
        activity.startActivity(i);
	}
	
	/* WifiConfigs */
	public List<WifiConfiguration> getNetworks() {
		WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
		List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
		//configs kann null sein, wenn z.B. das Wlan ausgeschaltet ist.
		if (configs == null) {
			return null;
		}
		WifiConfiguration one;
		for (int i = 0; i < configs.size(); i++) {
			one = configs.get(i);
			one.SSID = one.SSID.replaceAll("\"", "");
			configs.set(i, one);
		}
		Collections.sort(configs, configComparator);
		return configs;
	}
	
	public String getCurrentSSID() {
		return getCurrentNetwork().getSSID().replace("\"", "");
	}
	
	public WifiInfo getCurrentNetwork() {
		WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
		return wifiManager.getConnectionInfo();
	}
	
	public Comparator<WifiConfiguration> configComparator = new Comparator<WifiConfiguration>() {
		@Override
		public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
			return lhs.SSID.compareTo(rhs.SSID);
		}
	};
	
	@SuppressLint("InlinedApi")
	public boolean exportEntries(ArrayList<String> ids) {
		SqlHelper sql = new SqlHelper(context);
		JSONArray export = new JSONArray();
		List<HashMap<String, String>> list = sql.getEntries();
		try {
			for (int i = 0; i < list.size(); i++) {
				if (ids.contains(list.get(i).get("id"))) {
					JSONObject jsObj = new JSONObject();
					jsObj.put("title", list.get(i).get("title"));
					jsObj.put("ssid", list.get(i).get("ssid"));
					jsObj.put("network_link", list.get(i).get("network_link"));
					jsObj.put("default_link", list.get(i).get("default_link"));
					export.put(jsObj);
				}
			}
		} catch (Exception e) {
			return false;
		}
		exportString = export.toString();
		try {
			((MainActivity) activity).general.exportString = exportString;
		} catch (Exception e) {
			((AreaActivity) activity).general.exportString = exportString;
		}
		//Save file
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.setType("text/json");
			intent.putExtra(Intent.EXTRA_TITLE, "NetworkLink.json");
			activity.startActivityForResult(intent, SAVE_CODE);
		} else {
			try {
				File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				String path = download.getPath() + "/NetworkLink.json";
				FileOutputStream output = new FileOutputStream(path);
				saveEntries(output);
			} catch (Exception e) {
				showToast(R.string.error);
			}
		}
		return true;
	}
	
	public boolean saveEntries(OutputStream ouput) {
		try {
			ouput.write(exportString.getBytes());
			ouput.flush();
			ouput.close();
			showToast(R.string.saved);
			return true;
		} catch (Exception e) {
			showToast(R.string.error);
			return false;
		}
	}
	
	@SuppressLint("InlinedApi")
	public void importEntries() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.setType("*/*");
			activity.startActivityForResult(intent, READ_CODE);
		} else {
			try {
				File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				String path = download.getPath() + "/NetworkLink.json";
				FileInputStream input = new FileInputStream(path);
				loadEntries(input);
			} catch (Exception e) {
				showToast(R.string.error);
			}
		}
	}
	
	public void loadEntries(InputStream input) {
		try {
			String importString = "";
			StringBuilder builder = new StringBuilder();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
	        String line = reader.readLine();
	        while(line != null){
	            builder.append(line);
	            builder.append('\n');
	            line = reader.readLine();
	        }
	        importString = builder.toString();
			//Decode json
	        SqlHelper sql = new SqlHelper(context);
	        JSONArray json 	= new JSONArray(importString);
			JSONObject obj;
			try {
				for (int i = 0; i < json.length(); i++) {
					obj = json.getJSONObject(i);
					sql.createEntry(obj.getString("title"), obj.getString("ssid"), 
							obj.getString("network_link"), obj.getString("default_link"));
				}
				showToast(R.string.imported);
				updateFirstFragment();
			} catch (JSONException je) {
				showToast(R.string.error_json);
			}
		} catch (Exception e) {
			showToast(R.string.error);
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == Activity.RESULT_OK && data != null) {
    		if (requestCode == SAVE_CODE) {
				try {
					OutputStream ouput = activity.getContentResolver().openOutputStream(data.getData());
					saveEntries(ouput);
				} catch (Exception e) {
					showToast(R.string.error);
				}
			} else if (requestCode == READ_CODE) {
				try {
					InputStream input = activity.getContentResolver().openInputStream(data.getData());
					loadEntries(input);
				} catch (Exception e) {
					showToast(R.string.error);
				}
			}
		}
	}
}
