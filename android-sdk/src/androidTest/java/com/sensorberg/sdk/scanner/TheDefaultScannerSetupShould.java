package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestPlatform;

import android.test.AndroidTestCase;

import javax.inject.Inject;

import util.Utils;

import static org.fest.assertions.api.Assertions.assertThat;


public class TheDefaultScannerSetupShould extends AndroidTestCase{

    @Inject
    TestFileManager testFileManager;

    protected UIScanner tested;
    protected Settings settings;
    protected TestPlatform plattform;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        plattform = new TestPlatform();
        settings = new Settings(plattform);
        tested = new UIScanner(settings, plattform, plattform.clock, testFileManager);

        tested.scanTime = Long.MAX_VALUE;
        tested.waitTime = 0;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        settings = null;
        tested = null;
    }

    public void test_foreground_scanning_time_should_be_10seconds(){
        tested.hostApplicationInForeground();

        assertThat(tested.scanTime).isEqualTo(Utils.TEN_SECONDS);
    }

    public void test_foreground_pause_time_should_be_10seconds(){
        tested.hostApplicationInForeground();

        assertThat(tested.waitTime).isEqualTo(Utils.TEN_SECONDS);
    }

    public void test_background_scanning_time_should_be_20seconds(){
        tested.hostApplicationInBackground();

        assertThat(tested.scanTime).isEqualTo(Utils.ONE_SECOND * 20);
    }

    public void test_background_pause_time_should_be_2minutes(){
        tested.hostApplicationInBackground();

        assertThat(tested.waitTime).isEqualTo(Utils.ONE_MINUTE * 2);
    }

    public void test_exit_time_should_be_correct(){
        assertThat(settings.getExitTimeout()).isEqualTo(Utils.EXIT_TIME);
    }

    public void test_background_scan_time_should_be_bigger_than_exitTimeout(){
        tested.hostApplicationInBackground();

        assertThat(tested.waitTime).isGreaterThan(settings.getExitTimeout());
    }

    public void test_foreground_scan_time_should_be_bigger_than_exitTimeout(){
        assertThat(tested.scanTime).isGreaterThan(settings.getExitTimeout());
    }
}
