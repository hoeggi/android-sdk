package com.sensorberg.sdk;

import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestPlatform;

import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;

import javax.inject.Inject;
import javax.inject.Named;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TheSensorbergServiceShould extends AndroidTestCase {

    private static final String NEW_API_TOKEN = "SOME_NEW_API_TOKEN";
    private static final String DEFAULT_API_KEY = "DEFAULT_API_KEY";

    @Inject
    TestFileManager testFileManager;

    private static final String DEFAULT_AD_ID = "DEFAULT_AD_ID";

    private static final String NEW_AD_ID = "NEW_AD_ID";

    SensorbergService tested;

    private Intent CHANGE_API_KEY_MESSAGE;

    private Intent CHANGE_ADVERTISING_ID_MESSAGE;

    private TestPlatform testPlatform;

    private Context spyContext;

    @Inject
    @Named("androidPlatformIdentifier")
    PlatformIdentifier platformIdentifier;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testPlatform = spy(new TestPlatform());
        {
            CHANGE_API_KEY_MESSAGE = new Intent();
            CHANGE_API_KEY_MESSAGE.putExtra(SensorbergService.MSG_SET_API_TOKEN_TOKEN, NEW_API_TOKEN);
            CHANGE_API_KEY_MESSAGE.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_SET_API_TOKEN);
        }
        {
            CHANGE_ADVERTISING_ID_MESSAGE = new Intent();
            CHANGE_ADVERTISING_ID_MESSAGE.putExtra(SensorbergService.MSG_SET_API_ADVERTISING_IDENTIFIER_ADVERTISING_IDENTIFIER, NEW_AD_ID);
            CHANGE_ADVERTISING_ID_MESSAGE.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_SET_API_ADVERTISING_IDENTIFIER);
        }

        tested = new SensorbergService();
        tested.bluetoothPlatform = new TestBluetoothPlatform();
        tested.platform = testPlatform;

        Intent startIntent = new Intent(getContext(), SensorbergService.class);
        startIntent.putExtra(SensorbergService.EXTRA_API_KEY, DEFAULT_API_KEY);

        tested.onStartCommand(startIntent, -1, -1);
        reset(testPlatform);
    }

    public void should_persist_the_settings_when_getting_a_new_API_token() throws Exception {
        tested.onStartCommand(CHANGE_API_KEY_MESSAGE, -1, -1);
        verify(testFileManager).write(any(ServiceConfiguration.class), any(String.class));
    }

    public void should_persist_the_settings_when_getting_a_new_advertising_id() throws Exception {
        PlatformIdentifier spiedPlatformIdentifier = spy(platformIdentifier);
        tested.onStartCommand(CHANGE_ADVERTISING_ID_MESSAGE, -1, -1);
        verify(spiedPlatformIdentifier, times(1)).setAdvertisingIdentifier(anyString());
    }
}
