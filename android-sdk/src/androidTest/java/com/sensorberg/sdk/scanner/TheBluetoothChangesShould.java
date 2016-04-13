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

import util.Utils;

import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isEntryEvent;
import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isExitEvent;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class TheBluetoothChangesShould {

    @Inject
    TestFileManager testFileManager;

    Scanner tested;
    private TestPlatform platform;
    private long RANDOM_VALUE_THAT_IS_SHORTER_THAN_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT_BUT_LONGER_THAN_EXIT_EVENT_DELAY = Utils.THIRTY_SECONDS;
    private Settings settings;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        platform = new TestPlatform();

        settings = new Settings(platform);
        tested = new Scanner(settings, platform, false, platform.clock, testFileManager);
        tested.scanTime = Long.MAX_VALUE;
        tested.waitTime = 0L;
        tested.start();
        platform.clock.setNowInMillis(0);
    }

    @Test
    public void assert_random_values_are_within_range() {
        assertThat(RANDOM_VALUE_THAT_IS_SHORTER_THAN_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT_BUT_LONGER_THAN_EXIT_EVENT_DELAY).isLessThan(Settings.DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT);
        assertThat(RANDOM_VALUE_THAT_IS_SHORTER_THAN_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT_BUT_LONGER_THAN_EXIT_EVENT_DELAY).isGreaterThan(settings.getExitTimeout());
    }

    @Test
    public void still_sees_exit_events_when_bluetooth_is_restarted_in_a_short_interval() {
        ScannerListener mockScannerListener = mock(ScannerListener.class);
        tested.addScannerListener(mockScannerListener);
        platform.fakeIBeaconSighting();

        verify(mockScannerListener).onScanEventDetected(isEntryEvent());

        tested.stop();
        reset(mockScannerListener);
        platform.clock.increaseTimeInMillis(RANDOM_VALUE_THAT_IS_SHORTER_THAN_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT_BUT_LONGER_THAN_EXIT_EVENT_DELAY);
        tested.start();

        verify(mockScannerListener, never()).onScanEventDetected(isEntryEvent());
        verify(mockScannerListener, never()).onScanEventDetected(isExitEvent());

        long start = platform.clock.now();
        while (platform.clock.now() < start + Utils.EXIT_TIME) {
            platform.clock.increaseTimeInMillis(Utils.ONE_ADVERTISEMENT_INTERVAL);
        }
        verify(mockScannerListener, never()).onScanEventDetected(isExitEvent());

        platform.clock.increaseTimeInMillis(1);
        verify(mockScannerListener).onScanEventDetected(isExitEvent());

        verify(mockScannerListener, never()).onScanEventDetected(isEntryEvent());
    }


    public void beacon_events_are_removed_when_bluetooth_is_restarted_after_a_long_break_interval() {
        ScannerListener mockScannerListener = mock(ScannerListener.class);
        tested.addScannerListener(mockScannerListener);
        platform.fakeIBeaconSighting();

        verify(mockScannerListener).onScanEventDetected(isEntryEvent());

        tested.stop();
        reset(mockScannerListener);
        platform.clock.increaseTimeInMillis(Utils.VERY_LONG_TIME);
        tested.start();

        verify(mockScannerListener, never()).onScanEventDetected(isEntryEvent());
        verify(mockScannerListener, never()).onScanEventDetected(isExitEvent());

        long start = platform.clock.now();
        while (platform.clock.now() < start + Utils.EXIT_TIME * 2) {
            platform.clock.increaseTimeInMillis(Utils.ONE_ADVERTISEMENT_INTERVAL);
        }
        verify(mockScannerListener, never()).onScanEventDetected(isEntryEvent());
        verify(mockScannerListener, never()).onScanEventDetected(isExitEvent());

        while (platform.clock.now() < start + Utils.ONE_HOUR) {
            platform.clock.increaseTimeInMillis(Utils.ONE_ADVERTISEMENT_INTERVAL);
        }
        verify(mockScannerListener, never()).onScanEventDetected(isEntryEvent());
        verify(mockScannerListener, never()).onScanEventDetected(isExitEvent());

    }
}

