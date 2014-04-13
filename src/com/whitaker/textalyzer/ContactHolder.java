package com.whitaker.textalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.util.Log;

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
	
	public int incomingTextCount; //present DONE
	public int outgoingTextCount; //present
	
	public int incomingTextAverage; //present DONE
	public int outgoingTextAverage; //present
	
	public long timeOfFirstText;
	
	public long totalIncomingDelay;
	public long totalOutgoingDelay;
	
	public double averageIncomingDelay; //present DONE
	public double averageOutgoingDelay; //present
	
	public int outgoingConversationsStarted; //present MESSED UP
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
		incomingTextAverage = 0;
		if(incomingTextCount != 0)
		{
			incomingTextAverage = textReceivedLength / incomingTextCount;
			addInstruction("Average Length", "Incoming: " + incomingTextAverage, null);
		}
		
		outgoingTextAverage = 0;
		if(outgoingTextCount != 0)
		{
			outgoingTextAverage = textSentLength / outgoingTextCount;
			addInstruction("Average Length", null, "Outgoing: " + outgoingTextAverage);
		}
		
		Collections.sort(textMessages, new Comparator<TextMessage>() {
	        @Override
	        public int compare(TextMessage o1, TextMessage o2) {
	            return Double.compare(o1.timeCreated, o2.timeCreated);
	        }
	    });
		
		//TODO Calculate most common words, filter out articles, pronouns, etc
		timeOfFirstText = textMessages.get(0).timeCreated;
		Directions currentDirection = textMessages.get(0).direction;
		for (int i = 1; i < textMessages.size(); i++) //maybe size - 1
		{
			currentDirection = textMessages.get(i).direction;
			long delay = textMessages.get(i).timeCreated - textMessages.get(i - 1).timeCreated;
			Log.d("Royyy",personName + "direction: " + textMessages.get(i).direction + " delay:" + (delay));
			
		   
			
			
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
			
			if (delay > MainActivity.ONE_HOUR * 9) //Started a conversation
			{
				if (currentDirection == Directions.INBOUND)
				{
					incomingConversationsStarted++;
				}
				else 
				{
					outgoingConversationsStarted++;
				}	
			}
		}	
		
		addInstruction("Coversations Started", "Them: " + incomingConversationsStarted, "You: " + outgoingConversationsStarted);
		
		
		
		averageIncomingDelay = 0; 
		averageOutgoingDelay = 0;

		
		
		if(incomingTextCount != 0)
		{
			averageIncomingDelay = ((int)(((totalIncomingDelay / incomingTextCount) / 1000) * 10)) / 10; //take average delay,convert to seconds, round to one digit
			//Log.d("Royyy",personName + " " + (this.totalIncomingDelay));
			addInstruction("Average Delay", "Incoming: " + averageIncomingDelay, null);
			 
		}
		
		if(outgoingTextCount != 0)
		{
			averageOutgoingDelay = ((int)(((totalOutgoingDelay / outgoingTextCount) / 1000) * 10)) / 10; //take average delay,convert to seconds, round to one digit
			addInstruction("Average Delay", null, "Outcoming: " + averageOutgoingDelay);
		}

	}
}