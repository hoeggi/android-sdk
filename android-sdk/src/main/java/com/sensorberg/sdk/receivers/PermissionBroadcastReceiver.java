package com.sensorberg.sdk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.internal.PermissionChecker;

import javax.inject.Inject;

/**
 * @author skraynick
 * @date 16-06-13
 */
public class PermissionBroadcastReceiver extends BroadcastReceiver {

    @Inject
    PermissionChecker permissionChecker;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(SensorbergServiceMessage.EXTRA_LOCATION_PERMISSION)) {
            final int flagType = intent.getExtras().getInt("type");
            switch (flagType) {
                case SensorbergServiceMessage.MSG_LOCATION_SET:
                    dontShowPermission(context);
                    break;
                case SensorbergServiceMessage.MSG_LOCATION_NOT_SET_WHEN_NEEDED:
                    showPermission(context);
                    break;
            }
        }
    }

    /**
     * Sends a flag for showing the permission.
     *
     * @param context - Context object.
     */
    private void showPermission(Context context) {
        Intent service = new Intent(context, SensorbergServiceMessage.class);
        service.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_LOCATION_SERVICES_IS_SET);
        service.putExtra(SensorbergServiceMessage.EXTRA_LOCATION_PERMISSION, true);
        context.startService(service);
    }

    /**
     * Sends a flag for not showing the permission dialog.
     *
      * @param context - Context object.
     */
    private void dontShowPermission(Context context) {
        Intent service = new Intent(context, SensorbergServiceMessage.class);
        service.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_LOCATION_SERVICES_IS_SET);
        service.putExtra(SensorbergServiceMessage.EXTRA_LOCATION_PERMISSION, false);
        context.startService(service);
    }
}
