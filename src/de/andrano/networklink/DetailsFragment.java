package de.andrano.networklink;

import java.util.HashMap;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsFragment extends Fragment{
	
	GeneralMethod general;
	Resources resources;
	Context context;
	
	LayoutInflater inflater;
	ViewGroup container;
	
	HashMap<String, String> link;
	boolean isEmpty = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater 	= inflater;
		this.container 	= container;
		resources 	= getResources();
		context 	= getActivity().getApplicationContext();
		general 	= new GeneralMethod(getActivity());
		String id	= null;
		if (general.isDualPane) {
			Bundle arguments = getArguments();
			if (arguments != null) {
				id = arguments.getString(resources.getString(R.string.key_id));
			}
		} else {
			id = getActivity().getIntent().getExtras().getString(resources.getString(R.string.key_id));
		}
		//Get Layout
		View layout;
		if (id != null) { 
			layout = getDetails(Integer.parseInt(id));
		} else {
			layout = getFirstLink();
		}
		//ActionBar
		setHasOptionsMenu(true);
		return layout;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.details, menu);
		if (!general.isDualPane) {
			menu.removeItem(R.id.menu_settings);
		} else {
			//DualPane
			if (isEmpty) {
				menu.removeItem(R.id.menu_open);
				menu.removeItem(R.id.menu_edit);
				menu.removeItem(R.id.menu_delete_link);
			}
		}
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_open:
				general.openLink(link.get("ssid"), link.get("network_link"), link.get("default_link"));
				return true;
			case R.id.menu_edit:
				general.handleSecondFragment(general.code_new, resources.getString(R.string.key_id), link.get("id"));
				return true;
			case R.id.menu_share:
				
				return true;
			case R.id.menu_delete_link:
				deleteLink();
				return true;
			case R.id.menu_new_link:
				general.handleSecondFragment(general.code_new);
				return true;
			case R.id.menu_settings:
				//general.handleSecondFragment(general.code_settings);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private View setDetails(HashMap<String, String> map) {
		View layout = inflater.inflate(R.layout.details_fragment, container, false);
		//Get views
		TextView title = (TextView) layout.findViewById(R.id.textView1);
		TextView network = (TextView) layout.findViewById(R.id.textView3);
		TextView network_link = (TextView) layout.findViewById(R.id.textView5);
		TextView default_link = (TextView) layout.findViewById(R.id.textView7);
		//Set texts
		title.setText(map.get("title"));
		network.setText(map.get("ssid"));
		network_link.setText(map.get("network_link"));
		default_link.setText(map.get("default_link"));
		return layout;
	}
	
	private View getDetails(int id) {
		SqlHelper sql = new SqlHelper(context);
		link = sql.getEntry(id);
		if (link == null) {
			return getFirstLink();
		} else {
			return setDetails(link);
		}
	}
	
	private View getFirstLink() {
		SqlHelper sql = new SqlHelper(context);
		link = sql.getFirstEntry();
		if (link == null) {
			return getEmptyDetails();
		} else {
			return setDetails(link);
		}
	}
	
	private View getEmptyDetails() {
		isEmpty = true;
		return inflater.inflate(R.layout.empty_details_fragment, container, false);
	}
	
	private void deleteLink() {
		SqlHelper sql = new SqlHelper(context);
		sql.deleteEntry(link.get("id"));
		general.closeSecondFragment();
		general.updateFirstFragment();
	}
	
	
}