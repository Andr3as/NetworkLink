package de.andrano.networklink;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

public class AreaActivity extends Activity {
	
	Resources resources;
	GeneralMethod general;
	//Identifier
	private int code_view;
	private int code_new;
	private int code_settings;
	private int code_about;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		resources = getResources();
		general = new GeneralMethod(this);
		
		if (resources.getBoolean(R.bool.has_two_panes)) {
			finish();
			return;
		}
				
		code_view = resources.getInteger(R.integer.code_view);
		code_new = resources.getInteger(R.integer.code_new);
		code_settings = resources.getInteger(R.integer.code_settings);
		code_about = resources.getInteger(R.integer.code_about);
		
		String fragment_key = resources.getString(R.string.settings_extra);
		int fragment_code = getIntent().getExtras().getInt(fragment_key, -1);
		
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (fragment_code == code_view) {
			transaction.replace(android.R.id.content, new DetailsFragment());
		} else if (fragment_code == code_new) {
			transaction.replace(android.R.id.content, new NewFragment());
		} else if (fragment_code == code_settings) {
			transaction.replace(android.R.id.content, new SettingsFragment());
		} else if (fragment_code == code_about) {
			transaction.replace(android.R.id.content, new SettingsFragment());
		} else {
			finish();
			return;
		}
		// Display the fragment as the main content.
		transaction.commit();
	}
	
	@Override
	protected void onResume() {
		overridePendingTransition(R.anim.move_right_to_middle, R.anim.move_middle_to_left);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		overridePendingTransition(R.anim.move_left_to_middle, R.anim.move_middle_to_right);
		super.onPause();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	general.onActivityResult(requestCode, resultCode, data);
    	super.onActivityResult(requestCode, resultCode, data);
    }
}
