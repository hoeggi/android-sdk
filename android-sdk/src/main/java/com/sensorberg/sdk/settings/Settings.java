package com.sensorberg.sdk.settings;

import com.sensorberg.sdk.Constants;

import org.json.JSONObject;

import android.content.SharedPreferences;

import lombok.Getter;
import lombok.Setter;

public class Settings {

    @Getter
    private long cacheTtl = DefaultSettings.DEFAULT_CACHE_TTL;

    @Getter
    private long layoutUpdateInterval = DefaultSettings.DEFAULT_LAYOUT_UPDATE_INTERVAL;

    @Getter
    private long messageDelayWindowLength = DefaultSettings.DEFAULT_MESSAGE_DELAY_WINDOW_LENGTH;

    @Getter
    private long exitTimeoutMillis = DefaultSettings.DEFAULT_EXIT_TIMEOUT_MILLIS;

    @Getter
    private long foreGroundScanTime = DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME;

    @Getter
    private long foreGroundWaitTime = DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME;

    @Getter
    private long backgroundScanTime = DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME;

    @Getter
    private long backgroundWaitTime = DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME;

    @Getter
    private long millisBetweenRetries = DefaultSettings.DEFAULT_MILLIS_BEETWEEN_RETRIES;

    @Getter
    private int maxRetries = DefaultSettings.DEFAULT_MAX_RETRIES;

    @Getter
    @Setter
    private long historyUploadInterval = DefaultSettings.DEFAULT_HISTORY_UPLOAD_INTERVAL;

    @Getter
    private long cleanBeaconMapRestartTimeout = DefaultSettings.DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT;

    @Getter
    private long settingsUpdateInterval = DefaultSettings.DEFAULT_SETTINGS_UPDATE_INTERVAL;

    @Getter
    private boolean shouldRestoreBeaconStates = DefaultSettings.DEFAULT_SHOULD_RESTORE_BEACON_STATE;

    private Long revision = null;

    public Settings() {
    }

    public Settings(SharedPreferences preferences) {
        if (preferences != null) {
            exitTimeoutMillis = preferences
                    .getLong(Constants.SharedPreferencesKeys.Scanner.TIMEOUT_MILLIES, DefaultSettings.DEFAULT_EXIT_TIMEOUT_MILLIS);
            foreGroundScanTime = preferences
                    .getLong(Constants.SharedPreferencesKeys.Scanner.FORE_GROUND_SCAN_TIME, DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
            foreGroundWaitTime = preferences
                    .getLong(Constants.SharedPreferencesKeys.Scanner.FORE_GROUND_WAIT_TIME, DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
            backgroundScanTime = preferences
                    .getLong(Constants.SharedPreferencesKeys.Scanner.BACKGROUND_SCAN_TIME, DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
            backgroundWaitTime = preferences
                    .getLong(Constants.SharedPreferencesKeys.Scanner.BACKGROUND_WAIT_TIME, DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
            cleanBeaconMapRestartTimeout = preferences.getLong(Constants.SharedPreferencesKeys.Scanner.CLEAN_BEACON_MAP_RESTART_TIMEOUT,
                    DefaultSettings.DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT);
            revision = preferences.getLong(Constants.SharedPreferencesKeys.Settings.REVISION, Long.MIN_VALUE);

            settingsUpdateInterval = preferences
                    .getLong(Constants.SharedPreferencesKeys.Settings.UPDATE_INTERVAL, DefaultSettings.DEFAULT_SETTINGS_UPDATE_INTERVAL);

            maxRetries = preferences.getInt(Constants.SharedPreferencesKeys.Network.MAX_RESOLVE_RETRIES, DefaultSettings.DEFAULT_MAX_RETRIES);
            millisBetweenRetries = preferences
                    .getLong(Constants.SharedPreferencesKeys.Network.TIME_BETWEEN_RESOLVE_RETRIES, DefaultSettings.DEFAULT_MILLIS_BEETWEEN_RETRIES);

            historyUploadInterval = preferences
                    .getLong(Constants.SharedPreferencesKeys.Network.HISTORY_UPLOAD_INTERVAL, DefaultSettings.DEFAULT_HISTORY_UPLOAD_INTERVAL);
            layoutUpdateInterval = preferences
                    .getLong(Constants.SharedPreferencesKeys.Network.BEACON_LAYOUT_UPDATE_INTERVAL, DefaultSettings.DEFAULT_HISTORY_UPLOAD_INTERVAL);
            shouldRestoreBeaconStates = preferences.getBoolean(Constants.SharedPreferencesKeys.Scanner.SHOULD_RESTORE_BEACON_STATES,
                    DefaultSettings.DEFAULT_SHOULD_RESTORE_BEACON_STATE);
            cacheTtl = preferences.getLong(Constants.SharedPreferencesKeys.Platform.CACHE_OBJECT_TIME_TO_LIVE, DefaultSettings.DEFAULT_CACHE_TTL);
        }
    }

    public Settings(JSONObject settings, SettingsUpdateCallback settingsUpdateCallback) {
        exitTimeoutMillis = settings.optLong("scanner.exitTimeoutMillis", DefaultSettings.DEFAULT_EXIT_TIMEOUT_MILLIS);
        foreGroundScanTime = settings.optLong("scanner.foreGroundScanTime", DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
        foreGroundWaitTime = settings.optLong("scanner.foreGroundWaitTime", DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
        backgroundScanTime = settings.optLong("scanner.backgroundScanTime", DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
        backgroundWaitTime = settings.optLong("scanner.backgroundWaitTime", DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);

        cleanBeaconMapRestartTimeout = settings
                .optLong("scanner.cleanBeaconMapRestartTimeout", DefaultSettings.DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT);

        messageDelayWindowLength = settings.optLong("presenter.messageDelayWindowLength", DefaultSettings.DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT);
        cacheTtl = settings.optLong("cache.objectTTL", DefaultSettings.DEFAULT_CACHE_TTL);
        maxRetries = settings.optInt("network.maximumResolveRetries", DefaultSettings.DEFAULT_MAX_RETRIES);
        millisBetweenRetries = settings.optLong("network.millisBetweenRetries", DefaultSettings.DEFAULT_MILLIS_BEETWEEN_RETRIES);
        shouldRestoreBeaconStates = settings.optBoolean("scanner.restoreBeaconStates", DefaultSettings.DEFAULT_SHOULD_RESTORE_BEACON_STATE);

        long newHistoryUploadIntervalMillis = settings.optLong("network.historyUploadInterval", DefaultSettings.DEFAULT_HISTORY_UPLOAD_INTERVAL);
        if (newHistoryUploadIntervalMillis != historyUploadInterval) {
            historyUploadInterval = newHistoryUploadIntervalMillis;
            settingsUpdateCallback.onHistoryUploadIntervalChange(newHistoryUploadIntervalMillis);
        }

        long newLayoutUpdateInterval = settings.optLong("network.beaconLayoutUpdateInterval", DefaultSettings.DEFAULT_LAYOUT_UPDATE_INTERVAL);
        if (newLayoutUpdateInterval != layoutUpdateInterval) {
            layoutUpdateInterval = newLayoutUpdateInterval;
            settingsUpdateCallback.onSettingsBeaconLayoutUpdateIntervalChange(newLayoutUpdateInterval);
        }

        final long newSettingsUpdateInterval = settings.optLong("settings.updateTime", DefaultSettings.DEFAULT_SETTINGS_UPDATE_INTERVAL);
        if (newSettingsUpdateInterval != settingsUpdateInterval) {
            settingsUpdateInterval = newSettingsUpdateInterval;
            settingsUpdateCallback.onSettingsUpdateIntervalChange(newSettingsUpdateInterval);
        }

    }

    public void persistToPreferences(SharedPreferences preferences) {
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
}
