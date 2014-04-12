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
	
	public int incomingTextCount; //present
	public int outgoingTextCount; //present
	
	public int incomingTextAverage;
	public int outgoingTextAverage;
	
	public long timeOfFirstText;
	
	public long totalIncomingDelay;
	public long totalOutgoingDelay;
	
	public double averageIncomingDelay; //present
	public double averageOutgoingDelay; //present
	
	public int outgoingConversationsStarted; //present
	public int incomingConversationsStarted; //present
	
	public ArrayList<InstructionHolder> instructions;
	
	public ContactHolder()
	{
		textMessages = new ArrayList<TextMessage>();
		instructions = new ArrayList<InstructionHolder>();
		incomingWordFrequency = new HashMap<String,Integer> ();
		outgoingWordFrequency = new HashMap<String,Integer> ();
		
		textReceivedLength = 0;
		textSentLength = 0;
		incomingTextCount = 0;
		outgoingTextCount = 0;
		incomingTextAverage = 0;
		outgoingTextAverage = 0;
		
		incomingConversationsStarted = 0; 
		outgoingConversationsStarted = 0;
	}
	
	public class InstructionHolder
	{
		String instruction;
		String value1;
		String value2;
	}
	
	public void addInstruction(String instruction, String value1, String value2)
	{
		if(instruction != null)
		{
			boolean found = false;
			for(int i=0; i<instructions.size(); i++)
			{
				if(instructions.get(i).instruction.equals(instruction))
				{
					if(value1 != null)
					{
						instructions.get(i).value1 = value1;
					}
					instructions.get(i).value2 = value2;
					found = true;
					break;
				}
			}
			
			if(!found)
			{
				InstructionHolder holder = new InstructionHolder();
				holder.instruction = instruction;
				holder.value1 = value1;
				holder.value2 = value2;
				instructions.add(holder);
			}
		}
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
			long delay = textMessages.get(i).timeCreated - textMessages.get(i - 1).timeCreated;
			
			if (currentDirection != textMessages.get(i).direction) //Ignore times of double texts
			{
				if (delay < MainActivity.ONE_HOUR) //conversation part of a convo
				{	
					if (currentDirection == Directions.INBOUND)
					{
						totalIncomingDelay += delay;
					}
					else 
					{
						totalOutgoingDelay += delay;
					}
				}
			}
			
			if (delay > MainActivity.ONE_HOUR * 24) //Started a conversation
			{
				if (currentDirection == Directions.INBOUND)
				{
					incomingConversationsStarted++;
				}
				else 
				{
					incomingConversationsStarted++;
				}	
			}
		}	
		averageIncomingDelay = ((int)(((totalIncomingDelay / incomingTextCount) / 1000) * 10)) / 10; //take average delay,convert to seconds, round to one digit
		averageOutgoingDelay = ((int)(((totalOutgoingDelay / outgoingTextCount) / 1000) * 10)) / 10; //take average delay,convert to seconds, round to one digit

	}
}