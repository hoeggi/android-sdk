package com.sensorberg.sdk.scanner;

import org.fest.assertions.api.AbstractAssert;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import okhttp3.mockwebserver.RecordedRequest;
import util.Utils;


public class RecordedRequestAssert extends AbstractAssert<RecordedRequestAssert, RecordedRequest> {

    private RecordedRequestAssert(RecordedRequest actual) {
        super(actual, AbstractAssert.class);
    }

    public static RecordedRequestAssert assertThat(RecordedRequest request){
        return new RecordedRequestAssert(request);
    }

    public RecordedRequestAssert matchesRawResourceRequest(int rawResourceID, Context context) throws IOException, JSONException {
        JSONObject expectedRequest = Utils.getRawResourceAsJSON(rawResourceID, context);
        JSONObject body = new JSONObject(actual.getBody().toString());

        jsonObjsAreEqual(expectedRequest.getJSONObject("body"), body);


        return this;
    }

    public static boolean jsonObjsAreEqual (JSONObject js1, JSONObject js2) throws JSONException {
        if (js1 == null || js2 == null) {
            return (js1 == js2);
        }

        List<String> l1 =  asList(js1.keys());
        Collections.sort(l1);
        List<String> l2 =  asList(js2.keys());
        Collections.sort(l2);
        if (!l1.equals(l2)) {
            return false;
        }
        for (String key : l1) {
            Object val1 = js1.get(key);
            Object val2 = js2.get(key);
            if (val1 instanceof JSONObject) {
                if (!(val2 instanceof JSONObject)) {
                    return false;
                }
                if (!jsonObjsAreEqual((JSONObject)val1, (JSONObject)val2)) {
                    return false;
                }
            }

            if (val1 == null) {
                if (val2 != null) {
                    return false;
                }
            }  else if (!val1.equals(val2)) {
                return false;
            }
        }
        return true;
    }

    private static<T> List<T> asList(Iterator<T> iterator) {
        List<T> value = new ArrayList<T>();
        while (iterator.hasNext()){
            value.add(iterator.next());
        }
        return value;
    }

}
