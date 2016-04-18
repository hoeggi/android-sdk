package com.sensorberg.sdk.internal.http;

import com.android.sensorbergVolley.RequestQueue;
import com.android.sensorbergVolley.toolbox.BasicNetwork;
import com.android.sensorbergVolley.toolbox.DiskBasedCache;
import com.sensorberg.android.okvolley.OkHttpStack;
import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.URLFactory;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.TransportSettingsCallback;
import com.sensorberg.sdk.test.R;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.fest.assertions.api.Assertions;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

import static org.mockito.Mockito.spy;

public class OkVolleyShouldCacheTheSettingsWithEtags extends SensorbergApplicationTest {

    private static final TransportSettingsCallback MUST_NOT_FAIL = new TransportSettingsCallback() {
        @Override
        public void nothingChanged() {
            fail("there should be content returned by the network");
        }

        @Override
        public void onFailure(Exception e) {
            //fail("this should not fail");
        }

        @Override
        public void onSettingsFound(JSONObject settings) {
            Assertions.assertThat(settings.length()).isNotZero();
        }
    };

    @Inject
    @Named("noClock")
    Clock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    protected Transport tested;
    private OkHttpStack stack;
    private RequestQueue queue;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        stack = spy(new OkHttpStack());

        BasicNetwork network = new BasicNetwork(stack);

        File cacheDir = new File(getContext().getCacheDir(), "volley-test-" + String.valueOf(System.currentTimeMillis()));
        queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        tested = new OkHttpClientTransport(null, queue, clock, testPlatformIdentifier, true);
        tested.setApiToken(TestConstants.API_TOKEN);
        startWebserver();
    }


    public void test_should_answer_correctly() throws Exception {
        enqueue(R.raw.response_etag_001);
        tested.setSettingsCallback(MUST_NOT_FAIL);
        waitForRequests(1);
    }

    public void test_should_cache() throws Exception {
        enqueue(R.raw.response_etag_001);
        tested.setSettingsCallback(MUST_NOT_FAIL);
        tested.setSettingsCallback(MUST_NOT_FAIL);

        waitForRequests(1);

        Assertions.assertThat(server.getRequestCount()).isEqualTo(1);
    }

    public void test_cache_revalidation_with_etag() throws Exception {
        enqueue(R.raw.response_etag_001, R.raw.response_etag_002);
        tested.setSettingsCallback(MUST_NOT_FAIL);
        Thread.sleep(1200);
        tested.setSettingsCallback(MUST_NOT_FAIL);

        waitForRequests(2);
    }


    public void test_cache_revalidation_with_header() throws Exception {
        enqueue(R.raw.response_etag_001, R.raw.response_etag_002);

        tested.setSettingsCallback(MUST_NOT_FAIL);
        Thread.sleep(1200);
        tested.setSettingsCallback(MUST_NOT_FAIL);
        Assertions.assertThat(server.getRequestCount()).overridingErrorMessage("there should be two request.").isEqualTo(2);

        List<RecordedRequest> requests = waitForRequests(2);

        Assertions.assertThat(requests.get(1).getHeader("If-None-Match")).isNotEmpty();

    }

    public void test_manual_cache_invalidation() throws Exception {
        enqueue(R.raw.response_etag_001, R.raw.response_etag_001);
        tested.setSettingsCallback(MUST_NOT_FAIL);

        queue.getCache().invalidate(URLFactory.getSettingsURLString(TestConstants.API_TOKEN), true);

        tested.setSettingsCallback(MUST_NOT_FAIL);
        Assertions.assertThat(server.getRequestCount()).overridingErrorMessage("there should be two request. after invalidating the cache").isEqualTo(2);
    }
}
