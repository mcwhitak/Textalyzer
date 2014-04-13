package com.whitaker.textalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;

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
	
	public int averageIncomingDelay; //idgaf about fractions of a second.
	public int averageOutgoingDelay; 
	
	public int outgoingConversationsStarted; 
	public int incomingConversationsStarted; 
	
	public String [] incomingMostCommon = {"","",""};	
	public String [] outgoingMostCommon = {"","",""};
	
	public int incomingEmoticonsCount;
	public int outgoingEmoticonsCount;
	
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
		incomingEmoticonsCount = 0;
		outgoingEmoticonsCount = 0;
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
	
	public void analyze(Context ctx) 
	{
		incomingTextAverage = 0;
		if(incomingTextCount != 0)
		{
			incomingTextAverage = textReceivedLength / incomingTextCount;
			addInstruction(ctx.getString(R.string.info_pre_length), ctx.getString(R.string.info_pre_in)+ incomingTextAverage, null);
		}
		
		outgoingTextAverage = 0;
		if(outgoingTextCount != 0)
		{
			outgoingTextAverage = textSentLength / outgoingTextCount;
			addInstruction(ctx.getString(R.string.info_pre_length), null, ctx.getString(R.string.info_pre_out) + outgoingTextAverage);
		}
		
		Collections.sort(textMessages, new Comparator<TextMessage>() {
	        @Override
	        public int compare(TextMessage o1, TextMessage o2) {
	            return Double.compare(o1.timeCreated, o2.timeCreated);
	        }
	    });
		
		Directions currentDirection = textMessages.get(0).direction;
		for (int i = 1; i < textMessages.size(); i++)
		{
			currentDirection = textMessages.get(i).direction;
			long delay = textMessages.get(i).timeCreated - textMessages.get(i - 1).timeCreated;
			
			if (currentDirection != textMessages.get(i - 1).direction) //Ignore times of double texts
			{
				if (delay < MainActivity.ONE_HOUR) //conversation part of a convo 3,600,000
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
			
			if (delay > MainActivity.ONE_HOUR * 9) //Started a new conversation
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
	        

			if (currentDirection == Directions.INBOUND)
			{
				for (String e: emoticons)
				{
					if (textMessages.get(i).body.contains(e))
					{
						incomingEmoticonsCount++;
					}
			
				}					
			}
			else 
			{
				for (String e: emoticons)
				{
					if (textMessages.get(i).body.contains(e))
					{
						outgoingEmoticonsCount++;
					}
				}
			}
		}	
		
		addInstruction(ctx.getString(R.string.info_pre_common), ctx.getString(R.string.info_pre_in)+ incomingConversationsStarted, ctx.getString(R.string.info_pre_out) + outgoingConversationsStarted);
		
		averageIncomingDelay = 0; 
		averageOutgoingDelay = 0;

		if(incomingTextCount != 0)
		{
			averageIncomingDelay = (int)(((totalIncomingDelay / incomingTextCount) / 1000)); //take average delay,convert to seconds, round to one digit
			addInstruction(ctx.getString(R.string.info_pre_delay), ctx.getString(R.string.info_pre_in) + averageIncomingDelay, null); 
		}
		
		if(outgoingTextCount != 0)
		{
			averageOutgoingDelay = (int)(((totalOutgoingDelay / outgoingTextCount) / 1000)); //take average delay,convert to seconds, round to one digit
			addInstruction(ctx.getString(R.string.info_pre_delay), null, ctx.getString(R.string.info_pre_out) + averageOutgoingDelay);
		}
		
		int [] maxes = {0,0,0};
		String [] words = {"","",""}; 
		Iterator it = incomingWordFrequency.entrySet().iterator();
		if (incomingWordFrequency.keySet().size() >= 3)
		{
			while(it.hasNext())
			{
				Map.Entry pairs = (Map.Entry)it.next();
				int value = (Integer)pairs.getValue();
				String word = (String)pairs.getKey();
				
				for (int i = 0; i < 3; i++)
				{
					if (maxes[i] < value)
					{
						int iSmallest = 0;
						for (int j = 0; j < 3; j++)
						{
							if (maxes[j] < maxes[iSmallest])
							{
								iSmallest = j;
							}
							
						}
						maxes[iSmallest] = value;
						words[iSmallest] = word;
						break;					
					}
				}
			}
			incomingMostCommon[0] = words[0];
			incomingMostCommon[1] = words[1];
			incomingMostCommon[2] = words[2];
		}
		else if (incomingWordFrequency.keySet().size() == 2)
		{
			incomingMostCommon[0] = (String) incomingWordFrequency.keySet().toArray()[0];
			incomingMostCommon[1] = (String) incomingWordFrequency.keySet().toArray()[1];	
		}
		else if (incomingWordFrequency.keySet().size() == 1)
		{
			incomingMostCommon[0] = (String) incomingWordFrequency.keySet().toArray()[0];
		}

		
		//Do it again for outgoing
		maxes[0] = 0; maxes[1] = 0; maxes[2] = 0;
		words[0] = words[1] = words[2] = "";
		it = outgoingWordFrequency.entrySet().iterator();
		if (outgoingWordFrequency.keySet().size() >= 3)
		{
			while(it.hasNext())
			{
				Map.Entry pairs = (Map.Entry)it.next();
				int value = (Integer)pairs.getValue();
				String word = (String)pairs.getKey();
				
				for (int i = 0; i < 3; i++)
				{
					if (maxes[i] < value)
					{
						int iSmallest = 0;
						for (int j = 0; j < 3; j++)
						{
							if (maxes[j] < maxes[iSmallest])
							{
								iSmallest = j;
							}
							
						}
						maxes[iSmallest] = value;
						words[iSmallest] = word;
						break;					
					}
				}
			}
			outgoingMostCommon[0] = words[0];
			outgoingMostCommon[1] = words[1];
			outgoingMostCommon[2] = words[2];
		}
		else if (incomingWordFrequency.keySet().size() == 2)
		{
			outgoingMostCommon[0] = (String) outgoingWordFrequency.keySet().toArray()[0];
			outgoingMostCommon[1] = (String) outgoingWordFrequency.keySet().toArray()[1];	
		}
		else if (outgoingWordFrequency.keySet().size() == 1)
		{
			outgoingMostCommon[0] = (String) outgoingWordFrequency.keySet().toArray()[0];
		}
		
		
		//TODO add top 3 most common words to tip panel
		addInstruction(ctx.getString(R.string.info_pre_common), ctx.getString(R.string.info_pre_in) + incomingMostCommon, ctx.getString(R.string.info_pre_out) + outgoingMostCommon);

		//TODO add emoticon to panel

	}

	String [] emoticons = {":-)",":)",":o)",":]",":3",":c)",":>","=]","8)","=)",":}",":^)",":-D",":D","8-D","8D","x-D","xD",
			"X-D","XD","=-D","=D","=-3","=3","B^D",":-))",">:[",":-(",":(",":-c",":c",":-<",":<",":-[",":[",":{",";(",":-||",
			":@",">:(",":\'-(",":\'(",":\'-)",":\')","D:<","D:","D8","D;","D=","DX","v.v","D-\':",">:O",":-O",":O",":-o",":o",
			"8-0","O_O","o-o","O_o","o_O","o_o","O-O",":*",":^*","(\'}{\')",";-)",";)",";-]",";]",";D",";^)",":-,",">:P",
			":-P",":P","X-P","x-p","xp","XP",":-p",":p","=p",":-b",":b","d:",">:\\",">:/",":-/",":-.",":/",
			":\\","=/","=\\",":L","=L",":S",">.<",":|",":-|",":$",":-X",":X",":-#",":#","O:-)","0:-3","0:3","0:-)","0:)","0;^)",
			">:)",">;)",">:-)","}:-)","}:)","3:-)","3:)","o/\\o",">_>^","^<_<","|;-)","|-O",":-&",":&","#-)","%-)",":-###..",
			":###..","<:-|","<*)))-{","><(((*>","\\o/","*\0/*","@}-;-\'---","@>-->--","~(_8^(I)","5:-)","~:-\\","//0-0\\",
			"*<|:-)","=:o]",",:-)","<3","</3"};

}