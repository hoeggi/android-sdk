package com.sensorberg.sdk.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.Date;

public abstract class ISO8601TypeAdapter extends TypeAdapter<Date>{

    private static final DateTimeFormatter iso8601Format = ISODateTimeFormat.dateTime();

    public static final TypeAdapter<Date> DATE_ADAPTER = new ISO8601TypeAdapter() {
        @Override
        public void write(JsonWriter out, Date value) throws IOException {
            if (value != null) {
                out.value(iso8601Format.print(value.getTime()));
            } else {
                out.nullValue();
            }
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return iso8601Format.parseDateTime(in.nextString()).toDate();
        }
    };
}
