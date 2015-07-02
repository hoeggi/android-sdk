package com.sensorberg.sdk.model.realm;

import java.util.Date;

public class RealmFields {

    public interface Scan {
        String eventTime                = "eventTime";
        String hardwareAdress           = "hardwareAdress";
        String isEntry                  = "isEntry";
        String proximityUUID            = "proximityUUID";
        String proximityMajor           = "proximityMajor";
        String proximityMinor           = "proximityMinor";
        @Deprecated
        String sentToServerTimestamp    = "sentToServerTimestamp";
        String sentToServerTimestamp2   = "sentToServerTimestamp2";
        String createdAt                = "createdAt";
        long NO_DATE = Long.MIN_VALUE;
    }

    public interface Action {
        String actionId                 = "actionId";
        String timeOfPresentation       = "timeOfPresentation";
        String trigger                  = "trigger";
        @Deprecated
        String sentToServerTimestamp    = "sentToServerTimestamp";
        String sentToServerTimestamp2   = "sentToServerTimestamp2";
        String createdAt                = "createdAt";
        String pid                      = "pid";
        String keepForever              = "keepForever";

        long NO_DATE = Long.MIN_VALUE;
    }
}
