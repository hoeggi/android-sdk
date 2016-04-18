package com.sensorberg.sdk.internal.http;

import com.android.sensorbergVolley.RequestQueue;
import com.android.sensorbergVolley.VolleyError;
import com.android.sensorbergVolley.toolbox.BasicNetwork;
import com.android.sensorbergVolley.toolbox.DiskBasedCache;
import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.http.helper.OkHttpStackWithFailures;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.SettingsCallback;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.json.JSONObject;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static util.Utils.failWithVolleyError;

public class OkHttpClientTransportWithRetries extends SensorbergApplicationTest {

    @Inject
    @Named("noClock")
    Clock clock; //TODO should use test clock???

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    protected Transport tested;
    protected TestPlatform testPlattform;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        testPlattform = spy(new TestPlatform());

        BasicNetwork network = new BasicNetwork(new OkHttpStackWithFailures(2));

        File cacheDir = new File(getContext().getCacheDir(), "volley-test-" + String.valueOf(System.currentTimeMillis()));
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        when(testPlattform.getCachedVolleyQueue()).thenReturn(queue);

        tested = new OkHttpClientTransport(testPlattform, null, testPlattform.getCachedVolleyQueue(), clock, testPlatformIdentifier);
        tested.setApiToken(TestConstants.API_TOKEN);
    }

    public void test_succeed_even_after_two_failures() throws Exception {
        tested.getSettings(new SettingsCallback() {
            @Override
            public void nothingChanged() {
                fail();
            }

            @Override
            public void onFailure(VolleyError e) {
                failWithVolleyError(e, "failed to get settings");
            }

            @Override
            public void onSettingsFound(JSONObject settings) {

            }
        });

    }
}
