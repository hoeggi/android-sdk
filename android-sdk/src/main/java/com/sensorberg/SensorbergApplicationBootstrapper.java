package com.sensorberg;

import com.sensorberg.di.Component;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.receivers.ScannerBroadcastReceiver;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Platform;
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
            Intent service = new Intent(context, SensorbergService.class);
            service.putExtra(SensorbergServiceMessage.EXTRA_START_SERVICE, 1);
            if (apiKey != null) {
                service.putExtra(SensorbergServiceMessage.EXTRA_API_KEY, apiKey);
            }
            context.startService(service);
        }
    }

    public void presentBeaconEvent(BeaconEvent beaconEvent) {
        //TODO instead of overriding this, it should be a listener that is called
        //something like presenterListener that would then be also used for notifications in android 6
    }

    public void setResolverBaseURL(URL resolverBaseURL) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_TYPE_SET_RESOLVER_ENDPOINT);
        if (resolverBaseURL != null) {
            service.putExtra(SensorbergServiceMessage.MSG_SET_RESOLVER_ENDPOINT_ENDPOINT_URL, resolverBaseURL);
        }
        context.startService(service);
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
        sendEmptyMessage(SensorbergServiceMessage.MSG_SHUTDOWN);
    }

    public void enableService(Context context, String apiKey) {
        //TODO do we need this? It's not used anywhere
        ScannerBroadcastReceiver.setManifestReceiverEnabled(true, context);
        activateService(apiKey);
        hostApplicationInForeground();
    }

    public void hostApplicationInBackground() {
        Logger.log.applicationStateChanged("hostApplicationInBackground");
        sendEmptyMessage(SensorbergServiceMessage.MSG_APPLICATION_IN_BACKGROUND);
        unRegisterFromPresentationDelegation();
    }

    public void hostApplicationInForeground() {
        sendEmptyMessage(SensorbergServiceMessage.MSG_APPLICATION_IN_FOREGROUND);
        if (presentationDelegationEnabled) {
            registerForPresentationDelegation();
        }
    }

    protected void sendEmptyMessage(int messageType) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, messageType);
        context.startService(service);
    }

    protected void unRegisterFromPresentationDelegation() {
        sendReplyToMessage(SensorbergServiceMessage.MSG_UNREGISTER_PRESENTATION_DELEGATE);
    }

    protected void sendReplyToMessage(int messageType) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, messageType);
        service.putExtra(SensorbergServiceMessage.EXTRA_MESSENGER, messenger);
        context.startService(service);
    }

    protected void registerForPresentationDelegation() {
        sendReplyToMessage(SensorbergServiceMessage.MSG_REGISTER_PRESENTATION_DELEGATE);
    }

    public void changeAPIToken(String newApiToken) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SET_API_TOKEN);
        service.putExtra(SensorbergServiceMessage.MSG_SET_API_TOKEN_TOKEN, newApiToken);
        context.startService(service);
    }

    public void setAdvertisingIdentifier(String advertisingIdentifier) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER);
        service.putExtra(SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER_ADVERTISING_IDENTIFIER, advertisingIdentifier);
        context.startService(service);
    }

    public void setLogging(boolean enableLogging) {
        Intent service = new Intent(context, SensorbergService.class);
        int message = enableLogging ? SensorbergServiceMessage.MSG_TYPE_ENABLE_LOGGING : SensorbergServiceMessage.MSG_TYPE_DISABLE_LOGGING;
        service.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, message);
        context.startService(service);
    }

    public Component buildComponentAndInject(Context context) {
        return Component.Initializer.init((Application) context.getApplicationContext());
    }
}
