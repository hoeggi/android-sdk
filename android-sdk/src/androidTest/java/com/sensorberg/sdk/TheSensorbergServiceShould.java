package com.sensorberg.sdk;

import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestPlatform;

import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;

import javax.inject.Inject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class TheSensorbergServiceShould extends AndroidTestCase {

    private static final String NEW_API_TOKEN = "SOME_NEW_API_TOKEN";
    private static final String DEFAULT_API_KEY = "DEFAULT_API_KEY";

    @Inject
    TestFileManager testFileManager;

    SensorbergService tested;
    private Intent CHANGE_API_KEY_MESSAGE;
    private TestPlatform testPlatform;
    private Context spyContext;

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
}
