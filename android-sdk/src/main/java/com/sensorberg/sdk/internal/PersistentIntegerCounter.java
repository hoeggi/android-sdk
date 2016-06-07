package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.Logger;

import android.content.SharedPreferences;

/**
 * a class that represents a long value that is restored from @{SharedPreferences}. It is thread safe.
 */
public class PersistentIntegerCounter {

    private SharedPreferences settingsSharedPrefs;

    private int postToServiceCounter;

    private final Object postToServiceCounterMonitor = new Object();

    public PersistentIntegerCounter(SharedPreferences prefs) {
        settingsSharedPrefs = prefs;

        if (settingsSharedPrefs.contains(Constants.SharedPreferencesKeys.Platform.POST_TO_SERVICE_COUNTER)) {
            try {
                postToServiceCounter = settingsSharedPrefs.getInt(Constants.SharedPreferencesKeys.Platform.POST_TO_SERVICE_COUNTER, 0);
            } catch (Exception e) {
                Logger.log.logError("Could not fetch the last postToServiceCounter because of some weird Framework bug", e);
                postToServiceCounter = 0;
            }
        } else {
            postToServiceCounter = 0;
        }
    }

    /**
     * get the next value, +1 bigger than the last.
     *
     * @return the next value, unique, thread safe unique
     */
    public int next() {
        synchronized (postToServiceCounterMonitor) {
            if (postToServiceCounter == Integer.MAX_VALUE) {
                postToServiceCounter = 0;
            } else {
                postToServiceCounter++;
            }

            settingsSharedPrefs.edit()
                    .putInt(Constants.SharedPreferencesKeys.Platform.POST_TO_SERVICE_COUNTER, postToServiceCounter)
                    .apply();
            return postToServiceCounter;
        }
    }
}