package com.sensorberg.sdk.settings;

import com.google.gson.Gson;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;

import junit.framework.Assert;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class TheSettingsShould {

    @Inject
    @Named("realClock")
    Clock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    @Inject
    Gson gson;

    @Inject
    @Named("realRetrofitApiService")
    RetrofitApiServiceImpl realRetrofitApiService;

    SettingsManager tested;

    SettingsManager untouched;

    private SharedPreferences testedSharedPreferences;

    private SharedPreferences untouchedSharedPreferences;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        Transport transport = new RetrofitApiTransport(realRetrofitApiService, clock);
        transport.setApiToken(TestConstants.API_TOKEN);
        testedSharedPreferences = InstrumentationRegistry.getContext()
                .getSharedPreferences(Long.toString(System.currentTimeMillis()), Context.MODE_PRIVATE);
        tested = new SettingsManager(transport, testedSharedPreferences);

        untouchedSharedPreferences = InstrumentationRegistry.getContext()
                .getSharedPreferences(Long.toString(System.currentTimeMillis()), Context.MODE_PRIVATE);
        untouched = new SettingsManager(transport, untouchedSharedPreferences);
    }

    @Test
    public void test_initial_values_should_be_identical() throws Exception {
        Assertions.assertThat(untouched.getBackgroundScanTime()).isEqualTo(tested.getBackgroundScanTime());
        Assertions.assertThat(untouched.getBackgroundWaitTime()).isEqualTo(tested.getBackgroundWaitTime());
        Assertions.assertThat(untouched.getExitTimeoutMillis()).isEqualTo(tested.getExitTimeoutMillis());
        Assertions.assertThat(untouched.getForeGroundScanTime()).isEqualTo(tested.getForeGroundScanTime());
        Assertions.assertThat(untouched.getForeGroundWaitTime()).isEqualTo(tested.getForeGroundWaitTime());
    }

    @Test
    public void test_fetch_values_from_the_network() throws Exception {
        tested.updateSettingsFromNetwork();

        Assertions.assertThat(untouched.getBackgroundScanTime()).isNotEqualTo(tested.getBackgroundScanTime());
        Assertions.assertThat(untouched.getBackgroundWaitTime()).isNotEqualTo(tested.getBackgroundWaitTime());
        Assertions.assertThat(untouched.getExitTimeoutMillis()).isNotEqualTo(tested.getExitTimeoutMillis());
        Assertions.assertThat(untouched.getForeGroundScanTime()).isNotEqualTo(tested.getForeGroundScanTime());
        Assertions.assertThat(untouched.getForeGroundWaitTime()).isNotEqualTo(tested.getForeGroundWaitTime());
    }

    @Test
    public void test_update_the_default_values_if_the_constants_change() throws Exception {
        //prepare the shared preferences
        SharedPreferences.Editor editor = testedSharedPreferences.edit();
        editor.putLong(Constants.SharedPreferencesKeys.Scanner.BACKGROUND_WAIT_TIME, Constants.Time.ONE_MINUTE * 6);
        editor.commit();

        //load the last values from the shared preferences, as it happens after a restart

        Settings settingsFromPrefs = new Settings(testedSharedPreferences);
        Assertions.assertThat(settingsFromPrefs.getBackgroundWaitTime()).isEqualTo(Constants.Time.ONE_MINUTE * 6);

        //simulating a settings request without content
        Settings settingsFromEmptyJson = new Settings(null, SettingsUpdateCallback.NONE);
        Assertions.assertThat(settingsFromEmptyJson.getBackgroundWaitTime()).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
    }

    public void test_advertising_id_gets_persisted() throws Exception {
        //prepare the shared preferences
        Assert.fail();
//        Assertions.assertThat(untouched.getAdvertisingIdentifier()).isNull();
//        Assertions.assertThat(tested.getAdvertisingIdentifier()).isEqualTo(untouched.getAdvertisingIdentifier());
//        Assertions.assertThat(testedSharedPreferences.getString(Constants.SharedPreferencesKeys.Network.ADVERTISING_IDENTIFIER, "")).isEmpty();
//
//        tested.setAdvertisingIdentifier("TEST_ID");
//        Assertions.assertThat(testedSharedPreferences.getString(Constants.SharedPreferencesKeys.Network.ADVERTISING_IDENTIFIER, "")).isEqualTo(
//                "TEST_ID");
//
//        //load the last values from the shared preferences, as it happens after a restart
//        tested.restoreValuesFromPreferences();
//        Assertions.assertThat(tested.getAdvertisingIdentifier()).isEqualTo("TEST_ID");
//
//        //simulating a settings request without content
//        tested.onSettingsFound(new JSONObject());
//        Assertions.assertThat(tested.getAdvertisingIdentifier()).isEqualTo("TEST_ID");
    }
}
