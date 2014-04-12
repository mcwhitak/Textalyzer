package com.whitaker.textalyzer;

import com.whitaker.textalyzer.TextMessage.Directions;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DetailActivity extends Activity
{
	private TextView scoreHeaderTextView;
	private TextView scoreValueTextView;
	private TextView contactNameTextView;
	private ListView informationListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.detail_activity);
		
		Bundle b = getIntent().getExtras();
		Integer id = b.getInt("id");
		grabAllViews();
		
		ContactHolder holder = MainActivity.getContactHolder(id);
		
		Integer outgoing = 0;
		Integer incoming = 0;
		for(int i=0; i<holder.textMessages.size(); i++)
		{
			if(holder.textMessages.get(i).direction == Directions.INBOUND)
			{
				incoming++;
			}
			else
			{
				outgoing++;
			}
		}
		contactNameTextView.setText(holder.personName);
	}
	
	private void grabAllViews()
	{
		scoreHeaderTextView = (TextView)findViewById(R.id.score_header);
		scoreValueTextView = (TextView)findViewById(R.id.score_value);
		contactNameTextView = (TextView)findViewById(R.id.contact_name);
		informationListView = (ListView)findViewById(R.id.list_information);
	}
	
	private class InformationAdapter extends BaseAdapter
	{

		@Override
		public int getCount() 
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) 
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}