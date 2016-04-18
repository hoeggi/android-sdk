package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.settings.DefaultSettings;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestHandlerManager;
import com.sensorberg.sdk.testUtils.TestPlatform;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import android.test.AndroidTestCase;

import javax.inject.Inject;
import javax.inject.Named;

import static org.fest.assertions.api.Assertions.assertThat;

public class TheDefaultScanner extends AndroidTestCase {

    @Inject
    TestFileManager testFileManager;

    @Inject
    TestServiceScheduler testServiceScheduler;

    @Inject
    @Named("testHandlerWithCustomClock")
    TestHandlerManager testHandlerManager;

    @Inject
    @Named("testBluetoothPlatform")
    BluetoothPlatform bluetoothPlatform;

    private Scanner tested;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        TestPlatform platform = new TestPlatform();
        tested = new Scanner(new Settings(new DumbSucessTransport()), false, testHandlerManager.getCustomClock(), testFileManager, testServiceScheduler,
                testHandlerManager, bluetoothPlatform);

        tested.start();
    }

    public void test_should_be_initially_setup_to_scan_in_with_the_background_configuration() throws Exception {
        assertThat(tested.waitTime).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
        assertThat(tested.scanTime).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
    }
}



