package de.andrano.networklink;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CountFragment extends Fragment {

	private GeneralMethod general;
	private Context context;
	private Resources resources;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.count_fragment, container, false);
		context 	= getActivity().getApplicationContext();
		general 	= new GeneralMethod(getActivity());
		resources	= getActivity().getResources();
		
		if (!general.isDualPane) {
			getActivity().finish();
		}
		
		Bundle arguments = getArguments();
		if (arguments != null) {
			String count = arguments.getString(resources.getString(R.string.key_count));
			TextView count_view = (TextView) layout.findViewById(R.id.count_text);
			count_view.setText(count);
		} else {
			general.closeSecondFragment();
		}
		
		return layout;
	}	
}