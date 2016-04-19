package com.sensorberg.sdk.internal.http;

import com.android.sensorbergVolley.Request;
import com.android.sensorbergVolley.RequestQueue;
import com.android.sensorbergVolley.toolbox.BasicNetwork;
import com.android.sensorbergVolley.toolbox.DiskBasedCache;
import com.sensorberg.android.okvolley.OkHttpStack;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.TransportSettingsCallback;

import org.fest.assertions.api.Assertions;
import org.json.JSONObject;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by falkorichter on 14/01/15.
 */
public class OkVolleyShouldCacheTheSettings extends ApplicationTestCase<Application> {

    @Inject
    @Named("noClock")
    Clock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    protected Transport tested;
    private OkHttpStack stack;

    public OkVolleyShouldCacheTheSettings() {
        super(Application.class);
    }


    @Override
    protected void setUp() throws Exception {
        createApplication();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        stack = spy(new OkHttpStack());

        BasicNetwork network = new BasicNetwork(stack);

        File cacheDir = new File(getContext().getCacheDir(), "volley-test-" + String.valueOf(System.currentTimeMillis()));
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        tested = new OkHttpClientTransport(null, queue, clock, testPlatformIdentifier, true);
        tested.setApiToken(TestConstants.API_TOKEN);
    }

    public void test_should_only_call_the_network_once() throws Exception {
        tested.loadSettings(TransportSettingsCallback.NONE);
        tested.loadSettings(new TransportSettingsCallback() {
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
        });
        verify(stack, times(1)).performRequest(any(Request.class), anyMap());
    }
}
