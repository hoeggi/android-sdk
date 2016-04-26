package com.sensorberg.sdk;

import com.sensorberg.sdk.testUtils.TestPlatform;

import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TheSensorbergServiceShould extends AndroidTestCase {

    private static final String NEW_API_TOKEN = "SOME_NEW_API_TOKEN";
    private static final String DEFAULT_API_KEY = "DEFAULT_API_KEY";

    private static final String DEFAULT_AD_ID = "DEFAULT_AD_ID";

    private static final String NEW_AD_ID = "NEW_AD_ID";

    SensorbergService tested;
    private Intent CHANGE_API_KEY_MESSAGE;

    private Intent CHANGE_ADVERTISING_ID_MESSAGE;
    private TestPlatform testPlatform;
    private Context spyContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        testPlatform = spy(new TestPlatform());
        testPlatform.setContext(getContext());
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

        tested.platform = testPlatform;

        Intent startIntent = new Intent(getContext(), SensorbergService.class);
        startIntent.putExtra(SensorbergService.EXTRA_API_KEY, DEFAULT_API_KEY);

        tested.onStartCommand(startIntent, -1, -1);
        reset(testPlatform);
    }

    public void should_persist_the_settings_when_getting_a_new_API_token() throws Exception {
        tested.onStartCommand(CHANGE_API_KEY_MESSAGE, -1, -1);
        verify(testPlatform).write(any(ServiceConfiguration.class), any(String.class));
    }

    public void should_persist_the_settings_when_getting_a_new_advertising_id() throws Exception {
        tested.onStartCommand(CHANGE_ADVERTISING_ID_MESSAGE, -1, -1);
        verify(testPlatform.getTransport(), times(1)).setAdvertisingIdentifier(anyString());
    }
}
