package com.whitaker.textalyzer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.whitaker.textalyzer.TextMessage.Directions;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;

public class MainActivity extends Activity implements OnItemClickListener
{
	private TextView titleText;
	private ListView contactListView;
	private ArrayList<ContactHolder> personList;
	private HashMap<Integer, ContactHolder> contactMap;
	private ContactsAdapter contactAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		personList = new ArrayList<ContactHolder>();
		contactMap = new HashMap<Integer, ContactHolder>();
		
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		cursor.moveToFirst();
		do
		{
			//Each message is a separate while loop call
			Integer personCode = cursor.getInt(4);
			
			if(contactMap.get(personCode) == null && personCode != 0)
			{
				ContactHolder holder = new ContactHolder();
				holder.personId = personCode;
				
				String body = cursor.getString(13);
				holder.textReceived += body.length();
				
				holder.phoneNumber = cursor.getString(3);
				
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
				contactMap.put(personCode, holder);
				conCursor.close();
				
				TextMessage message = new TextMessage(Directions.INBOUND, body, cursor.getInt(5));
				holder.textMessages.add(message);
			}
			else if(personCode != 0)
			{
				ContactHolder holder = contactMap.get(personCode);
				String body = cursor.getString(13);
				holder.textReceived += body.length();
				TextMessage message = new TextMessage(Directions.INBOUND, body, cursor.getInt(5));
				holder.textMessages.add(message);
			}
			
		}while(cursor.moveToNext());
		cursor.close();
		
		cursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);
		cursor.moveToFirst();
		do
		{
			String address = cursor.getString(3);
			
			Iterator it = contactMap.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry pairs = (Map.Entry)it.next();
				ContactHolder holder = (ContactHolder)pairs.getValue();
				if(holder.phoneNumber.equals(address))
				{
					String body = cursor.getString(13);
					holder.textSent += body.length();
					TextMessage message = new TextMessage(Directions.OUTBOUND, body, cursor.getInt(5));
					holder.textMessages.add(message);
					break;
				}
			}
		}while(cursor.moveToNext());
		cursor.close();
		
		/*
		cursor = getContentResolver().query(Uri.parse("content://sms/out"), null, null, null, null);
		while(cursor.moveToNext())
		{
			String address = cursor.getString(3);
			Iterator it = contactMap.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry pairs = (Map.Entry)it.next();
				ContactHolder holder = (ContactHolder)pairs.getValue();
				if(holder.phoneNumber.equals(address))
				{
					String body = cursor.getString(13);
					holder.textSent += body.length();
					TextMessage message = new TextMessage(Directions.OUTBOUND, body, cursor.getInt(5));
					holder.textMessages.add(message);
					break;				}
			}
		}
		cursor.close();
		*/
		
		grabAllViews();
		titleText.setText("FINISHED BITCH DICK PUSSY CUNT");	
		
		contactAdapter = new ContactsAdapter();
		contactListView.setAdapter(contactAdapter);
		contactListView.setOnItemClickListener(this);

	}
	
	private void grabAllViews()
	{
		titleText = (TextView)findViewById(R.id.title_text);
		contactListView = (ListView)findViewById(R.id.contacts_list);
	}
	
	public class ContactHolder
	{
		public int personId;
		public int textReceived;
		public int textSent;
		public String personName;
		public String phoneNumber;
		public ArrayList<TextMessage> textMessages;
		
		public ContactHolder()
		{
			textMessages = new ArrayList<TextMessage>();
			textReceived = 0;
			textSent = 0;
		}
	}
	
	private class ContactsAdapter extends BaseAdapter
	{
		@Override
		public int getCount() 
		{
			return contactMap.size();
		}

		@Override
		public Object getItem(int position) 
		{
			Iterator it = contactMap.entrySet().iterator();
			int i=0;
			while(it.hasNext())
			{
				Map.Entry pairs = (Map.Entry)it.next();
				if(i == position)
				{
					return pairs.getValue();
				}
				i++;
			}
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			Iterator it = contactMap.entrySet().iterator();
			int i=0;
			while(it.hasNext())
			{
				Map.Entry pairs = (Map.Entry)it.next();
				if(i == position)
				{
					ContactHolder holder = (ContactHolder)pairs.getValue();
					return holder.personId;
				}
				i++;
			}
			return -1;
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
			
			if(position < contactMap.size())
			{
				ContactHolder holder = (ContactHolder)this.getItem(position);
				nameText.setText(holder.personName);
				subText.setText(holder.textReceived+" : " + holder.textSent);
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
			if(position < contactMap.size())
			{				
				ContactHolder contact = (ContactHolder)contactListView.getAdapter().getItem(position);
				Intent intent = new Intent(getCtx(), DetailActivity.class);
				intent.putExtra("name", contact.personName);
				startActivity(intent);
			}
		}
	}
	
}
