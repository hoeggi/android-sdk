package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;

import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isExitEvent;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@RunWith(AndroidJUnit4.class)
public class TheScannerWithRestoredStateShould {

    @Inject
    TestFileManager testFileManager;

    private Settings settings;

    private TestPlatform platform;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        platform = new TestPlatform();
        platform = spy(platform);

        settings = new Settings(platform);
        platform.setSettings(settings);
    }

    @Test
    public void should_trigger_exits_if_the_scanner_was_idle_for_too_long() throws Exception {

        long startTime = settings.getCleanBeaconMapRestartTimeout() / 2;

        platform.clock.setNowInMillis(startTime);
        Scanner tested = new Scanner(settings, platform, true, platform.clock, testFileManager);
        ScannerListener listener = mock(ScannerListener.class);
        tested.addScannerListener(listener);
        tested.start();

        platform.clock.increaseTimeInMillis(settings.getExitTimeout() - 1);
        platform.clock.increaseTimeInMillis(1);
        platform.clock.increaseTimeInMillis(1);

        verify(listener, times(1)).onScanEventDetected(isExitEvent());
    }


    public void should_not_trigger_exits_if_the_scanner_was_idle_for_too_long() throws Exception {

        long startTime = settings.getCleanBeaconMapRestartTimeout() + 1;

        platform.clock.setNowInMillis(startTime);
        Scanner tested = new Scanner(settings, platform, true, platform.clock, testFileManager);
        ScannerListener listener = mock(ScannerListener.class);
        tested.addScannerListener(listener);
        tested.start();

        platform.clock.increaseTimeInMillis(settings.getExitTimeout() - 1);
        platform.clock.increaseTimeInMillis(1);
        platform.clock.increaseTimeInMillis(1);

        verifyNoMoreInteractions(listener);
    }

    @Test
    public void should_not_trigger_entry_if_beacon_was_seen_again_after_restart() throws Exception {

        long startTime = settings.getCleanBeaconMapRestartTimeout() - 1;

        platform.clock.setNowInMillis(startTime);
        Scanner tested = new Scanner(settings, platform, true, platform.clock, testFileManager);
        ScannerListener listener = mock(ScannerListener.class);
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
