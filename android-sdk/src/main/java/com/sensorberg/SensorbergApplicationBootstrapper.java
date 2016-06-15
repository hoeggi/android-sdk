package com.sensorberg;

import com.sensorberg.di.Component;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergServiceIntents;
import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Platform;
import com.sensorberg.sdk.receivers.ScannerBroadcastReceiver;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorbergorm.SugarContext;

import net.danlew.android.joda.JodaTimeAndroid;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;
import lombok.Setter;

public class SensorbergApplicationBootstrapper implements Platform.ForegroundStateListener {

    protected final Context context;

    protected boolean presentationDelegationEnabled;

    protected final Messenger messenger = new Messenger(new IncomingHandler());

    @Getter
    @Setter
    private static Component component;

    @Inject
    @Named("androidBluetoothPlatform")
    protected BluetoothPlatform bluetoothPlatform;

    class IncomingHandler extends Handler {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SensorbergServiceMessage.MSG_PRESENT_ACTION:
                    Bundle bundle = msg.getData();
                    bundle.setClassLoader(BeaconEvent.class.getClassLoader());
                    BeaconEvent beaconEvent = bundle.getParcelable(SensorbergServiceMessage.MSG_PRESENT_ACTION_BEACONEVENT);
                    presentBeaconEvent(beaconEvent);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public SensorbergApplicationBootstrapper(Context context, boolean enablePresentationDelegation, String apiKey) {
        this.context = context;
        this.presentationDelegationEnabled = enablePresentationDelegation;
        setComponent(buildComponentAndInject(context));
        getComponent().inject(this);

        SugarContext.init(context);
        JodaTimeAndroid.init(context);

        activateService(apiKey);
    }

    private void activateService(String apiKey) {
        if (bluetoothPlatform.isBluetoothLowEnergySupported()) {
            context.startService(SensorbergServiceIntents.getStartServiceIntent(context, apiKey));
        }
    }

    public void presentBeaconEvent(BeaconEvent beaconEvent) {
        //TODO instead of overriding this, it should be a listener that is called
        //something like presenterListener that would then be also used for notifications in android 6
    }

    public void setResolverBaseURL(URL resolverBaseURL) {
        context.startService(SensorbergServiceIntents.getResolverEndpointIntent(context, resolverBaseURL));
    }

    public void setPresentationDelegationEnabled(boolean value) {
        //TODO should use listener and registration
        presentationDelegationEnabled = value;
        if (value) {
            registerForPresentationDelegation();
        } else {
            unRegisterFromPresentationDelegation();
        }
    }

    public void disableServiceCompletely(Context context) {
        //TODO should be renamed to disableService to correspond to enableService?
        context.startService(SensorbergServiceIntents.getShutdownServiceIntent(context));
    }

    public void enableService(Context context, String apiKey) {
        //TODO do we need this? It's not used anywhere
        ScannerBroadcastReceiver.setManifestReceiverEnabled(true, context);
        activateService(apiKey);
        hostApplicationInForeground();
    }

    public void hostApplicationInBackground() {
        Logger.log.applicationStateChanged("hostApplicationInBackground");
        context.startService(SensorbergServiceIntents.getAppInBackgroundIntent(context));
        unRegisterFromPresentationDelegation();
    }

    public void hostApplicationInForeground() {
        context.startService(SensorbergServiceIntents.getAppInForegroundIntent(context));
        if (presentationDelegationEnabled) {
            registerForPresentationDelegation();
        }
    }

    protected void unRegisterFromPresentationDelegation() {
        context.startService(SensorbergServiceIntents.getIntentWithReplyToMessenger(context,
                SensorbergServiceMessage.MSG_UNREGISTER_PRESENTATION_DELEGATE, messenger));
    }

    protected void registerForPresentationDelegation() {
        context.startService(SensorbergServiceIntents.getIntentWithReplyToMessenger(context,
                SensorbergServiceMessage.MSG_REGISTER_PRESENTATION_DELEGATE, messenger));
    }

    public void changeAPIToken(String newApiToken) {
        context.startService(SensorbergServiceIntents.getApiTokenIntent(context, newApiToken));
    }

    public void setAdvertisingIdentifier(String advertisingIdentifier) {
        Intent service = SensorbergServiceIntents.getAdvertisingIdentifierIntent(context, advertisingIdentifier);
        context.startService(service);
    }

    public void setLogging(boolean enableLogging) {
        context.startService(SensorbergServiceIntents.getServiceLoggingIntent(context, enableLogging));
    }

    public Component buildComponentAndInject(Context context) {
        return Component.Initializer.init((Application) context.getApplicationContext());
    }
}
