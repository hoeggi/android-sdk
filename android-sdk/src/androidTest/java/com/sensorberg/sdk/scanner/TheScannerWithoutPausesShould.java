package com.sensorberg.sdk.scanner;

import android.test.AndroidTestCase;

import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.mockito.Mockito;

import util.Utils;

import static com.sensorberg.sdk.testUtils.SensorbergMatcher.hasBeaconId;
import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isEntryEvent;
import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isExitEvent;
import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isNotEntryEvent;
import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isNotExitEvent;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;


public class TheScannerWithoutPausesShould extends AndroidTestCase {

    private TestPlatform plattform = null;
    private Scanner tested;

    @Override
    public void setUp() throws Exception {
        plattform = new TestPlatform();
        this.plattform.clock.setNowInMillis(0);

        setUpScanner();

        tested.scanTime = Long.MAX_VALUE;
        tested.waitTime = 0;

        tested.start();
    }

    private void setUpScanner() {
        tested = new Scanner(new Settings(plattform), plattform, false);
    }

    public void test_scanner_detects_exit() {

        plattform.fakeIBeaconSighting(TestPlatform.BYTES_FOR_SENSORBERG_BEACON_1);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        this.plattform.clock.setNowInMillis(Utils.EXIT_TIME_HAS_PASSED);

        verify(mockListener).onScanEventDetected(isExitEvent());
        verify(mockListener).onScanEventDetected(isNotEntryEvent());
        verify(mockListener).onScanEventDetected(hasBeaconId(TestPlatform.EXPECTED_BEACON_1));
    }

    public void test_scanner_detects_no_exit() {

        plattform.fakeIBeaconSighting();

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        this.plattform.clock.setNowInMillis(Utils.EXIT_TIME_NOT_YET);

        verifyNoMoreInteractions(mockListener);
    }


    public void test_should_exit_later_if_beacon_was_seen_twice() {
        //first sighting
        plattform.fakeIBeaconSighting();
        plattform.clock.setNowInMillis(Utils.EXIT_TIME - 1);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        //second sighting, a little later
        plattform.fakeIBeaconSighting();

        verifyZeroInteractions(mockListener);

        //wait until ExitEventDelay has passed
        plattform.clock.setNowInMillis(plattform.clock.now() + Utils.EXIT_TIME + 1);

        //verify
        verify(mockListener).onScanEventDetected(isExitEvent());
    }

    public void test_scanner_verify_beaconID() {

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);
        plattform.fakeIBeaconSighting(TestPlatform.BYTES_FOR_BEACON_1);

        verify(mockListener).onScanEventDetected(isEntryEvent());
        verify(mockListener).onScanEventDetected(isNotExitEvent());
        verify(mockListener).onScanEventDetected(hasBeaconId(TestPlatform.EXPECTED_BEACON_1));
    }


}
