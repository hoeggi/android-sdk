package com.sensorberg.sdk.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.model.realm.RealmScan;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.sensorberg.support.LatestBeacons;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

import static com.sensorberg.utils.ListUtils.distinct;
import static com.sensorberg.utils.ListUtils.map;

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
        textView.setHeight(300);
		StringBuilder infoText = new StringBuilder("This is an app that exposes some SDK APIs to the user");
		infoText.append('\n').append("API Key:").append(DemoApplication.API_KEY);
		infoText.append('\n').append("SDK Version:").append(BuildConfig.VERSION_NAME);
		infoText.append('\n').append("Bootstrapper Version:").append(com.sensorberg.sdk.bootstrapper.BuildConfig.VERSION_NAME);

		textView.setText(infoText.toString());

        Button button = new Button(this);
        button.setText("click me");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask task = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        {
                            long before = System.currentTimeMillis();
                            Collection<BeaconId> beacons = LatestBeacons.getLatestBeacons(getApplicationContext(),
                                    5, TimeUnit.MINUTES);
                            StringBuilder beaconIds = new StringBuilder("got these from the other process: ");
                            for (BeaconId beacon : beacons) {
                                beaconIds.append(beacon.getBid()).append(",");
                            }
                            beaconIds.append(" beacons");
                            beaconIds.append("took ").append(System.currentTimeMillis() - before).append("ms");
                            Logger.log.verbose(beaconIds.toString());
                        }
                        {
                            long before = System.currentTimeMillis();
                            Collection<BeaconId> beacons = getLatestBeaconsInMyProcess(getApplicationContext(),
                                    5, TimeUnit.MINUTES);
                            StringBuilder beaconIds = new StringBuilder("got these in my process: ");
                            for (BeaconId beacon : beacons) {
                                beaconIds.append(beacon.getBid()).append(",");
                            }
                            beaconIds.append(" beacons");
                            beaconIds.append("took ").append(System.currentTimeMillis() - before).append("ms");
                            Logger.log.verbose(beaconIds.toString());
                        }

                        return null;
                    }
                };
                task.execute();
            }
        });

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setHorizontalGravity(LinearLayout.VERTICAL);
        linearLayout.addView(textView);
//        linearLayout.addView(button);

        setContentView(button);

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

    /**
     * this method is only here for a speed reference.
     */
    @Deprecated
    public static Collection<BeaconId> getLatestBeaconsInMyProcess(Context context,long duration, TimeUnit unit){
        long now = System.currentTimeMillis() - unit.toMillis(duration);
        Realm realm = Realm.getInstance(context, BeaconActionHistoryPublisher.REALM_FILENAME);
        return  distinct(map(
                RealmScan.latestEnterEvents(now, realm),
                BeaconId.FROM_REALM_SCAN));
    }
}
