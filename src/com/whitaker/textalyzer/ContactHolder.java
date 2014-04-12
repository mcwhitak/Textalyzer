package com.whitaker.textalyzer;

import java.util.ArrayList;
import java.util.HashMap;

import com.whitaker.textalyzer.TextMessage.Directions;

public class ContactHolder
{
	public int personId;
	public int textReceivedLength;
	public int textSentLength;
	public String personName;
	public String phoneNumber;
	public ArrayList<TextMessage> textMessages;
	public HashMap<String,Integer> incomingWordFrequency;
	public HashMap<String,Integer> outgoingWordFrequency;
	
	public int incomingTextCount;
	public int outgoingTextCount;
	
	public int incomingTextAverage;
	public int outgoingTextAverage;
	
	public long timeOfFirstText;
	
	public long totalIncomingDelay;
	public long totalOutgoingDelay;
	
	
	public ContactHolder()
	{
		textMessages = new ArrayList<TextMessage>();
		incomingWordFrequency = new HashMap<String,Integer> ();
		outgoingWordFrequency = new HashMap<String,Integer> ();
		
		textReceivedLength = 0;
		textSentLength = 0;
		incomingTextCount = 0;
		outgoingTextCount = 0;
		incomingTextAverage = 0;
		outgoingTextAverage = 0;
	}
	
	public void analyze() 
	{
		incomingTextAverage = textReceivedLength / incomingTextCount;
		outgoingTextAverage = textReceivedLength / incomingTextCount;
		//TODO Calculate most common words, filter out articles, pronouns, etc
		timeOfFirstText = textMessages.get(0).timeCreated;
		Directions currentDirection = textMessages.get(0).direction;
		for (int i = 1; i < textMessages.size(); i++) //maybe size - 1
		{
			currentDirection = textMessages.get(i).direction;
			if (currentDirection != textMessages.get(i).direction) //Ignore times of double texts
			{
				long delay = textMessages.get(i).timeCreated - textMessages.get(i - 1).timeCreated;
				//if (delay < ONE_HOUR) //conversation part of a convo
				//{	
					if (currentDirection == Directions.INBOUND)
					{
						totalIncomingDelay += delay;
					}
					else 
					{
						totalOutgoingDelay += delay;
					}
				//}
			}
		}
		
			
				
	}
}