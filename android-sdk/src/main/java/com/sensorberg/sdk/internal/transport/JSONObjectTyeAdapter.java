package com.sensorberg.sdk.internal.transport;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

class JSONObjectTyeAdapter extends TypeAdapter<JSONObject> {
    @Override
    public void write(JsonWriter out, JSONObject value) throws IOException {
        if (value == null){
            out.nullValue();
            return;
        }
        out.beginObject();
        Iterator<String> keys = value.keys();
        while(keys.hasNext()){
            String key = keys.next();
            try {
                Object valueObject = value.get(key);
                out.name(key);
                if (valueObject instanceof Number){
                    out.value((Number) valueObject);
                } else if (valueObject instanceof JSONObject){
                    write(out, (JSONObject) valueObject);
                } else if (valueObject instanceof String){
                    out.value((String) valueObject);
                } else {
                    out.value(valueObject.toString());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        out.endObject();

    }

    @Override
    public JSONObject read(JsonReader in) throws IOException {
        return readJSONObject(in);
    }


    private JSONObject readJSONObject(JsonReader in) throws IOException {
        JSONObject value = new JSONObject();

        in.beginObject();
        while (in.hasNext()){
            String key = in.nextName();
            JsonToken token = in.peek();
            try {
                switch (token) {
                    case NUMBER:
                        String stringValue = in.nextString();
                        value.put(key,parseNumber(stringValue));
                        break;
                    case BOOLEAN:
                        value.put(key, in.nextBoolean());
                        break;
                    case STRING:
                        value.put(key, in.nextString());
                        break;
                    case NULL:
                        in.nextNull();
                        value.put(key, JSONObject.NULL);
                        break;
                    case BEGIN_OBJECT:
                        value.put(key, readJSONObject(in));
                        break;
                    case BEGIN_ARRAY:
                        value.put(key, readJSONArray(in));
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        in.endObject();

        return value;
    }

    private Number parseNumber(String stringValue) {
        try {
            long longValue = Long.parseLong(stringValue);
            if (longValue <= Integer.MAX_VALUE && longValue >= Integer.MIN_VALUE) {
                return (int) longValue;
            } else {
                return longValue;
            }
        } catch (NumberFormatException nfe2){
            try {
                return Double.parseDouble(stringValue);
            } catch (NumberFormatException nfe3){
                return null;
            }
        }
    }

    private JSONArray readJSONArray(JsonReader in) throws IOException {
        JSONArray value = new JSONArray();
        in.beginArray();
        while (in.hasNext()) {
            JsonToken token = in.peek();
            switch (token) {
                case NUMBER:
                    String stringValue = in.nextString();
                    value.put(parseNumber(stringValue));
                    break;
                case BOOLEAN:
                    value.put(in.nextBoolean());
                    break;
                case STRING:
                    value.put(in.nextString());
                    break;
                case NULL:
                    in.nextNull();
                    value.put(JSONObject.NULL);
                    break;
                case BEGIN_OBJECT:
                    value.put(readJSONObject(in));
                    break;
                case BEGIN_ARRAY:
                    value.put(readJSONArray(in));
                    break;
            }
        }
        in.endArray();
        return value;
    }
}
