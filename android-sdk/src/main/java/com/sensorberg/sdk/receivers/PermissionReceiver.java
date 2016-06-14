package com.sensorberg.sdk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.internal.PermissionChecker;

import javax.inject.Inject;

/**
 * @author skraynick
 * @date 16-06-13
 */
public class PermissionReceiver extends BroadcastReceiver {

    @Inject
    PermissionChecker permissionChecker;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && permissionChecker.hasLocationPermission()) {
                showPermission(context);
            } else {
                dontShowPermission(context);
            }
        }
    }

    /**
     * Sends a flag for showing the permission.
     *
     * @param context - Context object.
     */
    private void showPermission(Context context) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_LOCATION_SERVICES_IS_SET);
        service.putExtra(SensorbergService.EXTRA_LOCATION_PERMISSION, true);
        context.startService(service);
    }


    /**
     * Sends a flag for not showing the permission dialog.
     *
      * @param context - Context object.
     */
    private void dontShowPermission(Context context) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_LOCATION_SERVICES_IS_SET);
        service.putExtra(SensorbergService.EXTRA_LOCATION_PERMISSION, false);
        context.startService(service);
    }
}
