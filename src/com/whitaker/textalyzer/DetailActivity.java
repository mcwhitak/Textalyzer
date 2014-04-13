package com.whitaker.textalyzer;

import com.whitaker.textalyzer.ContactHolder.InstructionHolder;
import com.whitaker.textalyzer.TextMessage.Directions;

import android.app.Activity;
import android.os.Bundle;
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

public class DetailActivity extends Activity implements OnItemClickListener, OnClickListener
{
	private TextView scoreHeaderTextView;
	private TextView scoreValueTextView;
	private TextView hideHeaderTextView;
	private TextView hideSubTextView;
	private ListView informationListView;
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
		Integer id = b.getInt("id");
		grabAllViews();
		
		contactHolder = MainActivity.getContactHolder(id);
		
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
		scoreHeaderTextView.setText("Friend Score: " + contactHolder.personName);
		infoAdapter = new InformationAdapter();
		informationListView.setAdapter(infoAdapter);
		informationListView.setOnItemClickListener(this);
		headerRelativeView.setOnClickListener(this);
	}
	
	private void grabAllViews()
	{
		scoreHeaderTextView = (TextView)findViewById(R.id.score_header);
		scoreValueTextView = (TextView)findViewById(R.id.score_value);
		hideHeaderTextView = (TextView)findViewById(R.id.hide_header);
		hideSubTextView = (TextView)findViewById(R.id.hide_sub);
		informationListView = (ListView)findViewById(R.id.list_information);
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
					hintText.setText(getString(R.string.tip_delay));
				else if(iHolder.instruction.equals(getString(R.string.info_pre_count)))
					hintText.setText(getString(R.string.tip_count));
				else if(iHolder.instruction.equals(getString(R.string.info_pre_length)))
					hintText.setText(getString(R.string.tip_length));
				else if(iHolder.instruction.equals(getString(R.string.info_pre_convo)))
					hintText.setText(getString(R.string.tip_convo));
				else if(iHolder.instruction.equals(getString(R.string.info_pre_common)))
				{
					hintText.setText("Your other favorite words: " + contactHolder.outgoingMostCommon[1] + " and " + contactHolder.outgoingMostCommon[2]
							+ "\nFriend\'s favorite words: " +contactHolder.outgoingMostCommon[1] + ", " +contactHolder.outgoingMostCommon[2]);
				}
				else if(iHolder.instruction.equals(getString(R.string.info_pre_emote)))
				{
					if (contactHolder.outgoingEmoticonsCount > 3) //TODO ratio
						hintText.setText("Slow it down buddy...");
					else
						hintText.setText("Spice up this convo with a winkie face.");
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
	
	public void setContentView(int res)
	{
		super.setContentView(res);
		getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
        	case android.R.id.home:
        		super.onBackPressed();
        		break;	
        }
        return super.onOptionsItemSelected(item);
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
				scoreValueTextView.setVisibility(View.INVISIBLE);
				scoreValueTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_short));
				hideHeaderTextView.setVisibility(View.VISIBLE);
				hideHeaderTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_short));
				hideSubTextView.setVisibility(View.VISIBLE);
				hideSubTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_short));
			}
			else
			{
				scoreHeaderTextView.setVisibility(View.VISIBLE);
				scoreHeaderTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_short));
				scoreValueTextView.setVisibility(View.VISIBLE);
				scoreValueTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_short));
				hideHeaderTextView.setVisibility(View.INVISIBLE);
				hideHeaderTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_short));
				hideSubTextView.setVisibility(View.INVISIBLE);
				hideSubTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_short));

			}
		}
	}
}