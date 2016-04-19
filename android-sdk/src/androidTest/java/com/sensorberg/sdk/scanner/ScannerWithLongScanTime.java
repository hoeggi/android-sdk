package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestHandlerManager;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import javax.inject.Inject;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScannerWithLongScanTime extends AndroidTestCase {

    @Inject
    TestFileManager testFileManager;

    @Inject
    TestServiceScheduler testServiceScheduler;

    @Inject
    SharedPreferences sharedPreferences;

    TestHandlerManager handlerManager;

    private BluetoothPlatform spyBluetoothPlatform;

    private SettingsManager modifiedSettings;
    private UIScanner tested;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        sharedPreferences.edit().clear().commit();

        handlerManager = new TestHandlerManager();
        spyBluetoothPlatform = spy(new TestBluetoothPlatform());
        modifiedSettings = spy(new SettingsManager(new DumbSucessTransport(), sharedPreferences));

        when(modifiedSettings.getForeGroundScanTime()).thenReturn(Constants.Time.ONE_DAY);
        when(modifiedSettings.getForeGroundWaitTime()).thenReturn(Constants.Time.ONE_SECOND);
        tested = new UIScanner(modifiedSettings, handlerManager.getCustomClock(), testFileManager, testServiceScheduler, handlerManager, spyBluetoothPlatform);
    }

    public void test_should_pause_when_going_to_the_background_and_scanning_was_running() throws Exception {
        tested.hostApplicationInForeground();
        tested.start();

        handlerManager.getCustomClock().setNowInMillis(modifiedSettings.getBackgroundScanTime() - 1);
        handlerManager.getCustomClock().setNowInMillis(modifiedSettings.getBackgroundScanTime());
        handlerManager.getCustomClock().setNowInMillis(modifiedSettings.getBackgroundScanTime() + 1 );

        tested.hostApplicationInBackground();

        verify(spyBluetoothPlatform).stopLeScan();
    }
}
