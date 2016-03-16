package com.sensorberg.sdk.model.sugarorm;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.sensorberg.sdk.model.ISO8601TypeAdapter;
import com.sensorberg.sdk.model.realm.RealmFields;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author skraynick
 * @version 16-03-14
 */
public class SugarScan extends SugarRecord {

    private long eventTime;
    private boolean isEntry;
    private String proximityUUID;
    private int proximityMajor;
    private int proximityMinor;
    private long sentToServerTimestamp;
    private long sentToServerTimestamp2;
    private long createdAt;

    /**
     * Default constructor as required by SugarORM.
     */
    public SugarScan(){
    }

    //Getters and Setters
    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public boolean isEntry() {
        return isEntry;
    }

    public void setIsEntry(boolean isEntry) {
        this.isEntry = isEntry;
    }

    public String getProximityUUID() {
        return proximityUUID;
    }

    public void setProximityUUID(String proximityUUID) {
        this.proximityUUID = proximityUUID;
    }

    public int getProximityMajor() {
        return proximityMajor;
    }

    public void setProximityMajor(int proximityMajor) {
        this.proximityMajor = proximityMajor;
    }

    public int getProximityMinor() {
        return proximityMinor;
    }

    public void setProximityMinor(int proximityMinor) {
        this.proximityMinor = proximityMinor;
    }

    public int getTrigger(){
        return isEntry() ? ScanEventType.ENTRY.getMask() : ScanEventType.EXIT.getMask();
    }

    /**
     * Do not use after 1.0.1. There was a bug in volley that prevented the data from being sent to the server correctly.
     * @return  the deprecated timestamp
     */
    @Deprecated()
    public long getSentToServerTimestamp() {
        return sentToServerTimestamp;
    }

    /**
     * Do not use after 1.0.1. There was a bug in volley that prevented the data from being sent to the server correctly.
     * @param sentToServerTimestamp the deprecated timestamp
     */
    @Deprecated()
    public void setSentToServerTimestamp(long sentToServerTimestamp) {
        this.sentToServerTimestamp = sentToServerTimestamp;
    }

    public long getSentToServerTimestamp2() {
        return sentToServerTimestamp2;
    }

    public void setSentToServerTimestamp2(long sentToServerTimestamp2) {
        this.sentToServerTimestamp2 = sentToServerTimestamp2;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getPid(){
        return this.getProximityUUID().replace("-", "") + String.format("%1$05d%2$05d", this.getProximityMajor() , this.getProximityMinor());
    }

    //Functionality.

    /**
     * Creates a SugarScan Object.
     *
     * @param scanEvent - Sugar Event object.
     * @param timeNow -  the time now.
     *
     * @return - Returns a sugar scan object.
     */
    public static SugarScan from(ScanEvent scanEvent, long timeNow) {
        SugarScan value = new SugarScan();
        value.setEventTime(scanEvent.getEventTime());
        value.setIsEntry(scanEvent.getEventMask() == ScanEventType.ENTRY.getMask());
        value.setProximityUUID(scanEvent.getBeaconId().getUuid().toString());
        value.setProximityMajor(scanEvent.getBeaconId().getMajorId());
        value.setProximityMinor(scanEvent.getBeaconId().getMinorId());
        value.setSentToServerTimestamp2(RealmFields.Scan.NO_DATE);
        value.setCreatedAt(timeNow);
        return value;
    }

    /**
     * Returns a list of notSentScans.
     *
     * @return - List of not sent scans.
     */
    public static List<SugarScan> notSentScans(){
        return Select.from(SugarScan.class)
                .where(Condition.prop("SENT_TO_SERVER_TIMESTAMP2").eq(SugarFields.Scan.NO_DATE))
                .list();
    }

    //TODO - fix for update.
    public static void maskAsSent(List<SugarScan> scans, long timeNow, long cacheTtl) {
        if (scans.size() > 0) {
            for (int i = scans.size() - 1; i >= 0; i--) {
                scans.get(i).setSentToServerTimestamp2(timeNow);
            }
        }
        removeAllOlderThan(timeNow, cacheTtl);
    }

    /**
     * Remove older than specified scans.
     *
     * @param timeNow - Time now in milliseconds.
     * @param cacheTtl - ?
     */
    public static void removeAllOlderThan(long timeNow, long cacheTtl) {
        List<SugarScan> actionsToDelete = Select.from(SugarScan.class)
                .where(Condition.prop("CREATED_AT").lt(timeNow - cacheTtl))
                .and(Condition.prop(SugarFields.Scan.sentToServerTimestamp2Column).notEq(RealmFields.Action.NO_DATE))
                .list();

        if (actionsToDelete.size() > 0){
            for (int i = actionsToDelete.size() - 1; i >= 0; i--) {
                actionsToDelete.get(i).delete();
            }
        }
    }

    /**
     * Sugar scan object type adapter.
     */
    public static class SugarScanObjectTypeAdapter extends TypeAdapter<SugarScan> {

        @Override
        public void write(JsonWriter out, SugarScan value) throws IOException {
            out.beginObject();
            out.name("pid").value(value.getPid());
            out.name("trigger").value(value.getPid());
            out.name("dt");
            ISO8601TypeAdapter.DATE_ADAPTER.write(out, new Date(value.getEventTime()));
            out.endObject();
        }

        @Override
        public SugarScan read(JsonReader in) throws IOException {
            throw new IllegalArgumentException("You must not use this to read a SugarScanObject");
        }
    }
}
