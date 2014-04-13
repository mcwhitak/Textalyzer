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
import android.graphics.drawable.Drawable;
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
	private static HashMap<Integer, ContactHolder> contactMap;
	private ContactsAdapter contactAdapter;
	
	public static final int ONE_HOUR = 60 * 60 * 1000;
	
	
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
				if(body == null)
					continue;
				
				Log.d("ERICNELSON", body);
				holder.textReceivedLength += body.length(); 
				
				determineWordFrequency(body, Directions.INBOUND, holder);
				
				holder.incomingTextCount++;
				holder.addInstruction("Text Count:", "Incoming: " + holder.incomingTextCount, null);
				
				String address = cursor.getString(3);
				address = addressClipper(address);
				holder.phoneNumber = address;
				
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
				determineWordFrequency(body, Directions.INBOUND, holder);
				holder.textReceivedLength += body.length(); 
				holder.incomingTextCount++;
				holder.addInstruction("Text Count:", "Incoming: " + holder.incomingTextCount, null);
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
			if(address == null)
				continue;
			address = addressClipper(address);
			
			Iterator it = contactMap.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry pairs = (Map.Entry)it.next();
				ContactHolder holder = (ContactHolder)pairs.getValue();
				if(holder.phoneNumber.equals(address))
				{
					String body = cursor.getString(13);
					determineWordFrequency(body, Directions.OUTBOUND, holder);
					holder.textSentLength += body.length(); 
					holder.outgoingTextCount++;
					holder.addInstruction("Text Count:", null, "Outgoing: " + holder.outgoingTextCount);
					TextMessage message = new TextMessage(Directions.OUTBOUND, body, cursor.getInt(5));
					holder.textMessages.add(message);
					break;
				}
			}
		}while(cursor.moveToNext());
		cursor.close();
		
		Iterator it = contactMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry pairs = (Map.Entry)it.next();
			ContactHolder holder = (ContactHolder)pairs.getValue();
			holder.analyze();
		}
		
		Log.d("ERICNELSON", "SIZE: " + contactMap.size());
		
		grabAllViews();
		
		//create a runnable thread that starts before grab all views
		
		
		contactAdapter = new ContactsAdapter();
		contactListView.setAdapter(contactAdapter);
		contactListView.setOnItemClickListener(this);

	}
	
	public void determineWordFrequency (String body, Directions direction, ContactHolder holder)
	{
		String [] words = body.replaceAll("[!?,]", "").split("\\s+"); //remove punctuation and split by whitespace(s)
		if (direction == Directions.OUTBOUND)
		{
			for (String word: words)
			{
		        Integer frequency = holder.outgoingWordFrequency.get(word); //Must use wrapper to utilize null below
		        if (frequency == null)
		        {
		        	holder.outgoingWordFrequency.put(word,1);	
		        } 
		        else 
		        {
		        	holder.outgoingWordFrequency.put(word,frequency.intValue() + 1);
		        }
			}
		} 
		else if(direction == Directions.INBOUND)
		{
			for (String word: words)
			{
		        Integer frequency = holder.incomingWordFrequency.get(word); //Must use wrapper to utilize null below
		        if (frequency == null)
		        {
		        	holder.incomingWordFrequency.put(word,1);	
		        } 
		        else 
		        {
		        	holder.incomingWordFrequency.put(word,frequency.intValue() + 1);
		        }
			}
			
		}
	}
	
	private void grabAllViews()
	{
		contactListView = (ListView)findViewById(R.id.contacts_list);
		
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
			
			if(position < contactMap.size())
			{
				ContactHolder holder = (ContactHolder)this.getItem(position);
				nameText.setText(holder.personName);
			}
			return itemView;
		}
		
	}
	
	private String addressClipper(String address)
	{
		if(address.contains("+1"))
		{
			address = address.substring(2);
		}
		
		if(address.contains(" ") || address.contains("(") || address.contains(")"))
		{
			address = address.replace(" ", "").replace("(", "").replace(")", "").replace("-", "");
		}
		return address;
	}
	
	private Activity getCtx()
	{
		return this;
	}
	
	public static ContactHolder getContactHolder(int id)
	{
		return contactMap.get(id);
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
				intent.putExtra("id", contact.personId);
				startActivity(intent);
			}
		}
	}
}
