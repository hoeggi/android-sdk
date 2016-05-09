package com.sensorberg.sdk;

import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestHandlerManager;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(AndroidJUnit4.class)
public class TheInternalApplicationBootstrapperShould {

    private static final java.util.UUID UUID = java.util.UUID.randomUUID();

    private static final long SUPPRESSION_TIME = 10000;

    @Inject
    TestServiceScheduler testServiceScheduler;

    @Inject
    TestHandlerManager testHandlerManager;

    @Inject
    @Named("testBluetoothPlatform")
    BluetoothPlatform bluetoothPlatform;

    @Inject
    SharedPreferences sharedPreferences;

    InternalApplicationBootstrapper tested;

    private BeaconEvent beaconEventSupressionTime;

    private BeaconEvent beaconEventSentOnlyOnce;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        SugarScan.deleteAll(SugarScan.class);
        SugarAction.deleteAll(SugarAction.class);

        tested = spy(new InternalApplicationBootstrapper(new DumbSucessTransport(), testServiceScheduler, testHandlerManager, testHandlerManager.getCustomClock(),
                bluetoothPlatform, sharedPreferences));

        beaconEventSupressionTime = new BeaconEvent.Builder()
                .withAction(new InAppAction(UUID, "irrelevant", "irrelevant", null, null, 0))
                .withSuppressionTime(SUPPRESSION_TIME)
                .withPresentationTime(0)
                .build();

        beaconEventSentOnlyOnce = new BeaconEvent.Builder()
                .withAction(new InAppAction(UUID, "irrelevant", "irrelevant", null, null, 0))
                .withSendOnlyOnce(true)
                .withPresentationTime(0)
                .build();
    }

    @Test
    public void test_suppression_time() throws Exception {
        tested.onResolutionsFinished(Arrays.asList(beaconEventSupressionTime));
        verify(tested, times(1)).presentBeaconEvent(any(BeaconEvent.class));
    }

    @Test
    public void test_end_of_supression_time() {
        tested.onResolutionsFinished(Arrays.asList(beaconEventSupressionTime));

        testHandlerManager.getCustomClock().setNowInMillis(SUPPRESSION_TIME + 1);

        tested.onResolutionsFinished(Arrays.asList(beaconEventSupressionTime));
        verify(tested, times(2)).presentBeaconEvent(any(BeaconEvent.class));
    }

    @Test
    public void test_send_only_once() {
        tested.onResolutionsFinished(Arrays.asList(beaconEventSentOnlyOnce));
        verify(tested, times(1)).presentBeaconEvent(any(BeaconEvent.class));
    }

    public void test_should_return_the_sync_setting(){
        Assertions.assertThat(tested.isSyncEnabled()).isTrue();
    }

}
