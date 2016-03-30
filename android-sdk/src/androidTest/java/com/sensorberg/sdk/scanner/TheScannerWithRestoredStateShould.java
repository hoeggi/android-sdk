package com.sensorberg.sdk.scanner;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestPlatform;


import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isExitEvent;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@RunWith(AndroidJUnit4.class)
public class TheScannerWithRestoredStateShould {

    Scanner tested;
    private Settings settings;
    private TestPlatform platform;
    private ScannerListener listener;

    @Before
    public void setUp() throws Exception {
        platform = new TestPlatform().setContext(InstrumentationRegistry.getContext());
        platform  = spy(platform);
        File testFile = platform.getFile("enteredBeaconsCache");
        when(platform.getFile("enteredBeaconsCache")).thenReturn(testFile);


        settings = new Settings(platform, platform.getSettingsSharedPrefs());
        platform.setSettings(settings);

        listener = mock(ScannerListener.class);

        platform.clock.setNowInMillis(1000);

        tested = new Scanner(settings, platform, true);

        tested.start();
        platform.fakeIBeaconSighting();
        tested.stop();
    }

    @Test
    public void should_trigger_exits_if_the_scanner_was_idle_for_too_long() throws Exception {

        long startTime =  settings.getCleanBeaconMapRestartTimeout() / 2 ;

        platform.clock.setNowInMillis(startTime);
        tested = new Scanner(settings, platform, true);
        tested.addScannerListener(listener);
        tested.start();

        platform.clock.increaseTimeInMillis(settings.getExitTimeout() -1);
        platform.clock.increaseTimeInMillis(1);
        platform.clock.increaseTimeInMillis(1);

        verify(listener, times(1)).onScanEventDetected(isExitEvent());
   }

    public void should_not_trigger_exits_if_the_scanner_was_idle_for_too_long() throws Exception {

        long startTime =  settings.getCleanBeaconMapRestartTimeout() + 1;

        platform.clock.setNowInMillis(startTime);
        tested = new Scanner(settings, platform, true);
        tested.addScannerListener(listener);
        tested.start();

        platform.clock.increaseTimeInMillis(settings.getExitTimeout() - 1);
        platform.clock.increaseTimeInMillis(1);
        platform.clock.increaseTimeInMillis(1);

        verifyNoMoreInteractions(listener);
    }

    @Test
    public void should_not_trigger_entry_if_beacon_was_seen_again_after_restart() throws Exception {

        long startTime =  settings.getCleanBeaconMapRestartTimeout() - 1 ;

        platform.clock.setNowInMillis(startTime);
        tested = new Scanner(settings, platform, true);
        tested.addScannerListener(listener);
        tested.start();

        platform.clock.increaseTimeInMillis(settings.getExitTimeout() - 1);
        platform.fakeIBeaconSighting();
        platform.clock.increaseTimeInMillis(1);
        platform.fakeIBeaconSighting();
        platform.clock.increaseTimeInMillis(1);
        platform.fakeIBeaconSighting();

        verifyNoMoreInteractions(listener);
    }
}
