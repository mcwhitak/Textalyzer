package com.whitaker.textalyzer;


import java.util.ArrayList;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;

public class MainActivity extends Activity 
{
	private TextView titleText;
	private ListView contactListView;
	private ArrayList<ContactHolder> personList;
	private ArrayList<Integer> personIdList;
	private ContactsAdapter contactAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		personList = new ArrayList<ContactHolder>();
		personIdList = new ArrayList<Integer>();
		
		
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		cursor.moveToFirst();
		do
		{
			for(int i=0; i<cursor.getColumnCount(); i++)
			{
				if(i == 4)
				{
					Integer personCode = cursor.getInt(i);
					
					if(!personIdList.contains(personCode) && personCode!=0)
					{
						personIdList.add(personCode);
						ContactHolder holder = new ContactHolder();
						holder.personId = personCode;
						
						ContentResolver content = this.getContentResolver();
						String[] projection = {Data.MIMETYPE,
								ContactsContract.Contacts._ID,
								ContactsContract.Contacts.DISPLAY_NAME,
								ContactsContract.CommonDataKinds.Phone.NUMBER,
								ContactsContract.CommonDataKinds.Email.ADDRESS
						};
						
						String selection = ContactsContract.Data.RAW_CONTACT_ID + "=?";
						String sortOrder = Data.LOOKUP_KEY;
						String[] args = {personCode+""};
						Cursor conCursor = content.query(Data.CONTENT_URI, projection, selection, args, sortOrder);
						conCursor.moveToFirst();
						String displayName = conCursor.getString(2);
						holder.personName = displayName;
						personList.add(holder);
						conCursor.close();
					}
				}
				//Log.d("CATEGORY", cursor.getColumnName(i) + " : " + i);
			}
			
		}while(cursor.moveToNext());
		
		grabAllViews();
		titleText.setText("FINISHED BITCH DICK PUSSY CUNT");	
		
		contactAdapter = new ContactsAdapter();
		contactListView.setAdapter(contactAdapter);

	}
	
	private void grabAllViews()
	{
		titleText = (TextView)findViewById(R.id.title_text);
		contactListView = (ListView)findViewById(R.id.contacts_list);
	}
	
	private class ContactHolder 
	{
		public int personId;
		public String personName;
	}
	
	private class ContactsAdapter extends BaseAdapter
	{
		@Override
		public int getCount() 
		{
			return personList.size();
		}

		@Override
		public Object getItem(int position) 
		{
			return personList.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return personList.get(position).personId;
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
			TextView subText = (TextView)itemView.findViewById(R.id.contact_item_subtitle);
			
			if(position < personList.size())
			{
				nameText.setText(personList.get(position).personName);
				subText.setText(personList.get(position).personId + "");//
			}
			return itemView;
		}
		
	}
	
	private Activity getCtx()
	{
		return this;
	}
	
}
