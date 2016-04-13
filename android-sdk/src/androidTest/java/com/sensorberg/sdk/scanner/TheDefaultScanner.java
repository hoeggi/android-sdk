package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestPlatform;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import android.test.AndroidTestCase;

import javax.inject.Inject;

import static org.fest.assertions.api.Assertions.assertThat;

public class TheDefaultScanner extends AndroidTestCase {

    @Inject
    TestFileManager testFileManager;

    @Inject
    TestServiceScheduler testServiceScheduler;

    private TestPlatform platform;
    private Scanner tested;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        platform = new TestPlatform();
        tested = new Scanner(new Settings(platform), platform, false, platform.clock, testFileManager, testServiceScheduler);

        tested.start();
    }

    public void test_should_be_initially_setup_to_scan_in_with_the_background_configuration() throws Exception {
        assertThat(tested.waitTime).isEqualTo(Settings.DEFAULT_BACKGROUND_WAIT_TIME);
        assertThat(tested.scanTime).isEqualTo(Settings.DEFAULT_BACKGROUND_SCAN_TIME);
    }
}



