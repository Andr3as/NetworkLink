package de.andrano.networklink;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment{

	Activity activity;
	Resources resources;
	GeneralMethod general;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity 	= getActivity();
		resources 	= activity.getResources();
		general		= new GeneralMethod(activity);
		
		findPreference(resources.getString(R.string.key_import)).setOnPreferenceClickListener(importListener);
		return inflater.inflate(R.layout.settings_fragment, container, false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_about);
	}
	
	OnPreferenceClickListener importListener = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			general.importEntries();
			return false;
		}
	};
}
