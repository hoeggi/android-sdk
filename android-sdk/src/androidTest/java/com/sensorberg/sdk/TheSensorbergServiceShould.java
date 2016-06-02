package com.sensorberg.sdk;

import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.FileManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class TheSensorbergServiceShould {

    private static final String NEW_API_TOKEN = "SOME_NEW_API_TOKEN";

    private static final String DEFAULT_API_KEY = "DEFAULT_API_KEY";

    @Inject
    FileManager fileManager;

    SensorbergService tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        tested = new SensorbergService();
        tested.onCreate();
        fileManager = spy(fileManager);
        tested.fileManager = fileManager;

        Intent startIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        startIntent.putExtra(SensorbergService.EXTRA_API_KEY, DEFAULT_API_KEY);

        tested.onStartCommand(startIntent, -1, -1);
    }

    @Test
    public void should_persist_the_settings_when_getting_a_new_API_token() throws Exception {
        Intent changeApiKeyMessageIntent = new Intent();
        changeApiKeyMessageIntent.putExtra(SensorbergService.MSG_SET_API_TOKEN_TOKEN, NEW_API_TOKEN);
        changeApiKeyMessageIntent.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_SET_API_TOKEN);

        //TODO check if this is really optimal, to have persistence called twice
        tested.onStartCommand(changeApiKeyMessageIntent, -1, -1);
        verify(fileManager, times(2)).write(any(ServiceConfiguration.class), any(String.class));
    }
}
