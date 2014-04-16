package com.whitaker.textalyzer;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
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
	private static HashMap<String, ContactHolder> contactMap;
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
		
		personList = new ArrayList<ContactHolder>();
		contactMap = new HashMap<String, ContactHolder>();
		
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		cursor.moveToFirst();
		do
		{
			if(cursor.getCount() == 0)
				continue;
			
			//Each message is a separate while loop call
			String address = cursor.getString(3);
			if(address == null)
				continue;
			address = addressClipper(address);
			
			if(contactMap.get(address) == null)
			{
				ContactHolder holder = new ContactHolder();
				holder.phoneNumber = address;

				String body = cursor.getString(13);
				if(body == null)
					continue;

				holder.textReceivedLength += body.length(); 
				
				determineWordFrequency(body, Directions.INBOUND, holder);
				
				holder.incomingTextCount++;
				holder.addInstruction(this.getString(R.string.info_pre_count), getString(R.string.info_pre_in) + holder.incomingTextCount, null);
				
				Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
				while (phones.moveToNext())
				{
				  String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				  phoneNumber = addressClipper(phoneNumber);
				  if(phoneNumber.equals(address))
				  {
					  String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
					  holder.personName = name;
					  contactMap.put(address, holder);
					  TextMessage message = new TextMessage(Directions.INBOUND, body, cursor.getInt(5));
					  holder.textMessages.add(message);
					  break;
				  }
				}
				phones.close();
			}
			else
			{
				ContactHolder holder = contactMap.get(address);
				String body = cursor.getString(13);
				determineWordFrequency(body, Directions.INBOUND, holder);
				holder.textReceivedLength += body.length(); 
				holder.incomingTextCount++;
				holder.addInstruction(getString(R.string.info_pre_count), getString(R.string.info_pre_in) + holder.incomingTextCount, null);
				TextMessage message = new TextMessage(Directions.INBOUND, body, cursor.getInt(5));
				holder.textMessages.add(message);
			}
			
		}while(cursor.moveToNext());
		cursor.close();
		
		cursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);
		cursor.moveToFirst();
		do
		{
			if(cursor.getCount() == 0)
				continue;
			
			String address = cursor.getString(3);
			if(address == null)
				continue;
			address = addressClipper(address);

			ContactHolder holder = contactMap.get(address);
			if(holder != null)
			{
				String body = cursor.getString(13);
				determineWordFrequency(body, Directions.OUTBOUND, holder);
				holder.textSentLength += body.length(); 
				holder.outgoingTextCount++;
				holder.addInstruction(getString(R.string.info_pre_count), null, getString(R.string.info_pre_out) + holder.outgoingTextCount);
				TextMessage message = new TextMessage(Directions.OUTBOUND, body, cursor.getInt(5));
				holder.textMessages.add(message);
			}
			
		}while(cursor.moveToNext());
		cursor.close();
		
		Iterator it = contactMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry pairs = (Map.Entry)it.next();
			ContactHolder holder = (ContactHolder)pairs.getValue();
			holder.analyze(getCtx());
		}
				
		grabAllViews();
		
		contactAdapter = new ContactsAdapter();
		contactListView.setAdapter(contactAdapter);
		contactListView.setOnItemClickListener(this);//
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
	
	public String [] boringWords = {"the","be","and","of","a","in","to","have","to","it","I","I'm","im","I'll","Ok","that","for","you","he","with","on","do","say",
			"this","they","at","but","we","his","from","that","not","n't","n't","by","she","or","as","what","go","their","can","who","get","is","IS","Is",
			"if","would","her","all","my","make","about","know","will","as","up","one","there","year","so","think","when","which","them","did","Did","DID",
			"some","me","people","take","out","into","just","see","him","your","come","could","now","than","like","other","how","then","its",
			"our","two","more","these","want","way","look","first","also","new","because","day","more","use","no","find","here","thing","give",
			"many","The","are","ARE","Be","And","Of","A","In","To","Have","To","It","I","That","For","You","He","With","On","Do","Say","This","They","At",
			"But","We","His","From","That","Not","N't","N't","By","She","Or","As","What","Go","Their","Can","Who","Get","If","Would","Her",
			"All","My","Make","About","Know","Will","As","Up","One","There","Year","So","Think","When","Which","Them","Some","Me","People",
			"Take","Out","Into","Just","See","Him","Your","Come","Could","Now","Than","Like","Other","How","Then","Its","Our","Two","More",
			"These","Want","Way","Look","First","Also","New","Because","Day","More","Use","No","Find","Here","Thing","Give","Many","THE",
			"BE","AND","OF","A","IN","TO","HAVE","TO","IT","I","THAT","FOR","YOU","HE","WITH","ON","DO","SAY","THIS","THEY","AT","BUT","WE",
			"HIS","FROM","THAT","NOT","N'T","N'T","BY","SHE","OR","AS","WHAT","GO","THEIR","CAN","WHO","GET","IF","WOULD","HER","ALL","MY",
			"MAKE","ABOUT","KNOW","WILL","AS","UP","ONE","THERE","YEAR","SO","THINK","WHEN","WHICH","THEM","SOME","ME","PEOPLE","TAKE","OUT",
			"INTO","JUST","SEE","HIM","YOUR","COME","COULD","NOW","THAN","LIKE","OTHER","HOW","THEN","ITS","OUR","TWO","MORE","THESE","WANT",
			"WAY","LOOK","FIRST","ALSO","NEW","BECAUSE","DAY","MORE","USE","NO","FIND","HERE","THING","GIVE","MANY"};
}
