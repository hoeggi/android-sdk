package com.sensorberg.sdk.internal.http;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.android.sensorbergVolley.RequestQueue;
import com.android.sensorbergVolley.Response;
import com.android.sensorbergVolley.toolbox.JsonObjectRequest;
import com.android.sensorbergVolley.toolbox.RequestFuture;
import com.sensorberg.android.okvolley.GsonRequest;
import com.sensorberg.android.okvolley.OkVolley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    private static final String SETTINGS = "https://connect.sensorberg.com/api/applications/69954b55cdb77846d1f8b844bfc4004e722c910afdee638012a104f7f9842c33/settings/android/";

    public ApplicationTest(Class<Application> applicationClass) {
        super(Application.class);
    }

    public void test_something() throws Exception {
        RequestQueue queue = OkVolley.newRequestQueue(getContext(), false);

        GsonRequest<HashMap> integerGsonRequest = new GsonRequest<HashMap>(SETTINGS, HashMap.class,  null, new Response.Listener<HashMap>(){

            @Override
            public void onResponse(HashMap o) {

            }
        }, null);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(SETTINGS, null, future, future);
        queue.add(request);


        try {
            JSONObject response = future.get(30, TimeUnit.SECONDS); // this will block
            Log.d("TEST", response.toString(3));
        } catch (InterruptedException e) {
            // exception handling
            fail();
        } catch (ExecutionException e) {
            // exception handling
            fail();
        }

    }
}