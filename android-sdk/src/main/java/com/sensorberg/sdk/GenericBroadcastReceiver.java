package com.sensorberg.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sensorberg.sdk.background.SensorbergBroadcastReceiver;

public class GenericBroadcastReceiver extends SensorbergBroadcastReceiver{

    public static void setManifestReceiverEnabled(boolean enabled, Context context) {
        SensorbergBroadcastReceiver.setManifestReceiverEnabled(enabled, context, GenericBroadcastReceiver.class);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtras(intent.getExtras());
        context.startService(service);
    }

    @SuppressWarnings("ConstantConditions")
    private String toString(Intent intent) {
        StringBuilder builder = new StringBuilder("action:" + intent.getAction());
        Bundle extras = intent.getExtras();
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            builder.append("\nextra key:\"").append(key).append("\" value:\"").append(value).append("\" of type: ").append(value.getClass().getName());
        }
        return builder.toString();
    }
}
