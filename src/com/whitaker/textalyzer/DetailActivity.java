package com.whitaker.textalyzer;

import com.whitaker.textalyzer.ContactHolder.InstructionHolder;
import com.whitaker.textalyzer.TextMessage.Directions;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DetailActivity extends Activity implements OnItemClickListener
{
	private TextView scoreHeaderTextView;
	private TextView scoreValueTextView;
	private ListView informationListView;
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
	}
	
	private void grabAllViews()
	{
		scoreHeaderTextView = (TextView)findViewById(R.id.score_header);
		scoreValueTextView = (TextView)findViewById(R.id.score_value);
		informationListView = (ListView)findViewById(R.id.list_information);
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
			
			if(position < contactHolder.instructions.size())
			{
				InstructionHolder iHolder = contactHolder.instructions.get(position);
				
				catText.setText(iHolder.instruction);
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
}