package de.andrano.networklink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlHelper extends  SQLiteOpenHelper {

	public static int version 	= 1;
	public static String name	= "NetworkLinks";
	
	private Context context;
	private Resources resources;
	private SQLiteDatabase db;
	
	public SqlHelper(Context context) {
		super(context, name, null, version);
		this.context = context;
		this.resources = context.getResources();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.setLocale(Locale.getDefault());
		db.execSQL(context.getResources().getString(R.string.sql_create));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if ( (oldVersion == 0) && (newVersion == 1) ) {
			// Drop older table if existed
	        db.execSQL(context.getResources().getString(R.string.sql_drop));
	        // Create tables again
	        onCreate(db);
		}
	}
	
	public boolean createEntry(String title, String ssid, String network_link, String default_link) {
		db = getWritableDatabase();
		ContentValues values = addToValues(title, ssid, network_link, default_link);
		long row = db.insert("links", null, values);
		if (row == -1) {
			return false;
		}
		return true;
	}
	
	public List<HashMap<String, String>> getEntries() {
		Cursor result	= query(context.getResources().getString(R.string.sql_select));
		if (result == null) {
			return null;
		} else {
			int id_pos 		= result.getColumnIndex("id");
			int title_pos 	= result.getColumnIndex("title");
			int ssid_pos	= result.getColumnIndex("ssid");
			int network_link= result.getColumnIndex("network_link");
			int default_link= result.getColumnIndex("default_link");
			//fehlerhafte Abfrage abfangen
			if ( (id_pos == -1) || (title_pos == -1) || (ssid_pos == -1) || (network_link == -1) || (default_link == -1)) {
				return null;
			}
			else
			{
				List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
				result.moveToFirst();
				for (int i = 0; i < result.getCount(); i++) {
					HashMap<String, String> map = new HashMap<String, String>();
					Integer id = result.getInt(id_pos);
					map.put("id", id.toString());
					map.put("title", result.getString(title_pos));
		            map.put("ssid", result.getString(ssid_pos));
		            map.put("network_link", result.getString(network_link));
		            map.put("default_link", result.getString(default_link));
		            list.add(map);
					result.moveToNext();
				}
				return list;
			}
		}
	}
	
	public HashMap<String, String> getEntry(int id) {
		Cursor result	= query("SELECT * FROM links Where id = '" + String.valueOf(id) + "'");
		return parseOneResult(result);
	}
	
	public HashMap<String, String> getFirstEntry() {
		Cursor result = query(resources.getString(R.string.sql_select_first));
		return parseOneResult(result);
	}
	
	public boolean deleteEntry(String id) {
		db = getWritableDatabase();
		int result = db.delete(resources.getString(R.string.sql_table), "id = ?", new String[]{id});
		if (result == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean updateEntry(String id, String title, String ssid, 
			String network_link, String default_link) {
		db = getWritableDatabase();
		ContentValues values = addToValues(title, ssid, network_link, default_link);
		int result = db.update(resources.getString(R.string.sql_table), values, "id = ?", new String[]{id});
		if (result == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	private Cursor query(String query) {
		db 		 		= getReadableDatabase();
		Cursor result	= db.rawQuery(query, null);
		if (result.getCount() == 0) {
			return null;
		} else {
			return result;
		}
	}
	
	private HashMap<String, String> parseOneResult(Cursor result) {
		if (result == null) {
			return null;
		} else {
			int id_pos 		= result.getColumnIndex("id");
			int title_pos 	= result.getColumnIndex("title");
			int ssid_pos	= result.getColumnIndex("ssid");
			int network_link= result.getColumnIndex("network_link");
			int default_link= result.getColumnIndex("default_link");
			//fehlerhafte Abfrage abfangen
			if ( (id_pos == -1) || (title_pos == -1) || (ssid_pos == -1) || (network_link == -1) || (default_link == -1)) {
				return null;
			}
			else
			{
				result.moveToFirst();
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("id", String.valueOf(result.getInt(id_pos)));
				map.put("title", result.getString(title_pos));
	            map.put("ssid", result.getString(ssid_pos));
	            map.put("network_link", result.getString(network_link));
	            map.put("default_link", result.getString(default_link));
				return map;
			}
		}
	}
	
	private ContentValues addToValues(String title, String ssid, String network_link, String default_link) {
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("ssid", ssid);
		values.put("network_link", network_link);
		values.put("default_link", default_link);
		return values;
	}

}
