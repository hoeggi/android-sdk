package com.sensorberg.sdk.internal.http;

import com.android.sensorbergVolley.RequestQueue;
import com.android.sensorbergVolley.VolleyError;
import com.android.sensorbergVolley.toolbox.BasicNetwork;
import com.android.sensorbergVolley.toolbox.DiskBasedCache;
import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.Transport;
import com.sensorberg.sdk.internal.http.helper.OkHttpStackWithFailures;
import com.sensorberg.sdk.internal.transport.SettingsCallback;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.json.JSONObject;

import java.io.File;

import util.TestConstants;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static util.Utils.failWithVolleyError;

public class OkHttpClientTransportWithRetries extends SensorbergApplicationTest {

    protected Transport tested;
    protected TestPlatform testPlattform;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        testPlattform = spy(new TestPlatform());

        BasicNetwork network = new BasicNetwork(new OkHttpStackWithFailures(2));

        File cacheDir = new File(getContext().getCacheDir(), "volley-test-" + String.valueOf(System.currentTimeMillis()));
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        when(testPlattform.getVolleyQueue()).thenReturn(queue);

        tested = new OkHttpClientTransport(testPlattform, null);
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
