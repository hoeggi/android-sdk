package com.sensorberg.sdk.settings;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.fest.assertions.api.Assertions;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

public class TheSettingsShould extends AndroidTestCase {

    @Inject
    @Named("realClock")
    Clock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    SettingsManager tested;
    SettingsManager untouched;
    private SharedPreferences testedSharedPreferences;
    private SharedPreferences untouchedSharedPreferences;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        Transport transport = new OkHttpClientTransport(new TestPlatform().getCachedVolleyQueue(), clock, testPlatformIdentifier, true);
        transport.setApiToken(TestConstants.API_TOKEN);
        testedSharedPreferences = getContext().getSharedPreferences(Long.toString(System.currentTimeMillis()), Context.MODE_PRIVATE);
        tested = new SettingsManager(transport, testedSharedPreferences);

        untouchedSharedPreferences = getContext().getSharedPreferences(Long.toString(System.currentTimeMillis()), Context.MODE_PRIVATE);
        untouched = new SettingsManager(transport, untouchedSharedPreferences);
    }

    public void test_initial_values_should_be_identical() throws Exception {
        Assertions.assertThat(untouched.getBackgroundScanTime()).isEqualTo(tested.getBackgroundScanTime());
        Assertions.assertThat(untouched.getBackgroundWaitTime()).isEqualTo(tested.getBackgroundWaitTime());
        Assertions.assertThat(untouched.getExitTimeoutMillis()).isEqualTo(tested.getExitTimeoutMillis());
        Assertions.assertThat(untouched.getForeGroundScanTime()).isEqualTo(tested.getForeGroundScanTime());
        Assertions.assertThat(untouched.getForeGroundWaitTime()).isEqualTo(tested.getForeGroundWaitTime());
    }

    public void test_fetch_values_from_the_network() throws Exception {
        tested.updateSettingsFromNetwork();

        Assertions.assertThat(untouched.getBackgroundScanTime()).isNotEqualTo(tested.getBackgroundScanTime());
        Assertions.assertThat(untouched.getBackgroundWaitTime()).isNotEqualTo(tested.getBackgroundWaitTime());
        Assertions.assertThat(untouched.getExitTimeoutMillis()).isNotEqualTo(tested.getExitTimeoutMillis());
        Assertions.assertThat(untouched.getForeGroundScanTime()).isNotEqualTo(tested.getForeGroundScanTime());
        Assertions.assertThat(untouched.getForeGroundWaitTime()).isNotEqualTo(tested.getForeGroundWaitTime());
    }

    public void test_update_the_default_values_if_the_constants_change() throws Exception {
        //prepare the shared preferences
        SharedPreferences.Editor editor = testedSharedPreferences.edit();
        editor.putLong(Constants.SharedPreferencesKeys.Scanner.BACKGROUND_WAIT_TIME, Constants.Time.ONE_MINUTE * 6);
        editor.commit();

        //load the last values from the shared preferences, as it happens after a restart

        Settings settingsFromPrefs = new Settings(testedSharedPreferences);
        Assertions.assertThat(settingsFromPrefs.getBackgroundWaitTime()).isEqualTo(Constants.Time.ONE_MINUTE * 6);

        //simulating a settings request without content
        Settings settingsFromEmptyJson = new Settings(new JSONObject(), SettingsUpdateCallback.NONE);
        Assertions.assertThat(settingsFromEmptyJson.getBackgroundWaitTime()).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
    }
}
