package com.whitaker.textalyzer;

import java.io.IOException;
import java.util.ArrayList; //TODO can we delete this unused imports?
import java.util.Arrays;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.whitaker.textalyzer.TextMessage.Directions;
import com.whitaker.textalyzer.util.BounceListView;
import com.whitaker.textalyzer.util.TextalyzerApplication;
import com.whitaker_iacob.textalyzer.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;


public class MainActivity extends Activity implements OnItemClickListener, OnClickListener
{
	private BounceListView contactListView;
	private RelativeLayout generalLayout;
	private ContactsAdapter contactAdapter;
	
	
	public static final int ONE_HOUR = 60 * 60 * 1000;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		TextView abTV = (TextView)findViewById(titleId);
		abTV.setTextColor(Color.WHITE);
		
		TextalyzerApplication app = (TextalyzerApplication)this.getApplication();
		if(!app.isReady())
		{
			app.initMap();
			app.populateMap();
		}
		
		grabAllViews();
		
		contactAdapter = new ContactsAdapter();
		contactListView.setAdapter(contactAdapter);
		contactListView.setOnItemClickListener(this);
		generalLayout.setOnClickListener(this);
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.actionbar_layout, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_about:
				Intent intent = new Intent(this, AboutActivity.class);
				startActivity(intent);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void grabAllViews()
	{
		contactListView = (BounceListView)findViewById(R.id.contacts_list);
		generalLayout = (RelativeLayout)findViewById(R.id.general_relative);
	}
	

	
	private class ContactsAdapter extends BaseAdapter
	{
		private ArrayList<ContactHolder> contactList;
		private TextalyzerApplication app = (TextalyzerApplication) getCtx().getApplication();
		
		@SuppressWarnings("unchecked")
		public ContactsAdapter()
		{
			contactList = new ArrayList<ContactHolder>();
			for (String contactString: app.getKeySet())
			{
					contactList.add(app.getContact(contactString));
			}
			Collections.sort(contactList, new ContactHolder.ContactComparator());
		}
		
		@Override
		public int getCount() 
		{
			return contactList.size();
		}

		@Override
		public Object getItem(int position) 
		{
			return contactList.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View itemView = convertView;
			if(convertView == null)
			{
				LayoutInflater li = getCtx().getLayoutInflater();
				itemView = li.inflate(R.layout.contacts_item, null);
			}
			
			TextView nameText = (TextView)itemView.findViewById(R.id.contact_item_name);
			TextView countText = (TextView)itemView.findViewById(R.id.contact_item_total);
			
			if(position < contactList.size())
			{
				ContactHolder holder = (ContactHolder)this.getItem(position);
				nameText.setText(holder.personName);
				countText.setText((holder.outgoingTextCount + holder.incomingTextCount) + " Texts");
			}
			return itemView;
		}
	}
	

	
	private Activity getCtx()
	{
		return this;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		if(parent == contactListView)
		{
			if(position < contactListView.getAdapter().getCount())
			{				
				ContactHolder contact = (ContactHolder)contactListView.getAdapter().getItem(position);
				Intent intent = new Intent(getCtx(), DetailActivity.class);
				intent.putExtra("address", contact.phoneNumber);
				startActivity(intent);
			}
		}
	}
	
	@Override
	public void onClick(View view) 
	{
		if(view == generalLayout)
		{
			Intent intent = new Intent(getCtx(), GeneralActivity.class);
			startActivity(intent);
		}
	}
}
