package com.sensorberg.sdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.sensorberg.sdk.resolver.Resolver;
import com.sensorberg.sdk.scanner.Scanner;

@SuppressWarnings("javadoc")
public class DemoActivity extends Activity
{
	private TextView textView;

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        textView = new TextView(this);
        textView.setText("this is a very basic example...");
		setContentView(textView);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		((DemoApplication) getApplication()).setActivityContext(this);
	}

	@Override
	protected void onPause() {
		((DemoApplication) getApplication()).setActivityContext(null);
		super.onPause();
	}
}
