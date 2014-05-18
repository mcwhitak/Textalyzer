package com.whitaker.textalyzer.util;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class TextalyzerActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		TextView abTV = (TextView)findViewById(titleId);
		abTV.setTextColor(Color.WHITE);
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
}