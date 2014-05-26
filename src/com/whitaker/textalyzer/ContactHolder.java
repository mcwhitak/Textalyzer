package com.whitaker.textalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;

import com.whitaker.textalyzer.TextMessage.Directions;
import com.whitaker_iacob.textalyzer.R;

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
	
	public int incomingTextCount; //print
	public int outgoingTextCount; 
	private double textCountRatio;
	
	public int incomingTextAverage; //print
	public int outgoingTextAverage; 
	private double textAverageRatio;
	
	public long totalIncomingDelay;
	public long totalOutgoingDelay;
	
	public double averageIncomingDelay; //print
	public double averageOutgoingDelay; 
	private double delayRatio;
	
	public int outgoingConversationsStarted; //print
	public int incomingConversationsStarted; 
	private double conversationsStartedRatio;
	
	public String [] incomingMostCommon = {"","",""}; //print
	public String [] outgoingMostCommon = {"","",""};
	
	public int incomingEmoticonsCount; //print
	public int outgoingEmoticonsCount;
	private double emoticonsCountRatio;
	
	private double friendshipRatio;
	
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
		
		averageIncomingDelay = 0;
		averageOutgoingDelay = 0;
	}
	
	public class InstructionHolder
	{
		String instruction;
		String value1;
		String value2;
	}
	
	public double getTextCountRatio()
	{
		int total = incomingTextCount + outgoingTextCount;
		if (total == 0)
		{
			textCountRatio = -1;
		}
		else
		{
			textCountRatio = (double) outgoingTextCount / (double) total;
		}
		return textCountRatio;
	}
	
	public double getTextAverageRatio() 
	{
		int total = incomingTextAverage + outgoingTextAverage;
		if (total == 0) textAverageRatio = -1;
		else
			textAverageRatio = (double) outgoingTextAverage / (double) total;
		return textAverageRatio;
	}
	
	public double getDelayRatio() 
	{
		double total = averageIncomingDelay + averageOutgoingDelay;
		if (total == 0) delayRatio = -1;
		else
			delayRatio = (double) averageOutgoingDelay / (double) total;
		return delayRatio;
	}
	
	public double getConversationsStartedRatio() 
	{
		double total = incomingConversationsStarted + outgoingConversationsStarted;
		if (total == 0) conversationsStartedRatio = -1;
		else 
			conversationsStartedRatio = (double) outgoingConversationsStarted / (double) total;
		return conversationsStartedRatio;
	}
	
	public double getEmoticonsCountRatio() 
	{
		double total = incomingEmoticonsCount + outgoingEmoticonsCount;
		if (total == 0) emoticonsCountRatio = -1;
		else
			emoticonsCountRatio = (double) outgoingEmoticonsCount / (double) total;
		return emoticonsCountRatio;
	}
	
	public double getFriendshipRatio()   //TODO shouldn't recalculate these thangs
	{
		friendshipRatio = 0;
		
		if (getTextCountRatio() != -1)
			friendshipRatio += getTextCountRatio();
		if (getTextAverageRatio() != -1)
			friendshipRatio += getTextAverageRatio();
		if (getDelayRatio() != -1)
			friendshipRatio += getDelayRatio();
		if (getConversationsStartedRatio() != -1)
			friendshipRatio += getConversationsStartedRatio();
		if (getEmoticonsCountRatio() != -1)
			friendshipRatio += getEmoticonsCountRatio();
		
		friendshipRatio /= 5;
		return friendshipRatio;
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
		//Calculate Average Text Length
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
			
			//Count delay
			if (currentDirection != textMessages.get(i - 1).direction) //Ignore times of double texts
			{
				if (delay < MainActivity.ONE_HOUR) //conversation part of a convo 3,600,000
				{	
					if (currentDirection == Directions.INBOUND)
					{
						totalIncomingDelay += delay;
					}
					else if (currentDirection == Directions.OUTBOUND) 
					{
						totalOutgoingDelay += delay;
					}
				}
			}
			
			//Count conversations
			if (delay > MainActivity.ONE_HOUR * 6) //Started a new conversation
			{
				if (currentDirection == Directions.INBOUND)
				{
					incomingConversationsStarted++;
				}
				else if (currentDirection == Directions.OUTBOUND)
				{
					outgoingConversationsStarted++;
				}	
			}
	        
			//Count emoticons
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
			else if (currentDirection == Directions.OUTBOUND) 
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
				
		addInstruction(ctx.getString(R.string.info_pre_convo), ctx.getString(R.string.info_pre_in)+ incomingConversationsStarted, ctx.getString(R.string.info_pre_out) + outgoingConversationsStarted);
		
		if(incomingTextCount != 0)
		{
			averageIncomingDelay = ((int)(((((double)totalIncomingDelay / (double)incomingTextCount) / 60000.0)) * 10.0  ))/10.0; //take average delay,convert to seconds, round to one digit
			addInstruction(ctx.getString(R.string.info_pre_delay), ctx.getString(R.string.info_pre_in) + averageIncomingDelay + " min", null); 
		}
		
		if(outgoingTextCount != 0)
		{
			averageOutgoingDelay = ((int)(((((double)totalOutgoingDelay / (double)outgoingTextCount) / 60000.0)) * 10.0 ))/10.0; //take average delay,convert to seconds, round to one digit
			addInstruction(ctx.getString(R.string.info_pre_delay), null, ctx.getString(R.string.info_pre_out) + averageOutgoingDelay + " min");
		}
		
		//Count most common words
		int [] maxes = {0,0,0};
		String [] words = {"","",""}; 
		if (incomingWordFrequency.keySet().size() >= 3)
		{
			for (String word: incomingWordFrequency.keySet())
			{
				int value = incomingWordFrequency.get(word);
				
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
		if (outgoingWordFrequency.keySet().size() >= 3)
		{
			//while(it.hasNext())
			for (String word: outgoingWordFrequency.keySet())
			{
				int value = outgoingWordFrequency.get(word);
				
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
		else if (outgoingWordFrequency.keySet().size() == 2)
		{
			outgoingMostCommon[0] = (String) outgoingWordFrequency.keySet().toArray()[0];
			outgoingMostCommon[1] = (String) outgoingWordFrequency.keySet().toArray()[1];	
		}
		else if (outgoingWordFrequency.keySet().size() == 1)
		{
			outgoingMostCommon[0] = (String) outgoingWordFrequency.keySet().toArray()[0];
		}

		addInstruction(ctx.getString(R.string.info_pre_common), ctx.getString(R.string.info_pre_in) + incomingMostCommon[0], ctx.getString(R.string.info_pre_out) + outgoingMostCommon[0]);
		addInstruction(ctx.getString(R.string.info_pre_emote), ctx.getString(R.string.info_pre_in) + incomingEmoticonsCount, ctx.getString(R.string.info_pre_out) + outgoingEmoticonsCount);
	}
	
	public static class ContactComparator implements Comparator
	{
		@Override
		public int compare(Object lhs, Object rhs) 
		{
			ContactHolder l = (ContactHolder)lhs;
			ContactHolder r = (ContactHolder)rhs;
			
			Integer li = l.incomingTextCount + l.outgoingTextCount;
			Integer ri = r.incomingTextCount + r.outgoingTextCount;
			return ri.compareTo(li);
		}
		
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
