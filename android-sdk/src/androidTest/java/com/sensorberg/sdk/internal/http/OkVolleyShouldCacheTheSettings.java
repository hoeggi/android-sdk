package com.sensorberg.sdk.internal.http;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.android.sensorbergVolley.Request;
import com.android.sensorbergVolley.RequestQueue;
import com.android.sensorbergVolley.VolleyError;
import com.android.sensorbergVolley.toolbox.BasicNetwork;
import com.android.sensorbergVolley.toolbox.DiskBasedCache;
import com.sensorberg.android.okvolley.OkHttpStack;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.Transport;
import com.sensorberg.sdk.internal.transport.SettingsCallback;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.fest.assertions.api.Assertions;
import org.json.JSONObject;

import java.io.File;

import util.TestConstants;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by falkorichter on 14/01/15.
 */
public class OkVolleyShouldCacheTheSettings extends ApplicationTestCase<Application> {

    protected Transport tested;
    protected TestPlatform testPlattform;
    private OkHttpStack stack;

    public OkVolleyShouldCacheTheSettings() {
        super(Application.class);
    }


    @Override
    protected void setUp() throws Exception {
        createApplication();

        testPlattform = spy(new TestPlatform());

        stack = spy(new OkHttpStack());

        BasicNetwork network = new BasicNetwork(stack);

        File cacheDir = new File(getContext().getCacheDir(), "volley-test-" + String.valueOf(System.currentTimeMillis()));
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        when(testPlattform.getVolleyQueue()).thenReturn(queue);

        tested = new OkHttpClientTransport(testPlattform, null);
        tested.setApiToken(TestConstants.API_TOKEN);
    }

    public void test_should_only_call_the_network_once() throws Exception {
        tested.getSettings(SettingsCallback.NONE);
        tested.getSettings(new SettingsCallback() {
            @Override
            public void nothingChanged() {
                fail("there should be content returned by the network");
            }

            @Override
            public void onFailure(VolleyError e) {
                //fail("this should not fail");
            }

            @Override
            public void onSettingsFound(JSONObject settings) {
                Assertions.assertThat(settings.length()).isNotZero();
            }
        });
        verify(stack, times(1)).performRequest(any(Request.class), anyMap());
    }
}
