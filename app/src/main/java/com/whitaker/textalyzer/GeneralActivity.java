package com.whitaker.textalyzer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.db.chart.view.ChartView;
import com.whitaker.textalyzer.util.BounceListView;
import com.whitaker.textalyzer.util.TextalyzerActivity;
import com.whitaker.textalyzer.util.TextalyzerApplication;
import com.whitaker_iacob.textalyzer.R;

import com.db.chart.view.LineChartView;
import com.db.chart.model.LineSet;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
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
	
	private int totalIncomingTexts = 0;
	private int totalOutgoingTexts = 0;
	
	private String mostLikeleyToStartWithYou; //by started conversations
	private int mostLikeleyToStartWithYouCount; //by started conversations
	
	private String mostLikeleyToStartWithThem; //by started conversations
	private int mostLikeleyToStartWithThemCount; //by started conversations
	
	private String leastFavoriteContactForYou = "None"; // by started conversation - your started conversations
	private String leastFavoriteContactForThem = "None"; // by started conversation - your started conversations

	private LineSet lineSet;
	
	private ArrayList<String> adapterInstructions;
	
	private int [] textOGram = new int[]{0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0};

	private String[] categories = {"Total Incoming Texts", "Total Outgoing Texts","Most Likely to Start Conversations:", "With you", "You started",
					"Least Likely to Start Conversations:","With", "With you","Frequency of texts by hour", "", "WHY GOD"};

	private String[] timeLabels = {"12am", "1am", "2am", "3am", "4am", "5am", "6am", "7am", "8am", "9am", "10am", "11am",
									"12pm", "1pm", "2pm", "3pm", "4pm", "5pm", "6pm", "7am", "8pm", "9pm", "10pm", "11pm"};
	
	private int[] type = {0, 0, 1, 0, 0, 1 ,0, 0, 1, 2, 0};

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.general_stats);		
		grabAllViews();
		
		TextalyzerApplication app = (TextalyzerApplication)this.getApplication();
		if(!app.isReady())
		{
			app.initMap();
			app.populateMap();
		}
		
		adapterInstructions = new ArrayList<String>();
		lineSet = new LineSet();
		
		int leastFavoriteContactForYouCount = 0;
		int leastFavoriteContactForThemCount = 0;
		
		for (String key: app.getKeySet())
		{
			ContactHolder c = app.getContact(key);
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
				textOGram[new Date(t.timeCreated).getHours()]++; //getHours Deprecated & Overridden
			}	
		}
		
		//Create text for adapter
		adapterInstructions.add(Integer.toString(totalIncomingTexts));
		adapterInstructions.add(Integer.toString(totalOutgoingTexts));
		adapterInstructions.add("");
		adapterInstructions.add(mostLikeleyToStartWithYou + " by " + mostLikeleyToStartWithYouCount);
		adapterInstructions.add(mostLikeleyToStartWithThem + " by " + mostLikeleyToStartWithThemCount);
		adapterInstructions.add("");
		adapterInstructions.add(leastFavoriteContactForYou);
		adapterInstructions.add(leastFavoriteContactForThem);
		adapterInstructions.add("");
		adapterInstructions.add("BOB DICK");
		adapterInstructions.add("PENIS HAT DLXXXXX");
		for(int i=0; i<24; i++)
		{

			lineSet.addPoint(timeLabels[i], textOGram[i]);
		}
		itemListView.setAdapter(new ItemAdapter());
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
			int resID = R.layout.general_item;
			switch(type[position]) {
				case 2:
					resID = R.layout.general_linegraph;
					break;
				case 1:
					resID = R.layout.general_header;
					break;
			}

			View itemView = convertView;
			if(convertView == null || (int)convertView.getId() != (int)resID)
			{
				LayoutInflater li = getCtx().getLayoutInflater();
				itemView = li.inflate(resID, null);
			}
			
			if(resID == R.layout.general_item)
			{
				TextView catText = (TextView)itemView.findViewById(R.id.general_item_name);
				TextView resultText = (TextView)itemView.findViewById(R.id.general_item_result);
			
				if(position < categories.length )
				{
					catText.setText(categories[position]);
					resultText.setText(adapterInstructions.get(position));
				}
			}
			else if(resID == R.layout.general_header)
			{
				TextView breakText = (TextView)itemView.findViewById(R.id.breaktext);
				if(position < categories.length)
				{
					breakText.setText(categories[position]);
				}
			} else if (resID == R.layout.general_linegraph) {
				LineChartView chart = (LineChartView)itemView.findViewById(R.id.linechart);
				chart.addData(lineSet);
				chart.setFontSize(10);
				chart.show();
				chart.setVisibility(View.VISIBLE);
			}

			return itemView;
		}
	}	
	
	private Activity getCtx()
	{
		return this;
	}
}