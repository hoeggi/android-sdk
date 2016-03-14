package com.sensorberg.sdk.model.sugarorm;

import com.sensorberg.sdk.model.realm.RealmScan;

public class SugarFields {

    public interface Scan {
        String eventTime                = "eventTime";
        String hardwareAdress           = "hardwareAdress";
        String isEntry                  = "isEntry";
        String proximityUUID            = "proximityUUID";
        String proximityMajor           = "proximityMajor";
        String proximityMinor           = "proximityMinor";
        /**
         * see {@link RealmScan#setCreatedAt(long)} and {@link RealmScan#getCreatedAt()}
         */
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
        /**
         * see {@link SugarAction#setCreatedAt(long)} and {@link SugarAction#getCreatedAt()}
         */
        @Deprecated
        String sentToServerTimestamp    = "sentToServerTimestamp";
        String sentToServerTimestamp2   = "sentToServerTimestamp2";
        String createdAt                = "createdAt";
        String pid                      = "pid";
        String keepForever              = "keepForever";

        long NO_DATE = Long.MIN_VALUE;
    }
}
