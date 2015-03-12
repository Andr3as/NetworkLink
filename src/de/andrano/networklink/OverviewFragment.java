package de.andrano.networklink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class OverviewFragment extends Fragment{

	private Context context;
	private Activity activity;
	private GeneralMethod general;
	private Resources resources;
	private SqlHelper sql;
	
	private ListView lv;
	private TextView empty;
	private List<HashMap<String, String>> list;
	private HashMap<String, String> map;
	
	private boolean isActionMode = false;
	private ActionMode actionMode;
	private List<Integer> checkedItemsPositions = new ArrayList<Integer>();
	private List<View> checkedItemsViews = new ArrayList<View>();
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view 	= inflater.inflate(R.layout.overview_fragment, container, false);
		activity 	= getActivity();
		context 	= activity.getApplicationContext();
		general 	= new GeneralMethod((MainActivity) activity);
		resources 	= getResources();
		sql			= new SqlHelper(context);
		
		lv 			= (ListView) view.findViewById(android.R.id.list);
		lv.setOnItemClickListener(item_listener);
		lv.setOnItemLongClickListener(long_listener);
		lv.setOnScrollListener(scrollListener);
				
		empty 		= (TextView) view.findViewById(android.R.id.empty);
		updateLinks();
		
		return view;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater inflater = activity.getMenuInflater();
		inflater.inflate(R.menu.context, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public void onResume() {
		updateLinks();
		redoSelection();
		super.onResume();
	};
	
	private void updateLinks() {
		list 		= sql.getEntries();
		if (list != null) {
			String[] from = new String[] {"title", "ssid", "network_link"};
	        int[] to = new int[] { R.id.textView5, R.id.textView6, R.id.textView7};
	        
	        //SimpleAdapter adapter = new SimpleAdapter(context, list, R.layout.list_item, from, to);
	        LinkAdapter adapter = new LinkAdapter(context, list, R.layout.list_item, 
	        		from, to, link_listener, right_listener);
	        lv.setAdapter(adapter);
		} else {
			lv.setAdapter(new ArrayAdapter<String>(
					context, android.R.layout.simple_list_item_1, new ArrayList<String>()));
		}
		if (list != null) {
			//Hide no data hint
			empty.setVisibility(View.GONE);
		} else {
			empty.setVisibility(View.VISIBLE);
		}
	}
	
	private void handleSelection(View view, int position, long id) {
		if (!isActionMode) {
			startActionMode();
		}
		if (checkedItemsPositions.contains(position)) {
			//Deselect item
			view.setBackgroundColor(resources.getColor(android.R.color.transparent));
			checkedItemsPositions.remove((Object) position);
			checkedItemsViews.remove(view);
		} else {
			//Select item
			view.setBackgroundColor(resources.getColor(R.color.holo_blue_bright));
			checkedItemsPositions.add(position);
			checkedItemsViews.add(view);
		}
		if (checkedItemsPositions.isEmpty()) {
			actionMode.finish();
			return;
		}
		//Handle size
		actionMode.setTitle(String.valueOf(checkedItemsPositions.size()));
		//Handle second fragment
		if (general.isDualPane) {
			if (checkedItemsPositions.isEmpty()) {
				general.handleSecondFragment(general.code_view);
			} else {
				general.handleSecondFragment(general.code_count, resources.getString(R.string.key_count), 
						String.valueOf(checkedItemsPositions.size()));
			}
		}
	}
	
	private void redoSelection() {
		for (int i = 0; i < lv.getChildCount(); i++) {
			int pos = i + lv.getFirstVisiblePosition();
			if (checkedItemsPositions.contains(pos)) {
				lv.getChildAt(i).setBackgroundColor(resources.getColor(R.color.holo_blue_bright));
			}
		}
	}
	
	private void startActionMode() {
		if (!isActionMode) {
			isActionMode = true;
			actionMode = ((Activity) getActivity()).startActionMode(ActionModeCallback);
		}
	}
	
	private void stopActionMode() {
		if (isActionMode) {
			isActionMode = false;
			for (int i = 0; i < checkedItemsViews.size(); i++) {
				checkedItemsViews.get(i).setBackgroundColor(resources.getColor(android.R.color.transparent));
				//lv.getChildAt(checkedItemsPositions.get(i)).setBackgroundColor(resources.getColor(android.R.color.transparent));
			}
			checkedItemsPositions.clear();
			checkedItemsViews.clear();
		}
		if (general.isDualPane) {
			general.handleSecondFragment(general.code_view);
		}
		updateLinks();
	}
	
	private void deleteCheckedItems() {
		String id = "";
		for (int i = 0; i < checkedItemsPositions.size(); i++) {
			id = list.get(checkedItemsPositions.get(i)).get("id");
			sql.deleteEntry(id);
		}
		checkedItemsPositions.clear();
		isActionMode = false;
	}
	
	private void exportCheckedItems() {
		ArrayList<String> selectedIDs = new ArrayList<String>();
		String id = "";
		for (int i = 0; i < checkedItemsPositions.size(); i++) {
			id = list.get(checkedItemsPositions.get(i)).get("id");
			selectedIDs.add(id);
		}
		if (!general.exportEntries(selectedIDs)) {
			general.showToast(R.string.error);
		}
	}
	
	private void selectAllItems() {
		for (int i = 0; i < lv.getCount(); i++) {
			View view = lv.getChildAt(i);
			long id = lv.getItemIdAtPosition(i);
			int pos = i + lv.getFirstVisiblePosition();
			if (!checkedItemsPositions.contains(pos)) {
				try {
					handleSelection(view, pos, id);
				} catch(Exception e) {};
			}
		}
	}
	
	OnItemClickListener item_listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (isActionMode) {
				handleSelection(view, position, id);
			} else {
				map = list.get(position);
				general.openLink(map.get("ssid"), map.get("network_link"), map.get("default_link"));
			}
		}
	};
	
	OnClickListener link_listener = new OnClickListener() {	
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag(R.id.tag_position);
			View view = (View) v.getParent();
			long id = lv.getItemIdAtPosition(position);
			handleSelection(view, position, id);
		}
	};
	
	OnClickListener right_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag(R.id.tag_position);
			String id = list.get(position).get("id");
			general.handleSecondFragment(general.code_view, resources.getString(R.string.key_id), id);
		}
	};
	
	OnItemLongClickListener long_listener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			handleSelection(view, position, id);
			return true;
		}
	};
	
	OnScrollListener scrollListener = new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			redoSelection();
		}
	};
	
	ActionMode.Callback ActionModeCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			stopActionMode();
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflator = activity.getMenuInflater();
			inflator.inflate(R.menu.context, menu);
			return true;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.context_select_all:
					selectAllItems();
					return true;
				case R.id.context_export:
					exportCheckedItems();
					break;
				case R.id.context_delete:
					deleteCheckedItems();
					break;
			}
			actionMode.finish();
			return true;
		}
	};
}