package com.sensorberg.sdk.internal.http;

import com.android.sensorbergVolley.VolleyError;
import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.testUtils.TestPlatform;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.fest.assertions.api.Assertions;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;

import static org.mockito.Mockito.spy;

public class OkHttpUserAgentTest  extends SensorbergApplicationTest {

    @Inject
    @Named("noClock")
    Clock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    private OkHttpClientTransport transport;
    TestPlatform plattform;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        plattform = spy(new TestPlatform());

        transport = new OkHttpClientTransport(null, plattform.getCachedVolleyQueue(), clock, testPlatformIdentifier, true);
        startWebserver();
    }

    public void testUserAgentIsSetInVolleyOkHttpHeader() throws Exception {

        server.enqueue(new MockResponse().setBody("{}"));
        transport.perform(getUrl("/layout").toString(), new com.android.sensorbergVolley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new com.android.sensorbergVolley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RecordedRequest request = waitForRequests(1).get(0);
        Assertions.assertThat(request.getHeader("User-Agent")).isEqualTo(testPlatformIdentifier.getUserAgentString());
    }

    public void testInstallationIdentifierIsSetInVolleyOkHttpHeader() throws Exception {

        server.enqueue(new MockResponse().setBody("{}"));
        transport.perform(getUrl("/layout").toString(), new com.android.sensorbergVolley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new com.android.sensorbergVolley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RecordedRequest request = waitForRequests(1).get(0);
        Assertions.assertThat(request.getHeader("X-iid")).isEqualTo(testPlatformIdentifier.getDeviceInstallationIdentifier());
    }

    public void testAdvertiserIdentifierIsSetInVolleyOkHttpHeader() throws Exception {

        server.enqueue(new MockResponse().setBody("{}"));
        transport.perform(getUrl("/layout").toString(), new com.android.sensorbergVolley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new com.android.sensorbergVolley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RecordedRequest request = waitForRequests(1).get(0);
        Assertions.assertThat(request.getHeader("X-aid")).isEqualTo(testPlatformIdentifier.getAdvertiserIdentifier());
    }
}
