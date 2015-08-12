package de.andrano.networklink;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity{

	boolean isDualPane;
	
	GeneralMethod general;
	//Android resources
	Context context;
	Resources resource;
	//Categories
	View category;
	View area;
	
	private String fragment_key;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = getApplicationContext();
        resource = getResources();
        general = new GeneralMethod(this);
        
        fragment_key = resource.getString(R.string.settings_extra);
        isDualPane = resource.getBoolean(R.bool.has_two_panes);
        if (isDualPane) {
        	general.updateFirstFragment();
        	general.handleSecondFragment(general.code_view);
        }
                
        //Get intent
        Intent intent = getIntent();
        if (intent.hasExtra(fragment_key)) {
        	int extra = intent.getIntExtra(fragment_key, -1);
        	general.handleSecondFragment(extra);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	if (!isDualPane) {
            inflater.inflate(R.menu.main, menu);
    	}
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_new_link:
	        	general.handleSecondFragment(general.code_new);
	        	return true;
	        case R.id.menu_settings:
	        	general.handleSecondFragment(general.code_settings);
	        	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	general.onActivityResult(requestCode, resultCode, data);
    	super.onActivityResult(requestCode, resultCode, data);
    }
}
