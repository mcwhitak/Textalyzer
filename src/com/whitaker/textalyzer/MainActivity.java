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
	public static HashMap<String, ContactHolder> contactMap;
	private HashMap<String, String> nameMap;
	private ContactsAdapter contactAdapter;
	
	private static ArrayList<Date> timesReceived = new ArrayList<Date> ();
	private static ArrayList<Date> timesSent = new ArrayList<Date> ();
	
	public static final int ONE_HOUR = 60 * 60 * 1000;
	private final String [] boringWords = {"the","be","and","of","a","in","to","have","it","it's","i","i'm","im","ok","for","you","he","with","on","do","say",
			"this","they","at","but","we","his","from","that","not","n't","by","she","or","what","was","go","their","can","who","get","is",
			"if","would","her","all","my","make","about","know","will","as","up","one","there","year","so","think","when","which","them","that's","did",
			"some","me","people","take","out","into","just","see","him","your","come","could","now","than","like","other","how","then","its",
			"our","two","these","want","way","look","first","also","new","because","day","more","use","no","find","here","thing","give",
			"many","are","a","e","o","u","b","c","d"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		TextView abTV = (TextView)findViewById(titleId);
		abTV.setTextColor(Color.WHITE);
		
		contactMap = new HashMap<String, ContactHolder>();
		nameMap = new HashMap<String, String>();
		
		Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		while (phones.moveToNext())
		{
			String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			phoneNumber = addressClipper(phoneNumber);
			String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			nameMap.put(phoneNumber, name);
		}
		phones.close();
		
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		cursor.moveToFirst();
		do
		{
			if(cursor.getCount() == 0)
				continue;
			
			//Each message is a separate while loop call
			String address = cursor.getString(cursor.getColumnIndex("address"));
			if(address == null)
				continue;
			
			address = addressClipper(address);
			
			if(contactMap.get(address) == null)
			{
				String name = nameMap.get(address);
				if(name == null)
					continue;
				
				ContactHolder holder = new ContactHolder();
				holder.personName = name;
				holder.phoneNumber = address;

				String body = cursor.getString(cursor.getColumnIndex("body")).toLowerCase();
				
				if(body == null)
					continue;

				holder.textReceivedLength += body.length(); 
				
				determineWordFrequency(body, Directions.INBOUND, holder);
				
				holder.incomingTextCount++;
				holder.addInstruction(this.getString(R.string.info_pre_count), getString(R.string.info_pre_in) + holder.incomingTextCount, null);
				
				contactMap.put(address, holder);
				long date = cursor.getLong(cursor.getColumnIndex("date"));
				TextMessage message = new TextMessage(Directions.INBOUND, body, date);
				holder.textMessages.add(message);
				timesReceived.add(new Date(date));
			}
			else
			{
				ContactHolder holder = contactMap.get(address);
				String body = cursor.getString(cursor.getColumnIndex("body")).toLowerCase();
				
				determineWordFrequency(body, Directions.INBOUND, holder);
				holder.textReceivedLength += body.length(); 
				holder.incomingTextCount++;
				holder.addInstruction(getString(R.string.info_pre_count), getString(R.string.info_pre_in) + holder.incomingTextCount, null);
				long date = cursor.getLong(cursor.getColumnIndex("date"));
				TextMessage message = new TextMessage(Directions.INBOUND, body, date);
				holder.textMessages.add(message);
				timesReceived.add(new Date(date));
			}
			
		}while(cursor.moveToNext());
		cursor.close();
		
		cursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);
		cursor.moveToFirst();
		do
		{
			if(cursor.getCount() == 0)
				continue;
			
			String address = cursor.getString(cursor.getColumnIndex("address"));
			
			if(address == null)
				continue;
			address = addressClipper(address);

			ContactHolder holder = contactMap.get(address);
			if(holder != null)
			{
				String body = cursor.getString(cursor.getColumnIndex("body")).toLowerCase();
				determineWordFrequency(body, Directions.OUTBOUND, holder);
				holder.textSentLength += body.length(); 
				holder.outgoingTextCount++;
				holder.addInstruction(getString(R.string.info_pre_count), null, getString(R.string.info_pre_out) + holder.outgoingTextCount);
				long date = cursor.getLong(cursor.getColumnIndex("date"));
				TextMessage message = new TextMessage(Directions.OUTBOUND, body, date);
				holder.textMessages.add(message);
				timesSent.add(new Date(date));
			}
			
		} while(cursor.moveToNext());
		cursor.close();
		
		for (String contactString: contactMap.keySet())
		{
			contactMap.get(contactString).analyze(getCtx());
		}
		
		grabAllViews();
		
		contactAdapter = new ContactsAdapter(contactMap);
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
	
	public void determineWordFrequency (String body, Directions direction, ContactHolder holder)
	{
		String [] words = body.replaceAll("[!?,]", "").split("\\s+"); //remove punctuation and split by whitespace(s)
		if (direction == Directions.OUTBOUND)
		{
			for (String word: words)
			{
				if (!Arrays.asList(boringWords).contains(word))
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
		} 
		else if(direction == Directions.INBOUND)
		{
			for (String word: words)
			{
				if (!Arrays.asList(boringWords).contains(word))
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
	}
	
	private class ContactsAdapter extends BaseAdapter
	{
		private ArrayList<ContactHolder> contactList;
		
		@SuppressWarnings("unchecked")
		public ContactsAdapter(HashMap<String, ContactHolder> map)
		{
			contactList = new ArrayList<ContactHolder>();
			for (String contactString: contactMap.keySet())
			{
					contactList.add(contactMap.get(contactString));
			}
			Collections.sort(contactList, new ContactHolder.ContactComparator());
		}
		
		@Override
		public int getCount() 
		{
			return contactMap.size();
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
			
			if(position < contactMap.size())
			{
				ContactHolder holder = (ContactHolder)this.getItem(position);
				nameText.setText(holder.personName);
				countText.setText((holder.outgoingTextCount + holder.incomingTextCount) + " Texts");
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
		address = address.replace(" ", "").replace("(", "").replace(")", "").replace("-", "");
		return address;
	}
	
	private Activity getCtx()
	{
		return this;
	}
	
	public static ContactHolder getContactHolder(String address)
	{
		return contactMap.get(address);
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
