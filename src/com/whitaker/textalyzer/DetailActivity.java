package com.whitaker.textalyzer;

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
		String name = b.getString("name");
		
		grabAllViews();
		
		titleText.setText(name);
	}
	
	private void grabAllViews()
	{
		titleText = (TextView)findViewById(R.id.title_text);
		subText = (TextView)findViewById(R.id.subtitle_text);
	}
}