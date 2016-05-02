package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.URLFactory;
import com.sensorberg.sdk.internal.interfaces.BeaconHistoryUploadIntervalListener;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.internal.transport.interfaces.TransportSettingsCallback;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestClock;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.http.HEAD;
import util.TestConstants;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class TransportShould extends SensorbergApplicationTest {

    private static final UUID BEACON_ID = UUID.fromString("192E463C-9B8E-4590-A23F-D32007299EF5");

    private static final int MAJOR = 1337;

    private static final int MINOR = 1337;

    @Inject
    @Named("testClock")
    TestClock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    @Inject
    Gson gson;

    protected Transport tested;

    protected TestPlatform testPlattform;

    private ScanEvent scanEvent;

    private BeaconHistoryUploadIntervalListener listener;

    RetrofitApiServiceImpl mockRetrofitApiService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testPlattform = spy(new TestPlatform());
        clock.setNowInMillis(new DateTime(2015, 7, 10, 1, 1, 1).getMillis());

        scanEvent = new ScanEvent.Builder()
                .withBeaconId(new BeaconId(BEACON_ID, MAJOR, MINOR))
                .withEventMask(ScanEventType.ENTRY.getMask())
                .withEventTime(clock.now())
                .build();

        listener = mock(BeaconHistoryUploadIntervalListener.class);

        mockRetrofitApiService = mock(RetrofitApiServiceImpl.class);

        tested = new RetrofitApiTransport(mockRetrofitApiService, clock);
        tested.setBeaconHistoryUploadIntervalListener(listener);
        tested.setApiToken(TestConstants.API_TOKEN);
    }

    public void test_should_forward_the_layout_upload_interval_to_the_settings() throws Exception {
        startWebserver(com.sensorberg.sdk.test.R.raw.resolve_resolve_with_report_trigger);
        tested.getBeacon(new ResolutionConfiguration(scanEvent), BeaconResponseHandler.NONE);

        waitForRequests(1);
        verify(listener).historyUploadIntervalChanged(1337L * 1000);
    }

    public void test_failures() throws Exception {
        fail();
        //TODO

//        Network network = spy(new BasicNetwork(new OkHttpStack()));
//        doThrow(new Exception()).when(network).performRequest(any(HeadersJsonObjectRequest.class));
//
//        tested.loadSettings(new TransportSettingsCallback() {
//            @Override
//            public void nothingChanged() {
//                fail();
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                Log.d("FOO", "onFailure" + e.getLocalizedMessage());
//            }
//
//            @Override
//            public void onSettingsFound(JSONObject settings) {
//                fail();
//            }
//        });
    }

    // https://staging-manage.sensorberg.com/#/campaign/edit/29fa875c-66db-4957-be65-abc83f35538d
    // https://manage.sensorberg.com/#/campaign/edit/0ec64004-18a5-41df-a5dc-810d395dec83
    public void test_a_beacon_request() {
        tested.getBeacon(new ResolutionConfiguration(scanEvent), new BeaconResponseHandler() {
            @Override
            public void onSuccess(List<BeaconEvent> foundBeaconEvents) {
                Assertions.assertThat(foundBeaconEvents).overridingErrorMessage("There should be 1 action to the Beacon %s at %s there were %d",
                        scanEvent.getBeaconId().toTraditionalString(), URLFactory.getResolveURLString(), foundBeaconEvents.size()).isNotNull()
                        .hasSize(1);
            }

            @Override
            public void onFailure(Throwable cause) {
                fail("there was a failure with this request");
            }
        });
    }

    public void test_a_settings_request() {
        tested.loadSettings(new TransportSettingsCallback() {
            @Override
            public void nothingChanged() {
                fail("there should be changes to no settings");
            }

            @Override
            public void onFailure(Exception e) {
                fail();
            }

            @Override
            public void onSettingsFound(Settings settings) {
                Assertions.assertThat(settings).isNotNull();
            }
        });
    }

    public void test_publish_data_to_the_server() throws Exception {
        List<SugarScan> scans = new ArrayList<>();
        List<SugarAction> actions = new ArrayList<>();

        SugarScan scan1 = new SugarScan();
        scan1.setCreatedAt(System.currentTimeMillis() - Constants.Time.ONE_HOUR);
        scan1.setIsEntry(true);
        scan1.setProximityUUID(TestConstants.ANY_BEACON_ID.getUuid().toString());
        scan1.setProximityMajor(TestConstants.ANY_BEACON_ID.getMajorId());
        scan1.setProximityMinor(TestConstants.ANY_BEACON_ID.getMinorId());
        scan1.setEventTime(scan1.getCreatedAt());

        scans.add(scan1);

        tested.publishHistory(scans, actions, new TransportHistoryCallback() {
            @Override
            public void onFailure(Exception volleyError) {
                fail();
            }

            @Override
            public void onInstantActions(List<BeaconEvent> instantActions) {

            }

            @Override
            public void onSuccess(List<SugarScan> scans, List<SugarAction> actions) {

            }
        });

    }
}