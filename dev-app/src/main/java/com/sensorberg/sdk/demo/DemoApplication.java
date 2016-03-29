package com.sensorberg.sdk.demo;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.sensorbergorm.SugarContext;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.action.ActionType;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.action.UriMessageAction;
import com.sensorberg.sdk.action.VisitWebsiteAction;
import com.sensorberg.sdk.bootstrapper.BackgroundDetector;
import com.sensorberg.sdk.bootstrapper.SensorbergApplicationBootstrapper;
import com.sensorberg.sdk.testApp.BuildConfig;
import com.sensorberg.sdk.resolver.BeaconEvent;

@SuppressWarnings("javadoc")
public class DemoApplication extends Application
{
    private static final String TAG = "DemoApplication";

    public static final String API_KEY = "e33f35cae664e7ae50250f6f62296762936eb84200a2aa4522f0b22599959cbe";

    private SensorbergApplicationBootstrapper boot;
    private BackgroundDetector detector;

    //show all internal logging in debug mode
    static {
        if (BuildConfig.DEBUG) {
            Logger.enableVerboseLogging();
        }
    }

    private Context activityContext;

    @Override
	public void onCreate() {
		super.onCreate();
        SugarContext.init(this);
        Log.d(TAG, "onCreate application");

        boot = new SensorbergApplicationBootstrapper(this, true){
            @Override
            public void presentBeaconEvent(BeaconEvent beaconEvent) {
                Action action = beaconEvent.getAction();
                showAlert(action, beaconEvent.trigger);
            }
        };
        boot.activateService(API_KEY);
        boot.hostApplicationInForeground();

        detector = new BackgroundDetector(boot);
        registerActivityLifecycleCallbacks(detector);
	}

    public void showAlert(Action action, Integer trigger) {
        String payload = action.getPayload();
        ActionType type = action.getType();

        switch (type) {
            case MESSAGE_URI:
                UriMessageAction uriMessageAction = (UriMessageAction) action;
                showAlert(uriMessageAction.getUuid().hashCode(), uriMessageAction.getTitle(), uriMessageAction.getContent(), Uri.parse(uriMessageAction.getUri()), payload, trigger, type, action);
                break;
            case MESSAGE_WEBSITE:
                VisitWebsiteAction visitWebsiteAction = (VisitWebsiteAction) action;
                showAlert(visitWebsiteAction.getUuid().hashCode(), visitWebsiteAction.getSubject(), visitWebsiteAction.getBody(), visitWebsiteAction.getUri(), payload, trigger, type, action);
                break;
            case MESSAGE_IN_APP:
                InAppAction inAppAction = (InAppAction) action;
                showAlert(inAppAction.getUuid().hashCode(), inAppAction.getSubject(), inAppAction.getBody(), inAppAction.getUri(), payload, trigger, type, action);
                break;
        }
    }

    private void showAlert(int hashCode, String subject, String body, final Uri uri, final String payload, Integer trigger, ActionType type, Action action) {
        if (activityContext == null){
            MyActionPresenter.showNotification(getApplicationContext(), hashCode, subject, body, uri, action);
            return;
        }
        String triggerString;
        if (trigger == null){
            triggerString = "unknown";
        }  else if (trigger == 1){
            triggerString = "enter";
        } else if (trigger == 2){
            triggerString = "exit";
        } else {
            triggerString = "enter/exit";
        }

        StringBuilder bodyText = new StringBuilder(body);
        bodyText.append('\n').append("trigger: ").append(triggerString);
        bodyText.append('\n').append("type:").append(type);
        bodyText.append('\n').append("payload:").append(payload != null ? "attached" : "not attached" );
        bodyText.append('\n').append("delay:").append(action.getDelayTime() );
        bodyText.append('\n').append("uuid:").append(action.getUuid());
        bodyText.append('\n').append("url:").append(uri);


        AlertDialog.Builder dialog = new AlertDialog.Builder(activityContext)
                .setTitle(subject)
                .setMessage(bodyText.toString())
                .setPositiveButton("open url", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "open URL " + uri.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        if (payload != null) {
            dialog.setNeutralButton("Payload", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder payloadDialog = new AlertDialog.Builder(activityContext)
                            .setTitle("Payload:")
                            .setMessage(payload)
                            .setNeutralButton(android.R.string.ok, null);
                    payloadDialog.show();
                }
            });
        }
        dialog.show();
    }

    public void setActivityContext(Context activityContext) {
        this.activityContext = activityContext;
    }
}
