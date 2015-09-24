package com.sensorberg.sdk.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.resolver.Resolver;
import com.sensorberg.sdk.scanner.Scanner;

@SuppressWarnings("javadoc")
public class DemoActivity extends Activity
{
	private static final String EXTRA_ACTION = "com.sensorberg.demoActivity.extras.ACTION";

	private TextView textView;

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        textView = new TextView(this);
		StringBuilder infoText = new StringBuilder("This is an app that exposes some SDK APIs to the user");
		infoText.append('\n').append("API Key:").append(DemoApplication.API_KEY);
		infoText.append('\n').append("SDK Version:").append(BuildConfig.VERSION_NAME);
		infoText.append('\n').append("Bootstrapper Version:").append(com.sensorberg.sdk.bootstrapper.BuildConfig.VERSION_NAME);

		textView.setText(infoText.toString());
		setContentView(textView);
		((DemoApplication) getApplication()).setActivityContext(this);
		processIntent(getIntent());
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

	@Override
	protected void onNewIntent(Intent intent) {
		processIntent(intent);

		super.onNewIntent(intent);
	}

	private void processIntent(Intent intent) {
		if (intent != null) {
			Action action = intent.getParcelableExtra(EXTRA_ACTION);
			if (action != null) {
				DemoApplication application = (DemoApplication) getApplication();
				application.showAlert(action, null);
			}
		}
	}

	public static Intent getIntent(Context context, Action action) {
		Intent intent = new Intent(context, DemoActivity.class);
		intent.putExtra(EXTRA_ACTION, action);
		return intent;
	}
}
