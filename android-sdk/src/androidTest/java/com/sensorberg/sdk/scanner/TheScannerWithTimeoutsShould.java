package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestPlatform;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.mockito.Mockito;

import android.test.AndroidTestCase;

import javax.inject.Inject;

import util.Utils;

import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isExitEvent;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TheScannerWithTimeoutsShould extends AndroidTestCase {

    @Inject
    TestFileManager testFileManager;

    @Inject
    TestServiceScheduler testServiceScheduler;

    private TestPlatform plattform;

    private UIScanner tested;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        plattform = new TestPlatform();
        plattform.clock.setNowInMillis(0);
        tested = new UIScanner(new Settings(plattform), plattform, plattform.clock, testFileManager, testServiceScheduler, plattform);

        tested.start();
    }


    public void test_scanner_waits_to_the_edge_of_second_pause() {
        this.plattform.clock.setNowInMillis(0);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);

        long earliestBeaconSighting = Utils.EXIT_TIME + tested.scanTime;
        long beaconSighting = earliestBeaconSighting + (tested.scanTime - Utils.EXIT_TIME); //Exactly to the edge of a scan
        long shouldNotSeeBeaconExitUntil = beaconSighting + Utils.EXIT_TIME + tested.waitTime;
        long shouldSeeExitEvent = shouldNotSeeBeaconExitUntil + tested.waitTime;

        while (plattform.clock.now() < Utils.ONE_MINUTE * 2) {
            if (plattform.clock.now() == beaconSighting) {
                plattform.fakeIBeaconSighting();
                tested.addScannerListener(mockListener);
            }

            if (plattform.clock.now() < shouldNotSeeBeaconExitUntil) {
                verifyNoMoreInteractions(mockListener);
            }
            if (plattform.clock.now() > shouldSeeExitEvent) {
                verify(mockListener).onScanEventDetected(isExitEvent());
            }

            plattform.clock.increaseTimeInMillis(Utils.ONE_SECOND);
        }
    }

    public void test_scanner_waits_one_pause() {
        this.plattform.clock.setNowInMillis(0);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);

        long earliestBeaconSighting = Utils.EXIT_TIME + tested.scanTime;
        long beaconSighting = earliestBeaconSighting + (tested.scanTime / 2); //Somewhere in the middle of a Scan
        long shouldNotSeeBeaconExitUntil = beaconSighting + Utils.EXIT_TIME + tested.waitTime;
        long shouldSeeExitEvent = shouldNotSeeBeaconExitUntil + tested.waitTime;

        while (plattform.clock.now() < Utils.ONE_MINUTE * 2) {
            if (plattform.clock.now() == beaconSighting) {
                plattform.fakeIBeaconSighting();
                tested.addScannerListener(mockListener);
            }

            if (plattform.clock.now() < shouldNotSeeBeaconExitUntil) {
                verifyNoMoreInteractions(mockListener);
            }
            if (plattform.clock.now() > shouldSeeExitEvent) {
                verify(mockListener).onScanEventDetected(isExitEvent());
            }

            plattform.clock.increaseTimeInMillis(Utils.ONE_SECOND);
        }
    }
}
