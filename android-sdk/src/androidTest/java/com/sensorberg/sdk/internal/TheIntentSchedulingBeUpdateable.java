package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.test.TestGenericBroadcastReceiver;
import com.sensorberg.sdk.testUtils.TestClock;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.fest.assertions.api.Assertions;

import android.app.AlarmManager;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.FlakyTest;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

public class TheIntentSchedulingBeUpdateable extends AndroidTestCase {

    @Inject
    AlarmManager alarmManager;

    @Inject
    @Named("testClock")
    TestClock androidClock;

    @Inject
    PersistentIntegerCounter persistentIntegerCounter;

    TestServiceScheduler testServiceScheduler;

    private Bundle INTENT_BUNDLE;

    private Bundle INTENT_BUNDLE_2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testServiceScheduler = new TestServiceScheduler(getContext(), alarmManager, androidClock, persistentIntegerCounter, Constants.Time.ONE_SECOND / 10);
        INTENT_BUNDLE = new Bundle();
        INTENT_BUNDLE.putString("foo", "bar");

        INTENT_BUNDLE_2 = new Bundle();
        INTENT_BUNDLE_2.putString("bar", "foo");
        TestGenericBroadcastReceiver.reset();

    }

    @FlakyTest(tolerance = 5)
    public void testShouldUpdateAnIntent() throws InterruptedException {
        long time = System.currentTimeMillis();
        long index = System.currentTimeMillis();
        testServiceScheduler.scheduleIntent(index, 500L, INTENT_BUNDLE);
        testServiceScheduler.scheduleIntent(index, 500L, INTENT_BUNDLE_2);

        boolean intentFired = TestGenericBroadcastReceiver.getLatch().await(10, TimeUnit.SECONDS);
        Assertions.assertThat(intentFired)
                .overridingErrorMessage("The intent was not fired")
                .isTrue();
        Assertions.assertThat(TestGenericBroadcastReceiver.getIntent().getStringExtra("bar"))
                .overridingErrorMessage("the second scheduled intent should have been fired")
                .isNotNull()
                .isEqualTo("foo");
        long elapestime = System.currentTimeMillis() - time;
        Log.d("TEST", "The exact time was" + elapestime + "millis");
    }
}
