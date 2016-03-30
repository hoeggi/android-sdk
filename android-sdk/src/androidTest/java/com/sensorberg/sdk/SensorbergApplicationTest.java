package com.sensorberg.sdk;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.sensorberg.sdk.internal.URLFactory;
import com.sensorberg.sdk.internal.http.helper.RawJSONMockResponse;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.fest.assertions.api.Assertions;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

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
        server.play();
        previousConfiguration = URLFactory.switchToMockEnvironment(server.getUrl("/"));
    }

    protected java.net.URL getUrl(String path) {
        return server.getUrl(path);
    }

    public void enqueue(int... rawRequestsResourceIds) throws IOException, JSONException {
        for (int rawRequestId : rawRequestsResourceIds) {
            server.enqueue(fromRaw(rawRequestId));
        }
    }

    protected MockResponse fromRaw(int resourceID) throws IOException, JSONException {
        return RawJSONMockResponse.fromRawResource(getContext().getResources().openRawResource(resourceID)) ;
    }

    /*protected Realm getRealmInstance() {
        return Realm.getInstance(getContext(), BeaconActionHistoryPublisher.REALM_FILENAME);
    }*/

    protected List<RecordedRequest> waitForRequests(int i) throws InterruptedException {
        List<RecordedRequest> recordedRequests = new ArrayList<>();
        for (int i1 = i; i1 > 0; i1--) {
            recordedRequests.add(server.takeRequest(10, TimeUnit.SECONDS));
        }
        Assertions.assertThat(server.getRequestCount()).overridingErrorMessage("There should have been %d requests. Only %d requests were recorded.", i, server.getRequestCount()).isEqualTo(i);
        return recordedRequests;
    }
}
