package com.sensorberg.sdk.model.sugarorm;

public class SugarFields {

    public interface Scan {
        String eventTime                = "eventTime";
        String hardwareAdress           = "hardwareAdress";
        String isEntry                  = "isEntry";
        String proximityUUID            = "proximityUUID";
        String proximityMajor           = "proximityMajor";
        String proximityMinor           = "proximityMinor";
        String eventTimeColoumn         = "EVENT_TIME";
        String hardwareAdressColumn     = "HARDWARE_ADDRESS";
        String isEntryColumn            = "IS_ENTRY";
        String proximityUUIDColumn      = "PROXIMITY_UUID";
        String proximityMajorColumn     = "PROXIMITY_MAJOR";
        String proximityMinorColumn     = "PROXIMITY_MINOR";
        /**
         * see {@link SugarScan#setCreatedAt(long)} and {@link SugarScan#getCreatedAt()}
         */
        @Deprecated
        String sentToServerTimestamp        = "sentToServerTimestamp";
        String sentToServerTimestamp2       = "sentToServerTimestamp2";
        String createdAt                    = "createdAt";
        String sentToServerTimestampColumn  = "SENT_TO_SERVER_TIMESTAMP";
        String sentToServerTimestamp2Column = "SENT_TO_SERVER_TIMESTAMP2";
        String createdAtColumn              = "CREATED_AT";

        long NO_DATE = Long.MIN_VALUE;
    }

    public interface Action {
        String actionId                 = "actionId";
        String timeOfPresentation       = "timeOfPresentation";
        String trigger                  = "trigger";
        String actionIdColumn           = "ACTION_ID";
        String timeOfPresentationColumn = "TIME_OF_PRESENTATION";
        String triggerColumn            = "TRIGGER";
        /**
         * see {@link SugarAction#setCreatedAt(long)} and {@link SugarAction#getCreatedAt()}
         */
        @Deprecated
        String sentToServerTimestamp         = "sentToServerTimestamp";
        String sentToServerTimestamp2        = "sentToServerTimestamp2";
        String createdAt                     = "createdAt";
        String pid                           = "pid";
        String keepForever                   = "keepForever";
        String sentToServerTimestampColumn   = "SENT_TO_SERVER_TIMESTAMP";
        String sentToServerTimestamp2Column  = "SENT_TO_SERVER_TIMESTAMP2";
        String createdAtColumn               = "CREATED_AT";
        String pidColoumn                    = "PID";
        String keepForeverColumn             = "KEEP_FOREVER";

        long NO_DATE = Long.MIN_VALUE;
    }
}
