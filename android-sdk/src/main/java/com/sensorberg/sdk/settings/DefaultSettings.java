package com.sensorberg.sdk.settings;

import com.sensorberg.sdk.Constants;

public class DefaultSettings {

    static final boolean DEFAULT_SHOULD_RESTORE_BEACON_STATE = true;

    static final long DEFAULT_LAYOUT_UPDATE_INTERVAL = Constants.Time.ONE_DAY;

    static final long DEFAULT_HISTORY_UPLOAD_INTERVAL = 30 * Constants.Time.ONE_MINUTE;

    static final long DEFAULT_SETTINGS_UPDATE_INTERVAL = Constants.Time.ONE_DAY;

    static final long DEFAULT_EXIT_TIMEOUT_MILLIS = 9 * Constants.Time.ONE_SECOND;

    public static final long DEFAULT_FOREGROUND_SCAN_TIME = 10 * Constants.Time.ONE_SECOND;

    public static final long DEFAULT_FOREGROUND_WAIT_TIME = DEFAULT_FOREGROUND_SCAN_TIME;

    public static final long DEFAULT_BACKGROUND_WAIT_TIME = 2 * Constants.Time.ONE_MINUTE;

    public static final long DEFAULT_BACKGROUND_SCAN_TIME = 20 * Constants.Time.ONE_SECOND;

    public static final long DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT = Constants.Time.ONE_MINUTE;

    public static final long DEFAULT_MESSAGE_DELAY_WINDOW_LENGTH = Constants.Time.ONE_SECOND * 10;

    static final long DEFAULT_MILLIS_BEETWEEN_RETRIES = 5 * Constants.Time.ONE_SECOND;

    static final long DEFAULT_CACHE_TTL = 30 * Constants.Time.ONE_DAY;

    static final int DEFAULT_MAX_RETRIES = 3;

}
