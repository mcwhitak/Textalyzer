package com.whitaker.textalyzer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.whitaker.textalyzer.ContactHolder;
import com.whitaker.textalyzer.TextMessage;
import com.whitaker.textalyzer.TextMessage.Directions;
import com.whitaker_iacob.textalyzer.R;

import android.app.Application;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class TextalyzerApplication extends Application
{
	private final String [] boringWords = {"the","be","and","of","a","in","to","have","it","it's","i","i'm","im","ok","for","you","he","with","on","do","say",
			"this","they","at","but","we","his","from","that","not","n't","by","she","or","what","was","go","their","can","who","get","is",
			"if","would","her","all","my","make","about","know","will","as","up","one","there","year","so","think","when","which","them","that's","did",
			"some","me","people","take","out","into","just","see","him","your","come","could","now","than","like","other","how","then","its",
			"our","two","these","want","way","look","first","also","new","because","day","more","use","no","find","here","thing","give",
			"many","are","a","e","o","u","b","c","d", "&", "yeah", "don't", "dont", "i'll"};
	
	private Map<String, ContactHolder> contacts;
	private List<Date> timesReceived;
	private List<Date> timesSent;
	private Map<String, String> nameMap;
	
	public void initMap()
	{
		contacts = new HashMap<String, ContactHolder>();
		timesReceived = new ArrayList<Date>();
		timesSent = new ArrayList<Date>();
		nameMap = new HashMap<String, String>();
	}
	public void putContact(String address, ContactHolder holder)
	{
		contacts.put(address, holder);
	}
	
	public ContactHolder getContact(String address)
	{
		return contacts.get(address);
	}
	
	public Set<String> getKeySet()
	{
		return contacts.keySet();
	}
	
	public int getSize()
	{
		return contacts.size();
	}
	
	public boolean isReady()
	{
		return (contacts == null) ? false : true; 
	}
	
	private void grabNumbers()
	{
		Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		while (phones.moveToNext())
		{
			String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			phoneNumber = addressClipper(phoneNumber);
			String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			nameMap.put(phoneNumber, name);
		}
		phones.close();
	}
	
	private void grabInbox()
	{
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
			
			if(getContact(address) == null)
			{
				String name = nameMap.get(address);
				if(name == null)
					continue;
				
				ContactHolder holder = new ContactHolder();
				holder.personName = name;
				holder.phoneNumber = address;

				int index = cursor.getColumnIndex("body");
				if(index == -1)
				{
					continue;
				}
				String body = cursor.getString(index).toLowerCase();
				
				if(body == null)
					continue;

				holder.textReceivedLength += body.length(); 
				
				determineWordFrequency(body, Directions.INBOUND, holder);
				
				holder.incomingTextCount++;
				holder.addInstruction(this.getString(R.string.info_pre_count), getString(R.string.info_pre_in) + holder.incomingTextCount, null);
				
				putContact(address, holder);
				long date = cursor.getLong(cursor.getColumnIndex("date"));
				TextMessage message = new TextMessage(Directions.INBOUND, body, date);
				holder.textMessages.add(message);
				timesReceived.add(new Date(date));
			}
			else
			{
				ContactHolder holder = getContact(address);
				int index = cursor.getColumnIndex("body");
				if(index == -1)
				{
					continue;
				}
				String body = cursor.getString(cursor.getColumnIndex("body")).toLowerCase();
				
				if(body == null)
					continue;
				
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
	}
	
	private void grabOutbox()
	{
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);
		cursor.moveToFirst();
		do
		{
			if(cursor.getCount() == 0)
				continue;
			
			String address = cursor.getString(cursor.getColumnIndex("address"));
			
			if(address == null)
				continue;
			address = addressClipper(address);

			ContactHolder holder = getContact(address);
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
	}
	
	public void populateMap()
	{
		grabNumbers();
	
		grabInbox();
		grabOutbox();
		
		for (String contactString: getKeySet())
		{
			getContact(contactString).analyze(this);
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
}