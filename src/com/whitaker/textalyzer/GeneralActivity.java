package com.whitaker.textalyzer;

import com.whitaker.textalyzer.util.BounceListView;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GeneralActivity extends Activity
{
	private BounceListView itemListView;
	
	private String[] categories = {"Total Texts", "Favorite Contact",
			"Least Favorite Contact"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.general_stats);
		
		grabAllViews();
		itemListView.setAdapter(new ItemAdapter());
	}
	
	private void grabAllViews()
	{
		itemListView = (BounceListView)findViewById(R.id.list_general);
	}
	
	private class ItemAdapter extends BaseAdapter
	{
		@Override
		public int getCount() 
		{
			return categories.length;
		}

		@Override
		public Object getItem(int pos) 
		{
			return categories[pos];
		}

		@Override
		public long getItemId(int pos) 
		{
			return pos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View itemView = convertView;
			if(convertView == null)
			{
				LayoutInflater li = getCtx().getLayoutInflater();
				itemView = li.inflate(R.layout.general_item, null);
			}
			
			TextView catText = (TextView)itemView.findViewById(R.id.general_item_name);
			TextView resultText = (TextView)itemView.findViewById(R.id.general_item_result);
			
			if(position < categories.length)
			{
				catText.setText(categories[position]);
				resultText.setText("TEST");
			}
			return itemView;
		}
		
	}	
	
	private Activity getCtx()
	{
		return this;
	}
}