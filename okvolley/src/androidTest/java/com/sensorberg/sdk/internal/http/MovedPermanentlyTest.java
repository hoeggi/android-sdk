package com.sensorberg.sdk.internal.http;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.android.sensorbergVolley.AuthFailureError;
import com.android.sensorbergVolley.RequestQueue;
import com.android.sensorbergVolley.toolbox.RequestFuture;
import com.android.sensorbergVolley.toolbox.StringRequest;
import com.sensorberg.android.okvolley.OkVolley;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.apache.http.HttpStatus;
import org.fest.assertions.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class MovedPermanentlyTest extends ApplicationTestCase<Application> {

    private static final String OTHER_LOCATION = "/otherLocation";
    private static final String MOVED_PERMANENTLY = "/movedPermanently";
    private MockWebServer server;
    private RequestQueue tested;


    public MovedPermanentlyTest() {
        super(Application.class);
    }


    @Override
    protected void setUp() throws Exception {
        createApplication();

        tested = OkVolley.newRequestQueue(getContext(), false);

        server = new MockWebServer();
        server.play();

        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.SC_MOVED_PERMANENTLY)
                .addHeader("Location", server.getUrl(OTHER_LOCATION))
        );

        server.enqueue(new MockResponse().setBody("success"));
    }

    @Override
    public void tearDown() throws Exception {
        server.shutdown();
        super.tearDown();
    }

    public void test_headers_on_the_same_domain() throws Exception {
        RequestFuture<String> future =  RequestFuture.newFuture();
        StringRequest request = new StringRequest(server.getUrl("/movedPermanently").toString(), future, future){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<String, String>();
                header.put("foo", "bar");
                return header;
            }
        };

        tested.add(request);

        String body = future.get();


        Assertions.assertThat(body).isEqualTo("success");
        Assertions.assertThat(server.getRequestCount()).isEqualTo(2);

        RecordedRequest firstRequest = server.takeRequest();
        Assertions.assertThat(firstRequest.getPath()).isEqualTo(MOVED_PERMANENTLY);
        Assertions.assertThat(firstRequest.getHeader("foo")).isEqualTo("bar");

        RecordedRequest secondRequest = server.takeRequest();
        Assertions.assertThat(secondRequest.getPath()).isEqualTo(OTHER_LOCATION);
        Assertions.assertThat(secondRequest.getHeader("foo")).isEqualTo("bar");

    }
}
