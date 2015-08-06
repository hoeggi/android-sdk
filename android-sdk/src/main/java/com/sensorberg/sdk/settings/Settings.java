package com.sensorberg.sdk.settings;

import android.content.SharedPreferences;

import com.android.sensorbergVolley.VolleyError;
import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.Platform;
import com.sensorberg.sdk.internal.transport.SettingsCallback;

import org.json.JSONObject;

public class Settings implements SettingsCallback {

    public void setCallback(SettingsCallback callback) {
        this.callback = callback;
    }

    public interface SettingsCallback {
        SettingsCallback NONE = new SettingsCallback() {
            @Override
            public void onSettingsUpdateIntervalChange(Long updateIntervalMillies) {

            }

            @Override
            public void onSettingsBeaconLayoutUpdateIntervalChange(long newLayoutUpdateInterval) {

            }

            @Override
            public void onHistoryUploadIntervalChange(long newHistoryUploadInterval) {

            }
        };

        void onSettingsUpdateIntervalChange(Long updateIntervalMillies);

        void onSettingsBeaconLayoutUpdateIntervalChange(long newLayoutUpdateInterval);

        void onHistoryUploadIntervalChange(long newHistoryUploadInterval);
    }

    private static final boolean DEFAULT_SHOULD_RESTORE_BEACON_STATE = true;

    private static final long DEFAULT_LAYOUT_UPDATE_INTERVAL = Constants.Time.ONE_DAY;

    private static final long DEFAULT_HISTORY_UPLOAD_INTERVAL = 30 * Constants.Time.ONE_MINUTE;

    private static final long DEFAULT_SETTINGS_UPDATE_INTERVAL = Constants.Time.ONE_DAY;

    private static final long DEFAULT_EXIT_TIMEOUT_MILLIS = 9 * Constants.Time.ONE_SECOND;

    public static final long DEFAULT_FOREGROUND_SCAN_TIME = 10 * Constants.Time.ONE_SECOND;
    public static final long DEFAULT_FOREGROUND_WAIT_TIME = DEFAULT_FOREGROUND_SCAN_TIME;

    public static final long DEFAULT_BACKGROUND_WAIT_TIME = 2  * Constants.Time.ONE_MINUTE;
    public static final long DEFAULT_BACKGROUND_SCAN_TIME = 20 * Constants.Time.ONE_SECOND;

    public static final long DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT = Constants.Time.ONE_MINUTE;
    private static final long DEFAULT_MESSAGE_DELAY_WINDOW_LENGTH = Constants.Time.ONE_SECOND * 10;

    private static final long DEFAULT_MILLIS_BEETWEEN_RETRIES = 5 * Constants.Time.ONE_SECOND;

    private static final long DEFAULT_CACHE_TTL = 30 * Constants.Time.ONE_DAY;

    private long cacheTtl = DEFAULT_CACHE_TTL;

    private static final int DEFAULT_MAX_RETRIES = 3;

    private final Platform platform;
    private final SharedPreferences preferences;

    private long layoutUpdateInterval = DEFAULT_LAYOUT_UPDATE_INTERVAL;

    private long messageDelayWindowLength = DEFAULT_MESSAGE_DELAY_WINDOW_LENGTH;

    private long exitTimeoutMillis = DEFAULT_EXIT_TIMEOUT_MILLIS;

    private long foreGroundScanTime = DEFAULT_FOREGROUND_SCAN_TIME;
    private long foreGroundWaitTime = DEFAULT_FOREGROUND_WAIT_TIME;

    private long backgroundScanTime = DEFAULT_BACKGROUND_SCAN_TIME;
    private long backgroundWaitTime = DEFAULT_BACKGROUND_WAIT_TIME;

    private long millisBetweenRetries =  DEFAULT_MILLIS_BEETWEEN_RETRIES;
    private int maxRetries = DEFAULT_MAX_RETRIES;

    private long historyUploadInterval = DEFAULT_HISTORY_UPLOAD_INTERVAL;

    private long cleanBeaconMapRestartTimeout = DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT;
    private Long revision = null;
    private long settingsUpdateInterval = DEFAULT_SETTINGS_UPDATE_INTERVAL;

    private boolean shouldRestoreBeaconStates = DEFAULT_SHOULD_RESTORE_BEACON_STATE;

    private SettingsCallback callback = SettingsCallback.NONE;

    public Settings(Platform platform, SharedPreferences preferences){
        this.platform = platform;
        this.preferences = preferences;
    }

    public void restoreValuesFromPreferences(){
        if (preferences != null) {
            exitTimeoutMillis = preferences.getLong(Constants.SharedPreferencesKeys.Scanner.TIMEOUT_MILLIES, DEFAULT_EXIT_TIMEOUT_MILLIS);
            foreGroundScanTime = preferences.getLong(Constants.SharedPreferencesKeys.Scanner.FORE_GROUND_SCAN_TIME, DEFAULT_FOREGROUND_SCAN_TIME);
            foreGroundWaitTime = preferences.getLong(Constants.SharedPreferencesKeys.Scanner.FORE_GROUND_WAIT_TIME, DEFAULT_FOREGROUND_WAIT_TIME);
            backgroundScanTime = preferences.getLong(Constants.SharedPreferencesKeys.Scanner.BACKGROUND_SCAN_TIME, DEFAULT_BACKGROUND_SCAN_TIME);
            backgroundWaitTime = preferences.getLong(Constants.SharedPreferencesKeys.Scanner.BACKGROUND_WAIT_TIME, DEFAULT_BACKGROUND_WAIT_TIME);
            cleanBeaconMapRestartTimeout = preferences.getLong(Constants.SharedPreferencesKeys.Scanner.CLEAN_BEACON_MAP_RESTART_TIMEOUT, DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT);
            revision = preferences.getLong(Constants.SharedPreferencesKeys.Settings.REVISION, Long.MIN_VALUE);

            settingsUpdateInterval = preferences.getLong(Constants.SharedPreferencesKeys.Settings.UPDATE_INTERVAL, DEFAULT_SETTINGS_UPDATE_INTERVAL);

            maxRetries = preferences.getInt(Constants.SharedPreferencesKeys.Network.MAX_RESOLVE_RETRIES, DEFAULT_MAX_RETRIES);
            millisBetweenRetries =   preferences.getLong(Constants.SharedPreferencesKeys.Network.TIME_BETWEEN_RESOLVE_RETRIES, DEFAULT_MILLIS_BEETWEEN_RETRIES);

            historyUploadInterval = preferences.getLong(Constants.SharedPreferencesKeys.Network.HISTORY_UPLOAD_INTERVAL, DEFAULT_HISTORY_UPLOAD_INTERVAL);
            layoutUpdateInterval = preferences.getLong(Constants.SharedPreferencesKeys.Network.BEACON_LAYOUT_UPDATE_INTERVAL, DEFAULT_HISTORY_UPLOAD_INTERVAL);
            shouldRestoreBeaconStates = preferences.getBoolean(Constants.SharedPreferencesKeys.Scanner.SHOULD_RESTORE_BEACON_STATES, DEFAULT_SHOULD_RESTORE_BEACON_STATE);
            cacheTtl = preferences.getLong(Constants.SharedPreferencesKeys.Platform.CACHE_OBJECT_TIME_TO_LIVE, DEFAULT_CACHE_TTL);
        }
    }

    public void updateValues() {
        platform.getTransport().getSettings(this);
    }


    @Override
    public void nothingChanged() {
        //all is good nothing to do
        Logger.log.logSettingsUpdateState("nothingChanged");
    }

    @Override
    public void onFailure(VolleyError e) {
        Logger.log.logSettingsUpdateState("onFailure");
        Logger.log.logError("settings update failed", e);
    }

    @Override
    public void onSettingsFound(JSONObject settings) {
        Logger.log.logSettingsUpdateState("onSettingsFound: " + revision);

        if (settings == null) {
            settings = new JSONObject();
            preferences.edit().clear().apply();
        }

        exitTimeoutMillis = settings.optLong("scanner.exitTimeoutMillis", DEFAULT_EXIT_TIMEOUT_MILLIS);
        foreGroundScanTime = settings.optLong("scanner.foreGroundScanTime", DEFAULT_FOREGROUND_SCAN_TIME);
        foreGroundWaitTime = settings.optLong("scanner.foreGroundWaitTime", DEFAULT_FOREGROUND_WAIT_TIME);

        backgroundScanTime = settings.optLong("scanner.backgroundScanTime", DEFAULT_BACKGROUND_SCAN_TIME);
        backgroundWaitTime = settings.optLong("scanner.backgroundWaitTime", DEFAULT_BACKGROUND_WAIT_TIME);

        cleanBeaconMapRestartTimeout = settings.optLong("scanner.cleanBeaconMapRestartTimeout", DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT);

        messageDelayWindowLength = settings.optLong("presenter.messageDelayWindowLength", DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT);

        cacheTtl = settings.optLong("cache.objectTTL", DEFAULT_CACHE_TTL);

        maxRetries = settings.optInt("network.maximumResolveRetries", DEFAULT_MAX_RETRIES);
        millisBetweenRetries = settings.optLong("network.millisBetweenRetries", DEFAULT_MILLIS_BEETWEEN_RETRIES);
        shouldRestoreBeaconStates = settings.optBoolean("scanner.restoreBeaconStates", DEFAULT_SHOULD_RESTORE_BEACON_STATE);

        long newHistoryUploadIntervalMillis = settings.optLong("network.historyUploadInterval", DEFAULT_HISTORY_UPLOAD_INTERVAL);
        if (newHistoryUploadIntervalMillis != historyUploadInterval){
            historyUploadInterval = newHistoryUploadIntervalMillis;
            callback.onHistoryUploadIntervalChange(newHistoryUploadIntervalMillis);
        }

        long newLayoutUpdateInterval = settings.optLong("network.beaconLayoutUpdateInterval", DEFAULT_LAYOUT_UPDATE_INTERVAL);
        if (newLayoutUpdateInterval != layoutUpdateInterval){
            layoutUpdateInterval = newLayoutUpdateInterval;
            callback.onSettingsBeaconLayoutUpdateIntervalChange(newLayoutUpdateInterval);
        }

        final long newSettingsUpdateInterval = settings.optLong("settings.updateTime", DEFAULT_SETTINGS_UPDATE_INTERVAL);
        if (newSettingsUpdateInterval != settingsUpdateInterval){
            settingsUpdateInterval = newSettingsUpdateInterval;
            callback.onSettingsUpdateIntervalChange(newSettingsUpdateInterval);
        }

        persistToPreferecens();
    }

    public void historyUploadIntervalChanged(Long newHistoryUploadIntervalMillis) {
        if (newHistoryUploadIntervalMillis != historyUploadInterval){
            historyUploadInterval = newHistoryUploadIntervalMillis;
            callback.onHistoryUploadIntervalChange(newHistoryUploadIntervalMillis);
            persistToPreferecens();
        }
    }

    private void persistToPreferecens(){
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();

            if (revision != null) {
                editor.putLong(Constants.SharedPreferencesKeys.Settings.REVISION, revision);
            } else {
                editor.remove(Constants.SharedPreferencesKeys.Settings.REVISION);
            }

            editor.putLong(Constants.SharedPreferencesKeys.Scanner.TIMEOUT_MILLIES, exitTimeoutMillis);
            editor.putLong(Constants.SharedPreferencesKeys.Scanner.FORE_GROUND_SCAN_TIME, foreGroundScanTime);
            editor.putLong(Constants.SharedPreferencesKeys.Scanner.FORE_GROUND_WAIT_TIME, foreGroundWaitTime);
            editor.putLong(Constants.SharedPreferencesKeys.Scanner.BACKGROUND_SCAN_TIME, backgroundScanTime);
            editor.putLong(Constants.SharedPreferencesKeys.Scanner.BACKGROUND_WAIT_TIME, backgroundWaitTime);
            editor.putBoolean(Constants.SharedPreferencesKeys.Scanner.SHOULD_RESTORE_BEACON_STATES, shouldRestoreBeaconStates);

            editor.putLong(Constants.SharedPreferencesKeys.Settings.MESSAGE_DELAY_WINDOW_LENGTH, messageDelayWindowLength);
            editor.putLong(Constants.SharedPreferencesKeys.Settings.UPDATE_INTERVAL, settingsUpdateInterval);

            editor.putInt(Constants.SharedPreferencesKeys.Network.MAX_RESOLVE_RETRIES, maxRetries);
            editor.putLong(Constants.SharedPreferencesKeys.Network.TIME_BETWEEN_RESOLVE_RETRIES, millisBetweenRetries);
            editor.putLong(Constants.SharedPreferencesKeys.Network.HISTORY_UPLOAD_INTERVAL, historyUploadInterval);
            editor.putLong(Constants.SharedPreferencesKeys.Network.BEACON_LAYOUT_UPDATE_INTERVAL, layoutUpdateInterval);


            editor.apply();
        }
    }

    public long getLayoutUpdateInterval() {
        return layoutUpdateInterval;
    }

    public long getExitTimeout() {
        return exitTimeoutMillis;
    }

    public long getForeGroundScanTime() {
        return foreGroundScanTime;
    }

    public long getForeGroundWaitTime() {
        return foreGroundWaitTime;
    }

    public long getBackgroundScanTime() {
        return backgroundScanTime;
    }

    public long getBackgroundWaitTime() {
        return backgroundWaitTime;
    }

    public long getCleanBeaconMapRestartTimeout() {
        return cleanBeaconMapRestartTimeout;
    }

    public long getSettingsUpdateInterval() {
        return settingsUpdateInterval;
    }

    public long getMessageDelayWindowLength() {
        return messageDelayWindowLength;
    }

    public long getMillisBetweenRetries() {
        return millisBetweenRetries;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getHistoryUploadInterval() {
        return historyUploadInterval;
    }

    public boolean shouldRestoreBeaconStates() {
        return shouldRestoreBeaconStates;
    }

    public long getCacheTtl() {
        return cacheTtl;
    }

}
