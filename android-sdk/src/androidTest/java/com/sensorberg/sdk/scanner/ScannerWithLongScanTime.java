package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestPlatform;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import android.test.AndroidTestCase;

import javax.inject.Inject;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScannerWithLongScanTime extends AndroidTestCase {

    @Inject
    TestFileManager testFileManager;

    @Inject
    TestServiceScheduler testServiceScheduler;

    private BluetoothPlatform spyBluetoothPlatform;

    private TestPlatform spyPlatform;
    private Settings modifiedSettings;
    private UIScanner tested;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        spyPlatform = spy(new TestPlatform());
        spyBluetoothPlatform = spy(new TestBluetoothPlatform());
        modifiedSettings = spy(new Settings(new DumbSucessTransport()));
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        when(modifiedSettings.getForeGroundScanTime()).thenReturn(Constants.Time.ONE_DAY);
        when(modifiedSettings.getForeGroundWaitTime()).thenReturn(Constants.Time.ONE_SECOND);
        tested = new UIScanner(modifiedSettings, spyPlatform.clock, testFileManager, testServiceScheduler, spyPlatform, spyBluetoothPlatform);
    }

    public void test_should_pause_when_going_to_the_background_and_scanning_was_running() throws Exception {
        tested.hostApplicationInForeground();
        tested.start();

        spyPlatform.clock.setNowInMillis(modifiedSettings.getBackgroundScanTime() - 1 );
        spyPlatform.clock.setNowInMillis(modifiedSettings.getBackgroundScanTime() );
        spyPlatform.clock.setNowInMillis(modifiedSettings.getBackgroundScanTime() + 1 );

        reset(spyPlatform);
        tested.hostApplicationInBackground();

        verify(spyBluetoothPlatform).stopLeScan();
    }
}
