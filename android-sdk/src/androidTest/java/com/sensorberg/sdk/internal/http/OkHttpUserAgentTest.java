package com.sensorberg.sdk.internal.http;

import com.android.sensorbergVolley.VolleyError;
import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.Transport;
import com.sensorberg.sdk.testUtils.TestPlatform;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.fest.assertions.api.Assertions;
import org.json.JSONObject;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class OkHttpUserAgentTest  extends SensorbergApplicationTest {

    private OkHttpClientTransport transport;

    TestPlatform plattform;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        plattform = spy(new TestPlatform());
        plattform.setContext(getContext());

        when(plattform.useSyncClient()).thenReturn(true);

        transport = new OkHttpClientTransport(plattform, null);
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
        Assertions.assertThat(request.getHeader("User-Agent")).isEqualTo(plattform.getUserAgentString());
    }

    public void testAdvertisingIdIsSetInVolleyOkHttpHeader() throws Exception {

        transport.setAdvertisingIdentifier("ADVERTISING_ID_TEST");

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
        Assertions.assertThat(request.getHeader(Transport.ADVERTISING_IDENTIFIER)).isEqualTo("ADVERTISING_ID_TEST");
    }
}
