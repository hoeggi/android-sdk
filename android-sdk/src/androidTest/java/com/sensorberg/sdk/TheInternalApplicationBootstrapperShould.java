package com.sensorberg.sdk;

import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.sensorberg.sdk.testUtils.TestPlatform;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import java.util.Arrays;

import javax.inject.Inject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(AndroidJUnit4.class)
public class TheInternalApplicationBootstrapperShould{

    private static final java.util.UUID UUID = java.util.UUID.randomUUID();
    private static final long SUPPRESSION_TIME = 10000;

    @Inject
    TestServiceScheduler testServiceScheduler;

    InternalApplicationBootstrapper tested;
    private BeaconEvent beaconEventSupressionTime;
    private BeaconEvent beaconEventSentOnlyOnce;
    private TestPlatform testPlatform;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        testPlatform = new TestPlatform();
        BeaconActionHistoryPublisher.REALM_FILENAME = String.format("realm-%d.realm", System.currentTimeMillis());

        tested = spy(new InternalApplicationBootstrapper(testPlatform, testServiceScheduler, testPlatform, testPlatform.clock));

        beaconEventSupressionTime = new BeaconEvent.Builder()
                .withAction(new InAppAction(UUID, "irrelevant", "irrelevant", null ,null, 0))
                .withSuppressionTime(SUPPRESSION_TIME)
                .withPresentationTime(0)
                .build();

        beaconEventSentOnlyOnce = new BeaconEvent.Builder()
                .withAction(new InAppAction(UUID, "irrelevant", "irrelevant", null ,null, 0))
                .withSendOnlyOnce(true)
                .withPresentationTime(0)
                .build();
    }

    @Test
    public void test_suppression_time() throws Exception {
        tested.onResolutionsFinished(Arrays.asList(beaconEventSupressionTime));
        tested.onResolutionsFinished(Arrays.asList(beaconEventSupressionTime));
        verify(tested, times(1)).presentBeaconEvent(any(BeaconEvent.class));
    }

    @Test
    public void test_end_of_supression_time(){
        tested.onResolutionsFinished(Arrays.asList(beaconEventSupressionTime));

        testPlatform.clock.setNowInMillis(SUPPRESSION_TIME + 1);

        tested.onResolutionsFinished(Arrays.asList(beaconEventSupressionTime));
        verify(tested, times(2)).presentBeaconEvent(any(BeaconEvent.class));
    }

    @Test
    public void test_send_only_once(){
        tested.onResolutionsFinished(Arrays.asList(beaconEventSentOnlyOnce));
        tested.onResolutionsFinished(Arrays.asList(beaconEventSentOnlyOnce));
        verify(tested, times(1)).presentBeaconEvent(any(BeaconEvent.class));
    }


}
