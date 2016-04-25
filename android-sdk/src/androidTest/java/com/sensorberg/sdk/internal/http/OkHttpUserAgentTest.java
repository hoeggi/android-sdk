package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.TransportSettingsCallback;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.fest.assertions.api.Assertions;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import static org.mockito.Mockito.spy;

public class OkHttpUserAgentTest  extends SensorbergApplicationTest {

    @Inject
    @Named("noClock")
    Clock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    @Inject
    Gson gson;

    private Transport transport;
    TestPlatform plattform;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        plattform = spy(new TestPlatform());

        transport = new RetrofitApiTransport(getContext(), gson, clock, testPlatformIdentifier, true);
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
        Assertions.assertThat(request.getHeader("User-Agent")).isEqualTo(testPlatformIdentifier.getUserAgentString());
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
        Assertions.assertThat(request.getHeader("X-iid")).isEqualTo(testPlatformIdentifier.getDeviceInstallationIdentifier());
    }

    public void testAdvertiserIdentifierIsSetInVolleyOkHttpHeader() throws Exception {

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
        Assertions.assertThat(request.getHeader("X-aid")).isEqualTo(testPlatformIdentifier.getAdvertiserIdentifier());
    }
}
