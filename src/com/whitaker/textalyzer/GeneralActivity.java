package com.whitaker.textalyzer;

import java.util.Date;
import java.util.HashMap;
import com.whitaker.textalyzer.util.BounceListView;
import com.whitaker.textalyzer.util.TextalyzerActivity;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GeneralActivity extends TextalyzerActivity
{
	private BounceListView itemListView;
	private int [] textOGram = new int[]{0,0,0,0,0,0,
										0,0,0,0,0,0,
										0,0,0,0,0,0,
										0,0,0,0,0,0};
	
	private String[] categories = {"Total Incoming Texts", "Total Outgoing Texts","Most Likely to Start Conversations...", "With you", "You started",
			"Least Likely to Start Conversations...","With", "With you","Frequency of texts by hour",
			"12 am","1 am","2 am","3 am","4 am","5 am","6 am","7 am","8 am","9 am","10 am","11 am",
			"12 pm","1 pm","2 pm","3 pm","4 pm","5 pm","6 pm","7 pm","8 pm","9 pm","10 pm","11 pm"
	};
	
	private static HashMap<String, ContactHolder> contactMap = MainActivity.contactMap;
	
	private int totalIncomingTexts = 0;
	private int totalOutgoingTexts = 0;
	
	private String mostLikeleyToStartWithYou; //by started conversations
	private int mostLikeleyToStartWithYouCount; //by started conversations
	
	private String mostLikeleyToStartWithThem; //by started conversations
	private int mostLikeleyToStartWithThemCount; //by started conversations
	
	private String leastFavoriteContactForYou = "None"; // by started conversation - your started conversations
	private String leastFavoriteContactForThem = "None"; // by started conversation - your started conversations
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.general_stats);		
		grabAllViews();
		itemListView.setAdapter(new ItemAdapter());
		
		int leastFavoriteContactForYouCount = 0;
		int leastFavoriteContactForThemCount = 0;
		
		for (String key: contactMap.keySet())
		{
			ContactHolder c = contactMap.get(key);
			totalIncomingTexts += c.incomingTextCount;
			totalOutgoingTexts += c.outgoingTextCount;
			
			if (mostLikeleyToStartWithYouCount < c.incomingConversationsStarted)
			{
				mostLikeleyToStartWithYouCount = c.incomingConversationsStarted;
				mostLikeleyToStartWithYou = c.personName;
			}
			
			if (mostLikeleyToStartWithThemCount < c.outgoingConversationsStarted)
			{
				mostLikeleyToStartWithThemCount = c.outgoingConversationsStarted;
				mostLikeleyToStartWithThem = c.personName;
			}
			
			if (leastFavoriteContactForThemCount < (c.outgoingConversationsStarted - c.incomingConversationsStarted))
			{
				leastFavoriteContactForThemCount = c.outgoingConversationsStarted - c.incomingConversationsStarted;
				leastFavoriteContactForThem = c.personName;
			}
			
			if (leastFavoriteContactForYouCount < (c.incomingConversationsStarted - c.outgoingConversationsStarted))
			{
				leastFavoriteContactForYouCount = c.incomingConversationsStarted - c.outgoingConversationsStarted;
				leastFavoriteContactForYou = c.personName;
			}
			
			for (TextMessage t: c.textMessages)
			{
				textOGram[new Date(t.timeCreated).getHours()]++;
				//Log.d("Royyy",new Date(t.timeCreated).getHours() + ": " + textOGram[new Date(t.timeCreated).getHours()]);
			}	
		}

		 //by started conversations

	}
	
	private void grabAllViews()
	{
		itemListView = (BounceListView)findViewById(R.id.list_general);
	}
	
	private class ItemAdapter extends BaseAdapter
	{
		@Override
		public int getCount() 
		{
			return categories.length;
		}

		@Override
		public Object getItem(int pos) 
		{
			return categories[pos];
		}

		@Override
		public long getItemId(int pos) 
		{
			return pos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View itemView = convertView;
			if(convertView == null)
			{
				LayoutInflater li = getCtx().getLayoutInflater();
				itemView = li.inflate(R.layout.general_item, null);
			}
			
			TextView catText = (TextView)itemView.findViewById(R.id.general_item_name);
			TextView resultText = (TextView)itemView.findViewById(R.id.general_item_result);
			
			if(position < categories.length)
			{
				catText.setText(categories[position]);
				if (position == 0)
				{
					resultText.setText(Integer.toString(totalIncomingTexts)); 
				}
				else if (position == 1)
				{
					resultText.setText(Integer.toString(totalOutgoingTexts));
				}
				else if (position == 2)
				{
					resultText.setText("");
				}
				
				else if (position == 3)
				{
					resultText.setText(mostLikeleyToStartWithYou + " by " + mostLikeleyToStartWithYouCount);
				}
				else if (position == 4)
				{
					resultText.setText(mostLikeleyToStartWithThem + " by " + mostLikeleyToStartWithThemCount);
				}
				else if (position == 5)
				{
					
					resultText.setText("");
				}
				
				else if (position == 6)
				{
					resultText.setText(leastFavoriteContactForYou);
				}
				else if (position == 7)
				{
					resultText.setText(leastFavoriteContactForThem);
				}
				else if (position == 8)
				{
					resultText.setText("");
				}
				else if (position > 8 && position < 24)
				{
					resultText.setText(Integer.toString(textOGram[position-7]) + " texts, " + (double)Math.round((100*(double)textOGram[position-7]/(totalIncomingTexts+totalOutgoingTexts)) * 10) / 10 + "%");	
				}

			}
			return itemView;
		}
		
	}	
	
	private Activity getCtx()
	{
		return this;
	}
}