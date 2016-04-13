package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestPlatform;

import android.test.AndroidTestCase;

import javax.inject.Inject;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScannerWithLongScanTime extends AndroidTestCase {

    @Inject
    TestFileManager testFileManager;

    private TestPlatform spyPlatform;
    private Settings modifiedSettings;
    private UIScanner tested;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        spyPlatform = spy(new TestPlatform());
        modifiedSettings = spy(new Settings(spyPlatform));
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        when(modifiedSettings.getForeGroundScanTime()).thenReturn(Constants.Time.ONE_DAY);
        when(modifiedSettings.getForeGroundWaitTime()).thenReturn(Constants.Time.ONE_SECOND);
        tested = new UIScanner(modifiedSettings, spyPlatform, spyPlatform.clock, testFileManager);
    }

    public void test_should_pause_when_going_to_the_background_and_scanning_was_running() throws Exception {
        tested.hostApplicationInForeground();
        tested.start();

        spyPlatform.clock.setNowInMillis(modifiedSettings.getBackgroundScanTime() - 1 );
        spyPlatform.clock.setNowInMillis(modifiedSettings.getBackgroundScanTime() );
        spyPlatform.clock.setNowInMillis(modifiedSettings.getBackgroundScanTime() + 1 );


        reset(spyPlatform);
        tested.hostApplicationInBackground();

        verify(spyPlatform).stopLeScan();

    }
}
