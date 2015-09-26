package com.whitaker.textalyzer;

import com.whitaker.textalyzer.ContactHolder.InstructionHolder;
import com.whitaker.textalyzer.TextMessage.Directions;
import com.whitaker.textalyzer.util.BounceListView;
import com.whitaker.textalyzer.util.TextalyzerActivity;
import com.whitaker.textalyzer.util.TextalyzerApplication;
import com.whitaker_iacob.textalyzer.R;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DetailActivity extends TextalyzerActivity implements OnItemClickListener, OnClickListener
{
	private TextView scoreHeaderTextView;
	private TextView hideHeaderTextView;
	private TextView hideSubTextView;
	private BounceListView informationListView;
	private RelativeLayout headerRelativeView;
	private ContactHolder contactHolder;
	private InformationAdapter infoAdapter;
	private int tipIndex = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.detail_activity);

		Bundle b = getIntent().getExtras();
		String address = b.getString("address");
		grabAllViews();
		
		TextalyzerApplication app = (TextalyzerApplication)this.getApplication();
		if(!app.isReady())
		{
			app.initMap();
			app.populateMap();
		}
		contactHolder = app.getContact(address);
		
		Integer outgoing = 0;
		Integer incoming = 0;
		for(int i=0; i<contactHolder.textMessages.size(); i++)
		{
			if(contactHolder.textMessages.get(i).direction == Directions.INBOUND)
			{
				incoming++;
			}
			else
			{
				outgoing++;
			}
		}
		scoreHeaderTextView.setText(contactHolder.personName);
		int ratioLeft = (int)(contactHolder.getFriendshipRatio() * 100);
		int ratioRight = 100 - ratioLeft;
		String ratio = ratioLeft + ":" + ratioRight;
		
		if (contactHolder.getFriendshipRatio() < 0.44) 
		{
			hideSubTextView.setText("\"Friendship requires great communication.\"\n - Saint Francis de Sales");
		}
		else if (contactHolder.getFriendshipRatio() > 0.56)
		{
			hideSubTextView.setText("\"Many attempts to communicate are nullified by saying too much.\"\n - Robert Greenleaf");
		}
		else
		{
			hideSubTextView.setText("Hold a true friend and don't let go, for a true friend comes once in a lifetime.");
		}
		
		infoAdapter = new InformationAdapter();
		informationListView.setAdapter(infoAdapter);
		informationListView.setOverScrollMode(0);
		informationListView.setOnItemClickListener(this);
		headerRelativeView.setOnClickListener(this);	
	}
	
	private void grabAllViews()
	{
		scoreHeaderTextView = (TextView)findViewById(R.id.score_header);
		hideHeaderTextView = (TextView)findViewById(R.id.hide_header);
		hideSubTextView = (TextView)findViewById(R.id.hide_sub);
		informationListView = (BounceListView)findViewById(R.id.list_information);
		headerRelativeView = (RelativeLayout)findViewById(R.id.relative_header);
	}
	
	private class InformationAdapter extends BaseAdapter
	{
		@Override
		public int getCount() 
		{
			return contactHolder.instructions.size();
		}

		@Override
		public Object getItem(int position)
		{
			return contactHolder.instructions.get(position);
		}

		@Override
		public long getItemId(int position) 
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			View itemView = convertView;
			if(convertView == null)
			{
				LayoutInflater li = getCtx().getLayoutInflater();
				itemView = li.inflate(R.layout.result_item, null);
			}
			
			TextView catText = (TextView)itemView.findViewById(R.id.result_item_category);
			TextView val1Text = (TextView)itemView.findViewById(R.id.result_item_value1);
			TextView val2Text = (TextView)itemView.findViewById(R.id.result_item_value2);
			TextView hintText = (TextView)itemView.findViewById(R.id.result_tips);
			
			if(position < contactHolder.instructions.size())
			{
				InstructionHolder iHolder = contactHolder.instructions.get(position);
				
				catText.setText(iHolder.instruction);
				
				if(iHolder.instruction.equals(getString(R.string.info_pre_delay)))
				{
					if (0 <= contactHolder.getDelayRatio() && contactHolder.getDelayRatio() < 0.40) 
					{
						hintText.setText("Someone seems to likes you!");
					}
					else if (contactHolder.getDelayRatio() > 0.60)
					{
						hintText.setText("Well this is awkward...");
					} 
					else
					{
						hintText.setText("Do you set a timer to remind you when to reply?");
					}
				}
					
				else if(iHolder.instruction.equals(getString(R.string.info_pre_count)))
				{
					if (contactHolder.getTextCountRatio() < 0.40 && contactHolder.getTextCountRatio() >= 0) 
					{
						hintText.setText("Are you giving " + contactHolder.personName + " the cold shoulder? Rude!");
					}
					else if (contactHolder.getTextCountRatio() > 0.60)
					{
						hintText.setText(contactHolder.personName + " has a sticky enter button.");
					}

					else
					{
						hintText.setText("How polite, a one to one relationship. ");
					}
					
				}
					
				else if(iHolder.instruction.equals(getString(R.string.info_pre_length))) //average length
				{
					if (contactHolder.getTextAverageRatio() < 0.40 && contactHolder.getTextAverageRatio() >= 0) 
					{
						hintText.setText("C\'mon, beef up those texts with a few more characters.");
					}
					else if (contactHolder.getTextAverageRatio() > 0.60)
					{
						hintText.setText(contactHolder.personName + " sure is a fast typer...");
					}
					else
					{
						hintText.setText("Are you two purposefully matching each other in length? I'm onto you...");
					}
				}
				else if(iHolder.instruction.equals(getString(R.string.info_pre_convo)))
				{
					if (contactHolder.getConversationsStartedRatio() < 0.40 && contactHolder.getConversationsStartedRatio() >= 0) 
					{
						hintText.setText("Maybe you should start the conversation once in a while.");
					}
					else if (contactHolder.getConversationsStartedRatio() > 0.60)
					{
						hintText.setText("Let " + contactHolder.personName + " start the conversation for once.");
					}
					else
					{
						hintText.setText("You two are a chatty bunch.");
					}
				}					
				else if(iHolder.instruction.equals(getString(R.string.info_pre_common)))
				{
					hintText.setText("Your other favorite words: \"" + contactHolder.outgoingMostCommon[1] + "\" and \"" + contactHolder.outgoingMostCommon[2]
							+ "\"\n" + contactHolder.personName + "\'s other favorite words: \"" +contactHolder.incomingMostCommon[1] + "\" and \"" +contactHolder.incomingMostCommon[2] + "\"");
				}
				else if(iHolder.instruction.equals(getString(R.string.info_pre_emote)))
				{
					if (contactHolder.getEmoticonsCountRatio() < 0.40 && contactHolder.getEmoticonsCountRatio() >= 0) 
					{
						hintText.setText("Spice up this convo with a winkie face.");
					}
					else if (contactHolder.getEmoticonsCountRatio() > 0.60)
					{
						hintText.setText("Slow it down buddy...");
					}
					else
					{
						hintText.setText("You are a true professional.");
					}
				}
				
				val1Text.setText(iHolder.value1);
				if(iHolder.value2 != null)
				{
					val2Text.setText(iHolder.value2);
					val2Text.setVisibility(View.VISIBLE);
				}
				else
				{
					val2Text.setVisibility(View.GONE);
				}
			}
			
			return itemView;
		}
	}
	
	private Activity getCtx()
	{
		return this;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		RelativeLayout main = (RelativeLayout)view.findViewById(R.id.result_initial);
		if(tipIndex == position)
		{
			parent.getChildAt(position).findViewById(R.id.result_initial).setVisibility(View.VISIBLE);
			parent.getChildAt(position).findViewById(R.id.result_initial).startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
			parent.getChildAt(position).findViewById(R.id.result_tips).setVisibility(View.INVISIBLE);
			parent.getChildAt(position).findViewById(R.id.result_tips).startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
			tipIndex = -1;
		}
		else
		{
			parent.getChildAt(position).findViewById(R.id.result_initial).setVisibility(View.INVISIBLE);
			parent.getChildAt(position).findViewById(R.id.result_initial).startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
			parent.getChildAt(position).findViewById(R.id.result_tips).setVisibility(View.VISIBLE);
			parent.getChildAt(position).findViewById(R.id.result_tips).startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
			if(tipIndex != -1)
			{
				parent.getChildAt(tipIndex).findViewById(R.id.result_initial).setVisibility(View.VISIBLE);
				parent.getChildAt(tipIndex).findViewById(R.id.result_initial).startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
				parent.getChildAt(tipIndex).findViewById(R.id.result_tips).setVisibility(View.INVISIBLE);
				parent.getChildAt(tipIndex).findViewById(R.id.result_tips).startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
			}
			tipIndex = position;
		}
	}

	@Override
	public void onClick(View v) 
	{
		if(v == headerRelativeView)
		{
			if(scoreHeaderTextView.getVisibility() == View.VISIBLE)
			{
				scoreHeaderTextView.setVisibility(View.INVISIBLE);
				scoreHeaderTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_short));
				hideHeaderTextView.setVisibility(View.VISIBLE);
				hideHeaderTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_short));
				hideSubTextView.setVisibility(View.VISIBLE);
				hideSubTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_short));
			}
			else
			{
				scoreHeaderTextView.setVisibility(View.VISIBLE);
				scoreHeaderTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_short));
				hideHeaderTextView.setVisibility(View.INVISIBLE);
				hideHeaderTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_short));
				hideSubTextView.setVisibility(View.INVISIBLE);
				hideSubTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_short));

			}
		}
	}
}