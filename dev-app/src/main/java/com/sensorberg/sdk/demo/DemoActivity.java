package com.sensorberg.sdk.demo;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.testApp.BuildConfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("javadoc")
public class DemoActivity extends Activity {

    private static final String EXTRA_ACTION = "com.sensorberg.demoActivity.extras.ACTION";

    public static final UUID BEACON_PROXIMITY_ID = UUID.fromString("192E463C-9B8E-4590-A23F-D32007299EF5");

    private SugarAction tested;

    private Clock clock;

    private UUID uuid = UUID.fromString("6133172D-935F-437F-B932-A901265C24B0");

    private SugarScan testScan;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //app = (SugarApp)getApplication();
        tested = SugarAction.from(beaconEvent, clock);
        //tested.save();

        ScanEvent scanevent = new ScanEvent.Builder()
                .withEventMask(ScanEventType.ENTRY.getMask())
                .withBeaconId(new BeaconId(BEACON_PROXIMITY_ID, 1337, 1337))
                .withEventTime(100)
                .build();
        testScan = SugarScan.from(scanevent, 0);
        testScan.save();

        List<SugarScan> scans = SugarScan.listAll(SugarScan.class);
        //List<SugarAction> list = SugarAction.listAll(SugarAction.class);
        List<SugarScan> list2 = SugarScan.notSentScans();

        textView = new TextView(this);
        StringBuilder infoText = new StringBuilder("This is an app that exposes some SDK APIs to the user").append('\n');
        infoText.append('\n').append("sentToServerTimestamp2: ").append(list2.get(0).getSentToServerTimestamp2());
        infoText.append('\n').append("API Key: ").append(DemoApplication.API_KEY);
        infoText.append('\n').append("SDK Version: ").append(com.sensorberg.sdk.BuildConfig.VERSION_NAME);
        infoText.append('\n').append("Demo Version: ").append(BuildConfig.VERSION_NAME);

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
                    Logger.log.logError("foreground could not fetch the advertising identifier because of an IO Exception", e);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Logger.log.logError("foreground play services not available", e);
                } catch (GooglePlayServicesRepairableException e) {
                    Logger.log.logError("foreground  services need repairing", e);
                } catch (Exception e) {
                    Logger.log.logError("foreground could not fetch the advertising identifier because of an unknown error", e);
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
    protected void onResume() {
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
