package com.whitaker.textalyzer;

import com.whitaker.textalyzer.ContactHolder.InstructionHolder;
import com.whitaker.textalyzer.TextMessage.Directions;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DetailActivity extends Activity
{
	private TextView scoreHeaderTextView;
	private TextView scoreValueTextView;
	private ListView informationListView;
	private ContactHolder contactHolder;
	
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
		informationListView.setAdapter(new InformationAdapter());
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
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle your other action bar items...
        switch(item.getItemId())
        {
        	case android.R.id.home:
        		super.onBackPressed();
        		break;	
        }
        return super.onOptionsItemSelected(item);
    }
}