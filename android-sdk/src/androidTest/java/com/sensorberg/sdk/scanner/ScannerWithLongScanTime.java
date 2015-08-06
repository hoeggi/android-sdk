package com.sensorberg.sdk.scanner;

import android.test.AndroidTestCase;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestPlatform;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScannerWithLongScanTime extends AndroidTestCase {

    private TestPlatform spyPlatform;
    private Settings modifiedSettings;
    private UIScanner tested;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        spyPlatform = spy(new TestPlatform());
        modifiedSettings = spy(new Settings(spyPlatform, null));

        when(modifiedSettings.getForeGroundScanTime()).thenReturn(Constants.Time.ONE_DAY);
        when(modifiedSettings.getForeGroundWaitTime()).thenReturn(Constants.Time.ONE_SECOND);
        tested = new UIScanner(modifiedSettings, spyPlatform);
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
