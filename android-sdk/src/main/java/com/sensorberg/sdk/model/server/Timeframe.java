package com.sensorberg.sdk.model.server;

import java.util.Date;

@SuppressWarnings("WeakerAccess") //gson serialization
public class Timeframe {
    /**
     * can be null, if so, only the end is important.
     */
    public Date start;
    /**
     * can be null, if so, only the start is important.
     */
    public Date end;

    public Timeframe(Long startMillis, Long endMillis) {
        if(startMillis != null){
            start = new Date(startMillis);
        }
        if (endMillis != null){
            end = new Date(endMillis);
        }
    }

    public boolean valid(long now) {
        return (start == null || now >= start.getTime()) &&
                 (end == null || now <= end.getTime());
    }
}
