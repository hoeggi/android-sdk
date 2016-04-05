package com.sensorberg.sdk.resolver;

import android.test.AndroidTestCase;

import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.URLFactory;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.fest.assertions.api.Assertions;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

import util.TestConstants;

import static org.mockito.Mockito.*;

public class TheResolverShould extends AndroidTestCase{

    private Resolver testedWithFakeBackend;

    private static final ScanEvent SCANEVENT_1 = new ScanEvent.Builder()
            .withBeaconId(TestConstants.REGULAR_BEACON_ID)
            .build();
    private Resolver tested;
    private TestPlatform androidPlattform;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ResolverConfiguration resolverConfiguration = new ResolverConfiguration();
        androidPlattform = spy(new TestPlatform());
        androidPlattform.setContext(getContext());
        androidPlattform.setTransport(new OkHttpClientTransport(androidPlattform, null));
        androidPlattform.getTransport().setApiToken(TestConstants.API_TOKEN);
        androidPlattform.clock.setNowInMillis(new DateTime(2015, 7, 7, 1, 1, 1).getMillis());

        testedWithFakeBackend = new Resolver(resolverConfiguration, androidPlattform);
        ResolverConfiguration realConfiguration = new ResolverConfiguration();
        tested = new Resolver(realConfiguration, androidPlattform);
    }


    public void test_should_try_to_resolve_a_beacon(){
        Resolution resolution = getResolution();
        Resolution spyResolution = spy(resolution);

        testedWithFakeBackend.startResolution(spyResolution);


        verify(spyResolution).queryServer();
    }

    private Resolution getResolution() {
        ResolutionConfiguration resolutionConiguration = new ResolutionConfiguration();
        resolutionConiguration.setScanEvent(SCANEVENT_1);
        return testedWithFakeBackend.createResolution(resolutionConiguration);
    }


    /**
     * account falko@sensorberg.com
     * https://manage.sensorberg.com/#/applications/edit/38eda3c5-649e-4178-9682-314d14abf1fe
     * https://manage.sensorberg.com/#/campaign/edit/bd67e5ec-4426-4f51-b962-6beea2c82695
     * https://manage.sensorberg.com/#/beacon/view/14053e1f-567b-43e5-818f-811c700b7ae4
     *
     */
    public void test_enter_exit_action(){
        URLFactory.Conf env = URLFactory.switchToProductionEnvironment();

        androidPlattform.getTransport().setApiToken("8961ee72ea4834053b376ad54007ea277cba4305db12188b74d104351ca8bf8a");


        ResolverListener mockListener = new ResolverListener() {
            @Override
            public void onResolutionFailed(Resolution resolution, Throwable cause) {
                fail(cause.getMessage());
            }

            @Override
            public void onResolutionsFinished(List<BeaconEvent> events) {
                //TODO check server configuration and change this again.
                //also, ideally, this test should use a mock request and not depend on server
                Assertions.assertThat(events).hasSize(0);
//                Assertions.assertThat(events).hasSize(1);
            }

        };
        tested.addResolverListener(mockListener);
        ResolutionConfiguration conf = new ResolutionConfiguration();
        conf.setScanEvent(new ScanEvent.Builder()
                        .withBeaconId(new BeaconId(UUID.fromString("73676723-7400-0000-ffff-0000ffff0003"), 40122, 43878))
                        .withEventMask(ScanEventType.ENTRY.getMask()).build()
        );
        Resolution resolution = tested.createResolution(conf);
        resolution.start();


        URLFactory.restorePreviousConf(env);
    }

    /**
     * https://manage.sensorberg.com/#/campaign/edit/ab68d4ee-8b2d-4f40-adc2-a7ebc9505e89
     * https://manage.sensorberg.com/#/campaign/edit/5dc7f22f-dbcf-4065-8b28-e81b0149fcc8
     * https://manage.sensorberg.com/#/campaign/edit/292ba508-226e-41c3-aac7-969fa712c435
     *
     * https://staging-manage.sensorberg.com/#/campaign/edit/be0c8822-937c-49ee-9890-13fb8ecbad05
     * https://staging-manage.sensorberg.com/#/campaign/edit/01fc187b-de29-4b87-b04c-32cdf60d4270
     * https://staging-manage.sensorberg.com/#/campaign/edit/ed464330-ea1b-4132-a993-d81796871587
     *
     */

    public void test_resolve_in_app_function() throws Exception {

        ResolverListener mockListener = new ResolverListener() {
            @Override
            public void onResolutionFailed(Resolution resolution, Throwable cause) {
                fail(cause.getMessage() + resolution.toString());
            }

            @Override
            public void onResolutionsFinished(List<BeaconEvent> events) {
                Assertions.assertThat(events).hasSize(3);
            }

        };
        tested.addResolverListener(mockListener);
        ResolutionConfiguration conf = new ResolutionConfiguration();
        conf.setScanEvent(new ScanEvent.Builder()
                        .withBeaconId(TestConstants.IN_APP_BEACON_ID)
                        .withEventMask(ScanEventType.ENTRY.getMask()).build()
        );
        Resolution resolution = tested.createResolution(conf);
        resolution.start();

    }

    /**
     *  https://manage.sensorberg.com/#/campaign/edit/6edd5ff0-d63a-4968-b7fa-b448d1c3a0e9
     *
     *  https://staging-manage.sensorberg.com/#/campaign/edit/be0c8822-937c-49ee-9890-13fb8ecbad05
     */
    public void test_beacon_with_delay() throws Exception {

        ResolverListener mockListener = mock(ResolverListener.class);
        tested.addResolverListener(mockListener);
        ResolutionConfiguration conf = new ResolutionConfiguration();
        conf.setScanEvent(new ScanEvent.Builder()
                .withBeaconId(TestConstants.DELAY_BEACON_ID)
                .withEventMask(ScanEventType.ENTRY.getMask()).build()
        );
        Resolution resolution = tested.createResolution(conf);
        resolution.start();

        verify(mockListener).onResolutionsFinished(argThat(new BaseMatcher<List<BeaconEvent>>() {
            public long delay;

            @Override
            public boolean matches(Object o) {
                List<BeaconEvent> list = (List<BeaconEvent>) o;
                delay = list.get(0).getAction().getDelayTime();
                return delay == 120000;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("Delaytime was %d and not %d as expected", delay, 120000));
            }
        }));
    }
}
