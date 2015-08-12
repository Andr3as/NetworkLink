package de.andrano.networklink;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class LinkAdapter extends SimpleAdapter{

	private Context context;
	private List<? extends Map<String, ?>> list;
	private int resource;
	private String[] from;
	private int[] to;
	private OnClickListener link_listener;
	private OnClickListener right_listener;
	
	public LinkAdapter(Context context, List<? extends Map<String, ?>> data, int resource, 
			String[] from, int[] to, OnClickListener link_listener, OnClickListener right_listener) {
		super(context, data, resource, from, to);
		this.context 	= context;
		this.list 		= data;
		this.resource 	= resource;
		this.from 		= from;
		this.to 		= to;
		this.link_listener 	= link_listener;
		this.right_listener = right_listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView 			= inflater.inflate(resource, parent, false);
		
		Map<String, ?> map = list.get(position);
		
		if (from.length != to.length) {
			return rowView;
		}

		ViewHolder holder = new ViewHolder();
		
		for (int i = 0; i < from.length; i++) {
			TextView text = (TextView) rowView.findViewById(to[i]);
			text.setText(map.get(from[i]).toString());
		}
		
		holder.link_view 	= (ImageView) rowView.findViewById(R.id.imageView2);
		holder.right_view 	= (ImageView) rowView.findViewById(R.id.imageView3);
		holder.position 	= position;
		rowView.setTag(holder);
		
		setLinkClickListeners(holder.link_view);
		setRightClickListeners(holder.right_view);
		
		setTagsToViews(holder.link_view, position); 
		setTagsToViews(holder.right_view, position);

		return rowView;
	}
	
	private void setTagsToViews(View view, Object tag) {
		view.setTag(R.id.tag_position, tag);
	}
	
	private void setLinkClickListeners(View view) { 
		view.setOnClickListener(link_listener); 
	}
	
	private void setRightClickListeners(View view) { 
		view.setOnClickListener(right_listener); 
	}
}


/*ImageView link_view	= (ImageView) rowView.findViewById(R.id.imageView2);
//link_view.setOnClickListener(icon_link_click);
TextView title_view = (TextView) rowView.findViewById(R.id.textView5);
title_view.setText(map.get("title").toString());
TextView network_view = (TextView) rowView.findViewById(R.id.textView6);
network_view.setText(map.get("ssid").toString());
TextView net_link_view = (TextView) rowView.findViewById(R.id.textView7);
net_link_view.setText(map.get("network_link").toString());
ImageView right_view = (ImageView) rowView.findViewById(R.id.imageView3);
//right_view.setOnClickListener(icon_right_click);
*/