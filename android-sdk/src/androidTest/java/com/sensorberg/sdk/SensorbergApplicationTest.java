package com.sensorberg.sdk;

import com.sensorberg.sdk.internal.URLFactory;
import com.sensorberg.sdk.internal.http.helper.RawJSONMockResponse;

import org.fest.assertions.api.Assertions;
import org.json.JSONException;

import android.app.Application;
import android.net.Uri;
import android.test.ApplicationTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public abstract class SensorbergApplicationTest extends ApplicationTestCase<Application> {
    protected MockWebServer server;
    private URLFactory.Conf previousConfiguration;

    public SensorbergApplicationTest() {
        super(Application.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createApplication();
        System.setProperty("dexmaker.dexcache", getApplication().getCacheDir().getPath());
    }

    @Override
    public void tearDown() throws Exception {
        if (server != null){
            server.shutdown();
        }
        if (previousConfiguration != null) {
            URLFactory.restorePreviousConf(previousConfiguration);
        }
    }

    protected void startWebserver(int... rawRequestsResourceIds) throws IOException, JSONException {
        server = new MockWebServer();
        enqueue(rawRequestsResourceIds);
        server.start();
        previousConfiguration = URLFactory.switchToMockEnvironment(Uri.parse("https://test-resolver.sensorberg.com/layout/"));
    }

    public void enqueue(int... rawRequestsResourceIds) throws IOException, JSONException {
        for (int rawRequestId : rawRequestsResourceIds) {
            server.enqueue(fromRaw(rawRequestId));
        }
    }

    protected MockResponse fromRaw(int resourceID) throws IOException, JSONException {
        return RawJSONMockResponse.fromRawResource(getContext().getResources().openRawResource(resourceID)) ;
    }

    protected List<RecordedRequest> waitForRequests(int i) throws InterruptedException {
        List<RecordedRequest> recordedRequests = new ArrayList<>();
        for (int i1 = i; i1 > 0; i1--) {
            recordedRequests.add(server.takeRequest(10, TimeUnit.SECONDS));
        }
        Assertions.assertThat(server.getRequestCount()).overridingErrorMessage("There should have been %d requests. Only %d requests were recorded.", i, server.getRequestCount()).isEqualTo(i);
        return recordedRequests;
    }
}
