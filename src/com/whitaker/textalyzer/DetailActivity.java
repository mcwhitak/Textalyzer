package com.whitaker.textalyzer;

import com.whitaker.textalyzer.MainActivity.ContactHolder;
import com.whitaker.textalyzer.TextMessage.Directions;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends Activity
{
	private TextView scoreHeaderTextView;
	private TextView scoreValueTextView;
	private TextView contactNameTextView;
	private TextView titleText;
	private TextView subText;
	private TextView thirdText;
	private TextView fourthText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.detail_activity);
		
		Bundle b = getIntent().getExtras();
		Integer id = b.getInt("id");
		grabAllViews();
		
		ContactHolder holder = MainActivity.getContactHolder(id);
		
		Integer outgoing = 0;
		Integer incoming = 0;
		for(int i=0; i<holder.textMessages.size(); i++)
		{
			if(holder.textMessages.get(i).direction == Directions.INBOUND)
			{
				incoming++;
			}
			else
			{
				outgoing++;
			}
		}
		contactNameTextView.setText(holder.personName);
		titleText.setText("Phone: " + holder.phoneNumber);
		subText.setText("ID: " + holder.personId);
		
		thirdText.setText("Outgoing: " + outgoing);
		fourthText.setText("Incoming: " + incoming);
	}
	
	private void grabAllViews()
	{
		scoreHeaderTextView = (TextView)findViewById(R.id.score_header);
		scoreValueTextView = (TextView)findViewById(R.id.score_value);
		contactNameTextView = (TextView)findViewById(R.id.contact_name);
		titleText = (TextView)findViewById(R.id.title_text);
		subText = (TextView)findViewById(R.id.subtitle_text);
		thirdText = (TextView)findViewById(R.id.third_text);
		fourthText = (TextView)findViewById(R.id.fourth_text);
	}
}