package com.sensorberg.sdk.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarFields;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.Resolver;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.scanner.Scanner;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("javadoc")
public class DemoActivity extends Activity
{
	private static final String EXTRA_ACTION = "com.sensorberg.demoActivity.extras.ACTION";
    public static final UUID BEACON_PROXIMITY_ID = UUID.fromString("192E463C-9B8E-4590-A23F-D32007299EF5");
    private SugarAction tested;
    private TextView textView;
    private Clock clock;
    private UUID uuid = UUID.fromString("6133172D-935F-437F-B932-A901265C24B0");
	private SugarScan testScan;



    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        BeaconEvent beaconEvent = new BeaconEvent.Builder()
                .withAction(new InAppAction(uuid, null, null, null, null, 0))
                .withPresentationTime(1337)
                .withTrigger(ScanEventType.ENTRY.getMask())
                .build();
        beaconEvent.setBeaconId(new BeaconId(BEACON_PROXIMITY_ID, 1337, 1337));
        clock = new Clock() {
            @Override
            public long now() {
                return 0;
            }

            @Override
            public long elapsedRealtime() {
                return 0;
            }
        };
        tested = SugarAction.from(beaconEvent, clock);
        tested.save();

		ScanEvent scanevent = new ScanEvent.Builder()
				.withEventMask(ScanEventType.ENTRY.getMask())
				.withBeaconId(new BeaconId(BEACON_PROXIMITY_ID, 1337, 1337))
				.withEventTime(100)
				.build();
		testScan = SugarScan.from(scanevent, 0);
		testScan.save();

		List<SugarScan> scans = SugarScan.listAll(SugarScan.class);
 		List<SugarAction> list = SugarAction.listAll(SugarAction.class);
        SugarAction sugar = SugarAction.findById(SugarAction.class, 1);
        List<SugarAction> list2 = SugarAction.notSentScans();


        textView = new TextView(this);
		StringBuilder infoText = new StringBuilder("This is an app that exposes some SDK APIs to the user");
        infoText.append('\n').append("TESTed").append(HeadersJsonObjectRequest.gson.toJson(tested));
        infoText.append('\n').append("Action ID: ").append(list.get(0).getActionId());
        infoText.append('\n').append("sentToServerTimestamp2: ").append(list2.get(0).getSentToServerTimestamp2());
		infoText.append('\n').append("API Key:").append(DemoApplication.API_KEY);
		infoText.append('\n').append("SDK Version:").append(BuildConfig.VERSION_NAME);
		infoText.append('\n').append("Bootstrapper Version:").append(com.sensorberg.sdk.bootstrapper.BuildConfig.VERSION_NAME);
		infoText.append('\n').append("Scan proof it works!: ").append(scans.get(0).getSentToServerTimestamp2());

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
