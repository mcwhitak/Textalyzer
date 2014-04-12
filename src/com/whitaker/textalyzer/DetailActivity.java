package com.whitaker.textalyzer;

import com.whitaker.textalyzer.MainActivity.ContactHolder;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends Activity
{
	private TextView titleText;
	private TextView subText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.detail_activity);
		
		Bundle b = getIntent().getExtras();
		Integer id = b.getInt("id");
		grabAllViews();
		
		ContactHolder holder = MainActivity.getContactHolder(id);
		titleText.setText(holder.personName);
		subText.setText(holder.phoneNumber);
	}
	
	private void grabAllViews()
	{
		titleText = (TextView)findViewById(R.id.title_text);
		subText = (TextView)findViewById(R.id.subtitle_text);
	}
}