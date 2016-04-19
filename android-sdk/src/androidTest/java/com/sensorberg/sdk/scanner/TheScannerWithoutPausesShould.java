package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestHandlerManager;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.mockito.Mockito;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import javax.inject.Inject;
import javax.inject.Named;

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

    @Inject
    TestFileManager testFileManager;

    @Inject
    TestServiceScheduler testServiceScheduler;

    @Inject
    @Named("testHandlerWithCustomClock")
    TestHandlerManager testHandlerManager;

    @Inject
    TestBluetoothPlatform bluetoothPlatform;

    @Inject
    SharedPreferences sharedPreferences;

    private Scanner tested;

    @Override
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        sharedPreferences.edit().clear().commit();
        this.testHandlerManager.getCustomClock().setNowInMillis(0);

        setUpScanner();

        tested.scanTime = Long.MAX_VALUE;
        tested.waitTime = 0;

        tested.start();
    }

    private void setUpScanner() {
        tested = new Scanner(new SettingsManager(new DumbSucessTransport(), sharedPreferences), false, testHandlerManager.getCustomClock(), testFileManager, testServiceScheduler,
                testHandlerManager, bluetoothPlatform);
    }

    public void test_scanner_detects_exit() {

        bluetoothPlatform.fakeIBeaconSighting(TestBluetoothPlatform.BYTES_FOR_SENSORBERG_BEACON_1);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        this.testHandlerManager.getCustomClock().setNowInMillis(Utils.EXIT_TIME_HAS_PASSED);

        verify(mockListener).onScanEventDetected(isExitEvent());
        verify(mockListener).onScanEventDetected(isNotEntryEvent());
        verify(mockListener).onScanEventDetected(hasBeaconId(TestBluetoothPlatform.EXPECTED_BEACON_1));
    }

    public void test_scanner_detects_no_exit() {

        bluetoothPlatform.fakeIBeaconSighting();

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        this.testHandlerManager.getCustomClock().setNowInMillis(Utils.EXIT_TIME_NOT_YET);

        verifyNoMoreInteractions(mockListener);
    }


    public void test_should_exit_later_if_beacon_was_seen_twice() {
        //first sighting
        bluetoothPlatform.fakeIBeaconSighting();
        testHandlerManager.getCustomClock().setNowInMillis(Utils.EXIT_TIME - 1);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        //second sighting, a little later
        bluetoothPlatform.fakeIBeaconSighting();

        verifyZeroInteractions(mockListener);

        //wait until ExitEventDelay has passed
        testHandlerManager.getCustomClock().setNowInMillis(testHandlerManager.getCustomClock().now() + Utils.EXIT_TIME + 1);

        //verify
        verify(mockListener).onScanEventDetected(isExitEvent());
    }

    public void test_scanner_verify_beaconID() {

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);
        bluetoothPlatform.fakeIBeaconSighting(TestBluetoothPlatform.BYTES_FOR_BEACON_1);

        verify(mockListener).onScanEventDetected(isEntryEvent());
        verify(mockListener).onScanEventDetected(isNotExitEvent());
        verify(mockListener).onScanEventDetected(hasBeaconId(TestBluetoothPlatform.EXPECTED_BEACON_1));
    }


}
