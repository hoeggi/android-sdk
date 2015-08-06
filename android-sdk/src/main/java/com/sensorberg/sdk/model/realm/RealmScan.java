package com.sensorberg.sdk.model.realm;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sensorberg.sdk.model.ISO8601TypeAdapter;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

//needed for realm serialization
@SuppressWarnings("WeakerAccess")
public class RealmScan extends RealmObject {

    private long eventTime;
    private boolean isEntry;
    private String proximityUUID;
    private int proximityMajor;
    private int proximityMinor;
    private long sentToServerTimestamp;
    private long sentToServerTimestamp2;
    private long createdAt;

    public static RealmScan from(ScanEvent scanEvent, Realm realm, long now) {
        RealmScan value = realm.createObject(RealmScan.class);
        value.setEventTime(scanEvent.getEventTime());
        value.setEntry(scanEvent.getEventMask() == ScanEventType.ENTRY.getMask());
        value.setProximityUUID(scanEvent.getBeaconId().getUuid().toString());
        value.setProximityMajor(scanEvent.getBeaconId().getMajorId());
        value.setProximityMinor(scanEvent.getBeaconId().getMinorId());
        value.setSentToServerTimestamp2(RealmFields.Scan.NO_DATE);
        value.setCreatedAt(now);
        return value;
    }

    public boolean isEntry() {
        return isEntry;
    }

    public void setEntry(boolean isEntry) {
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

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
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

    public String getPid(){
        return this.getProximityUUID().replace("-", "") + String.format("%1$05d%2$05d", this.getProximityMajor() , this.getProximityMinor());
    }

    public int getTrigger(){
        return isEntry() ? ScanEventType.ENTRY.getMask() : ScanEventType.EXIT.getMask();
    }


    public static Type ADAPTER_TYPE() {
        try {
            return Class.forName("io.realm.RealmScanRealmProxy");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("io.realm.RealmScanRealmProxy was not found");
        }
    }

    public static RealmResults<RealmScan> notSentScans(Realm realm){
        RealmQuery<RealmScan> scans = realm.where(RealmScan.class)
                .equalTo(RealmFields.Scan.sentToServerTimestamp2, RealmFields.Scan.NO_DATE);
        return scans.findAll();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public static void maskAsSent(List<RealmScan> scans, Realm realm, long now, long cacheTtl) {
        if (scans.size() > 0) {
            realm.beginTransaction();
            for (int i = scans.size() - 1; i >= 0; i--) {
                scans.get(i).setSentToServerTimestamp2(now);
            }
            realm.commitTransaction();
        }
        removeAllOlderThan(realm, now, cacheTtl);
    }

    public static void removeAllOlderThan(Realm realm, long now, long cacheTtl) {
        RealmResults<?> actionsToDelete = realm.where(RealmScan.class)
                .lessThan(RealmFields.Scan.createdAt, now - cacheTtl)
                .not().equalTo(RealmFields.Scan.sentToServerTimestamp2, RealmFields.Action.NO_DATE)
                .findAll();

        if (actionsToDelete.size() > 0){
            realm.beginTransaction();
            for (int i = actionsToDelete.size() - 1; i >= 0; i--) {
                actionsToDelete.get(i).removeFromRealm();
            }
            realm.commitTransaction();
        }
    }

    public long getSentToServerTimestamp2() {
        return sentToServerTimestamp2;
    }

    public void setSentToServerTimestamp2(long sentToServerTimestamp2) {
        this.sentToServerTimestamp2 = sentToServerTimestamp2;
    }

    public static class RealmScanObjectTypeAdapter extends TypeAdapter<RealmScan> {

        @Override
            public void write(JsonWriter out, RealmScan value) throws IOException {
                out.beginObject();
                out.name("pid").value(value.getPid());
                out.name("trigger").value(value.getTrigger());
                out.name("dt");
                ISO8601TypeAdapter.DATE_ADAPTER.write(out, new Date(value.getEventTime()));
                out.endObject();
            }

            @Override
            public RealmScan read(JsonReader in) throws IOException {
                throw new IllegalArgumentException("you must not use this to read a RealmScanObject");
            }

    }
}
