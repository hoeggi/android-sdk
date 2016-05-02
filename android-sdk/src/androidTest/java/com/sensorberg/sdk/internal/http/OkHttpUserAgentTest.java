package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportSettingsCallback;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.fest.assertions.api.Assertions;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import static org.mockito.Mockito.spy;

public class OkHttpUserAgentTest extends SensorbergApplicationTest {

    @Inject
    @Named("noClock")
    Clock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    @Inject
    @Named("realRetrofitApiService")
    RetrofitApiServiceImpl realRetrofitApiService;

    @Inject
    Gson gson;

    private Transport transport;

    TestPlatform plattform;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        plattform = spy(new TestPlatform());

        transport = new RetrofitApiTransport(realRetrofitApiService, clock);
        startWebserver();
    }

    public void testUserAgentIsSetInVolleyOkHttpHeader() throws Exception {

        server.enqueue(new MockResponse().setBody("{}"));
        transport.loadSettings(new TransportSettingsCallback() {
            @Override
            public void nothingChanged() {

            }

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSettingsFound(Settings settings) {

            }
        });

        RecordedRequest request = waitForRequests(1).get(0);
        Assertions.assertThat(request.getHeader(Transport.HEADER_USER_AGENT)).isEqualTo(testPlatformIdentifier.getUserAgentString());
    }

    public void testInstallationIdentifierIsSetInVolleyOkHttpHeader() throws Exception {

        server.enqueue(new MockResponse().setBody("{}"));
        transport.loadSettings(new TransportSettingsCallback() {
            @Override
            public void nothingChanged() {

            }

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSettingsFound(Settings settings) {

            }
        });

        RecordedRequest request = waitForRequests(1).get(0);
        Assertions.assertThat(request.getHeader(Transport.HEADER_INSTALLATION_IDENTIFIER))
                .isEqualTo(testPlatformIdentifier.getDeviceInstallationIdentifier());
    }

    public void testAdvertiserIdentifierIsSetInVolleyOkHttpHeader() throws Exception {

        server.enqueue(new MockResponse().setBody("{}").setResponseCode(200));
        transport.loadSettings(new TransportSettingsCallback() {
            @Override
            public void nothingChanged() {

            }

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSettingsFound(Settings settings) {

            }
        });

        RecordedRequest request = waitForRequests(1).get(0);
        Assertions.assertThat(request.getHeader(Transport.HEADER_ADVERTISER_IDENTIFIER)).isEqualTo(testPlatformIdentifier.getAdvertiserIdentifier());
    }
}
