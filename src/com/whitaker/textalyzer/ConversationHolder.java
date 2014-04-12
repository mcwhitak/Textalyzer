package com.whitaker.textalyzer;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import android.util.Log;

import com.whitaker.textalyzer.MainActivity.ContactHolder;
import com.whitaker.textalyzer.TextMessage.Directions;


public class ConversationHolder {
		
	private class Person 
	{
		private ArrayList<TextMessage> textMessages; 
		private double averageOutboundLength;
		private double averageInboundLength;
		private int numOutbound;
		private int numInbound;
		private String name;
		
		public Person(ContactHolder contact)
		{
			this.name = contact.personName;
			this.textMessages = contact.textMessages;
			double inboundTotal = 0, outboundTotal = 0;
			
			for (int i = 0; i < textMessages.size(); i++)
			{
				if (textMessages.get(i).direction == Directions.INBOUND)
				{
					inboundTotal += textMessages.get(i).body.length();
					this.numInbound++;
				}
				else if (textMessages.get(i).direction == Directions.OUTBOUND)
				{
					outboundTotal += textMessages.get(i).body.length();
					this.numOutbound++;					
				}	
			}
			this.averageInboundLength = inboundTotal / this.numInbound;
			this.averageOutboundLength = outboundTotal / this.numOutbound;			
			
		}
		
	}
	
	private Map<Integer,Person> conversations;
	
	//Int -> Contact Holder (ArrayList, Name)
	public ConversationHolder(TreeMap<Integer,ContactHolder> textMap) 
	{
		this.conversations = new HashMap<Integer, Person> ();
        for(int key: textMap.keySet())
        {
        	Person person = new Person(textMap.get(key));
        	conversations.put(key, person);
        	Log.d("Roy Stuff.", person.name + " Outbound average length: " + conversations.get(key).averageOutboundLength);
        }
	}
	
	
	
}
