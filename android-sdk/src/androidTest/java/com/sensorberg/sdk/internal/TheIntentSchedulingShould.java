package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.testUtils.TestClock;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.fest.assertions.api.Assertions;

import android.app.AlarmManager;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.FlakyTest;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

public class TheIntentSchedulingShould extends AndroidTestCase {

    @Inject
    AlarmManager alarmManager;

    @Inject
    @Named("testClock")
    TestClock androidClock;

    @Inject
    PersistentIntegerCounter persistentIntegerCounter;

    TestServiceScheduler testServiceScheduler;

    private Bundle INTENT_BUNDLE;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testServiceScheduler = new TestServiceScheduler(getContext(), alarmManager, androidClock, persistentIntegerCounter, Constants.Time.ONE_SECOND);
        INTENT_BUNDLE = new Bundle();
        INTENT_BUNDLE.putString("foo", "bar");
        TestGenericBroadcastReceiver.reset();
    }

    @FlakyTest(tolerance = 5)
    public void testShouldScheduleAnIntent() throws Exception {
        testServiceScheduler.scheduleIntent(1, 500L, INTENT_BUNDLE);

        boolean intentFired = TestGenericBroadcastReceiver.getLatch().await(10, TimeUnit.SECONDS);
        Assertions.assertThat(intentFired)
                .overridingErrorMessage("The intent was not fired")
                .isTrue();
    }

    @FlakyTest(tolerance = 5)
    public void testShouldUnScheduleAnIntent() throws Exception {
        testServiceScheduler.scheduleIntent(2, 500L, INTENT_BUNDLE);

        testServiceScheduler.unscheduleIntent(2);
        boolean intentFired = TestGenericBroadcastReceiver.getLatch().await(10, TimeUnit.SECONDS);
        Assertions.assertThat(intentFired)
                .overridingErrorMessage("The intent was fired even though it was unscheduled")
                .isFalse();
    }
}
