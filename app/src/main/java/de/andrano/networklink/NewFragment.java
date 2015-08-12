package de.andrano.networklink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class NewFragment extends Fragment{

	private GeneralMethod general;
	private Context context;
	private Resources resources;
	
	private List<WifiConfiguration> networks;
	private int network = -1;
	
	private boolean editEntry = false;
	private HashMap<String, String> map;
	
	/* Views */
	private Spinner spinner;
	private EditText edit_title;
	private EditText edit_net_link;
	private EditText edit_default_link;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.new_fragment, container, false);
		context 	= getActivity().getApplicationContext();
		general 	= new GeneralMethod(getActivity());
		resources	= getActivity().getResources();
		//ActionBar
		setHasOptionsMenu(true);
		//Get views
		spinner = (Spinner) layout.findViewById(R.id.spinner1);
		edit_title	= (EditText) layout.findViewById(R.id.editText1);
		edit_net_link = (EditText) layout.findViewById(R.id.editText2);
		edit_default_link = (EditText) layout.findViewById(R.id.editText3);
		
		//Add networks
		networks = general.getNetworks();
		if (networks == null) {
			new MyDialogFragment().show(getActivity().getFragmentManager(), "dialog");
			return layout;
		}
		List<String> ssids = new ArrayList<String>();
		for (int i = 0; i < networks.size(); i++) {
			ssids.add(networks.get(i).SSID);
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,R.layout.spinner_item, ssids);
		dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
		spinner.setAdapter(dataAdapter);
		/* Edit entry? */
		String id	= null;
		if (general.isDualPane) {
			Bundle arguments = getArguments();
			if (arguments != null) {
				id = arguments.getString(resources.getString(R.string.key_id));
			}
		} else {
			id = getActivity().getIntent().getExtras().getString(resources.getString(R.string.key_id));
		}
		if ( (id != null) && (networks != null) ) {
			editEntry = true;
			SqlHelper sql = new SqlHelper(context);
			map = sql.getEntry(Integer.valueOf(id));
			edit_title.setText(map.get("title"));
			String ssid = map.get("ssid");
			for (int i = 0; i < networks.size(); i++) {
				if (networks.get(i).SSID == ssid) {
					spinner.setSelection(i);
					network = i;
					break;
				}
			}
			edit_net_link.setText(map.get("network_link"));
			edit_default_link.setText(map.get("default_link"));
		}
		/* Responding to user action */
		network = 0;
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				network = position;
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		return layout;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.new_link, menu);
		if (!general.isDualPane) {
			menu.removeItem(R.id.menu_settings);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save:
				newEntry();
				return false;
			case R.id.menu_discard:
				general.closeSecondFragment();
				return false;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void newEntry() {
		String title = edit_title.getText().toString();
		if (network == -1) {
			general.showToast(R.string.error_failed_new_entry);
			general.closeSecondFragment();
			return;
		}		
		String SSID	= networks.get(network).SSID;
		String network_link = edit_net_link.getText().toString();
		String default_link = edit_default_link.getText().toString();
		if (network_link.length() == 0 || default_link.length() == 0) {
			general.showToast(R.string.error_failed_new_entry);
			general.closeSecondFragment();
			return;
		}
		//Edit values
		if (!network_link.startsWith("http://") && !network_link.startsWith("https://")) {
			network_link = "http://" + network_link;
		}
		if (!default_link.startsWith("http://") && !default_link.startsWith("https://")) {
			default_link = "http://" + default_link;
		}
		//Insert entry		
		SqlHelper sql = new SqlHelper(context);
		Boolean result = false;
		if (editEntry) {
			String mTitle = map.get("title");
			String mSSID = map.get("ssid");
			String mNLink = map.get("network_link");
			String mDLink = map.get("default_link");
			if ( (mTitle.equals(title)) && (mSSID.equals(SSID)) 
					&& (mNLink.equals(network_link)) && (mDLink.equals(default_link)) ) {
				general.showToast(R.string.error_nothing_changed);
				general.closeSecondFragment();
				return;
			}
			result = sql.updateEntry(map.get("id"), title, SSID, network_link, default_link);
		} else {
			result = sql.createEntry(title, SSID, network_link, default_link);
		}
		if (result) {
			general.showToast(R.string.saved);
		} else {
			general.showToast(R.string.error_failed_new_entry);
		}
		if (general.isDualPane) {
			general.updateFirstFragment();
		}
		general.closeSecondFragment();
	}
	
	public static class MyDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
            	.setMessage(R.string.new_activate_msg)
            	.setPositiveButton(R.string.new_activate_btn, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
				    	startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
				    	dismiss();
				    }
				})
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				})
                .create();
        }
    }
	
}