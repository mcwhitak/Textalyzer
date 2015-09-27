package com.whitaker.textalyzer;

import com.whitaker_iacob.textalyzer.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.content.Intent;
import com.whitaker.textalyzer.OpenSourceActivity;


public class AboutActivity extends Activity implements OnClickListener
{
	private Button openSource;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		openSource = (Button)findViewById(R.id.open_source);
		openSource.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == openSource) {
			Intent intent = new Intent(this, OpenSourceActivity.class);
			startActivity(intent);
		}
	}
}
