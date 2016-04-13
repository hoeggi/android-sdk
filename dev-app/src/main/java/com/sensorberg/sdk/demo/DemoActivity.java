package com.sensorberg.sdk.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.internal.AndroidPlatform;

import java.io.IOException;

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
		StringBuilder infoText = new StringBuilder("This is an app that exposes some SDK APIs to the user").append('\n');
		infoText.append('\n').append("API Key: ").append(DemoApplication.API_KEY);
		infoText.append('\n').append("SDK Version: ").append(BuildConfig.VERSION_NAME);
		infoText.append('\n').append("Bootstrapper Version: ").append(com.sensorberg.sdk.bootstrapper.BuildConfig.VERSION_NAME);
		infoText.append('\n').append("Installation ID: ").append(new AndroidPlatform(getApplicationContext()).getDeviceInstallationIdentifier());

		textView.setText(infoText.toString());
		setContentView(textView);
		((DemoApplication) getApplication()).setActivityContext(this);
		processIntent(getIntent());

		AsyncTask<String, Integer, Pair<String, Long>> task = new AsyncTask<String, Integer, Pair<String, Long>>() {
			@Override
			protected Pair<String, Long> doInBackground(String... params) {
				long timeBefore = System.currentTimeMillis();
				String advertiserIdentifier = "not-found";
				try {
                    advertiserIdentifier = "google:" + AdvertisingIdClient.getAdvertisingIdInfo(DemoActivity.this).getId();
			} catch (IOException e) {
				Logger.log.logError("foreground could not fetch the advertising identifier because of an IO Exception" , e);
			} catch (GooglePlayServicesNotAvailableException e) {
				Logger.log.logError("foreground play services not available", e);
			} catch (GooglePlayServicesRepairableException e) {
				Logger.log.logError("foreground  services need repairing", e);
			} catch (Exception e){
				Logger.log.logError("foreground could not fetch the advertising identifier because of an unknown error" , e);
			}
				long timeItTook = System.currentTimeMillis() - timeBefore;
				Logger.log.verbose("foreground fetching the advertising identifier took " + timeItTook + " millis");
				return Pair.create(advertiserIdentifier, timeItTook);
			}

			@Override
			protected void onPostExecute(Pair<String, Long> o) {
				textView.append("\nGoogle Advertising ID: " + o.first);
				textView.append("\nGoogle ID took: " + o.second + " milliseconds");
			}
		};

		task.execute();
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
