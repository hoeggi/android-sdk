package com.sensorberg.sdk.model.sugarorm;

import com.orm.SugarRecord;
import com.orm.query.Select;
import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.model.realm.RealmFields;
import com.sensorberg.sdk.resolver.BeaconEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Condition;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by skraynick on 16-03-14.
 */
public class SugarAction extends SugarRecord {

    private String actionId;
    private long timeOfPresentation;
    private long sentToServerTimestamp;
    private long sentToServerTimestamp2;
    private long createdAt;
    private int trigger;
    private String pid;
    private boolean keepForever;

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public long getTimeOfPresentation() {
        return timeOfPresentation;
    }

    public void setTimeOfPresentation(long timeOfPresentation) {
        this.timeOfPresentation = timeOfPresentation;
    }

    /**
     * * Do not use after 1.0.1. There was a bug in volley that prevented the data from being sent to the server correctly.
     *
     * @return the deprecated timestamp
     */
    @Deprecated
    public long getSentToServerTimestamp() {
        return sentToServerTimestamp;
    }

    /**
     * Do not use after 1.0.1. There was a bug in volley that prevented the data from being sent to the server correctly.
     *
     * @param sentToServerTimestamp the deprecated timestamp
     */
    @Deprecated()
    public void setSentToServerTimestamp(long sentToServerTimestamp) {
        this.sentToServerTimestamp = sentToServerTimestamp;
    }

    public void setSentToServerTimestamp2(long sentToServerTimestamp2) {
        this.sentToServerTimestamp2 = sentToServerTimestamp2;
    }

    public long getSentToServerTimestamp2() {
        return sentToServerTimestamp2;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

    public int getTrigger() {
        return trigger;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setKeepForever(boolean keepForever) {
        this.keepForever = keepForever;
    }

    public boolean getKeepForever() {
        return keepForever;
    }

    /**
     * Gets SugarAction object.
     *
     * @param beaconEvent - The beacon object.
     * @param clock - Clock class object.
     *
     * @return - Returns a SugarAction class object.
     */
    public static SugarAction from(BeaconEvent beaconEvent, Clock clock) {
        SugarAction value = new SugarAction();
        value.setActionId(beaconEvent.getAction().getUuid().toString());
        value.setTimeOfPresentation(beaconEvent.getPresentationTime());
        value.setSentToServerTimestamp2(SugarFields.Action.NO_DATE);
        value.setCreatedAt(clock.now());
        value.setTrigger(beaconEvent.trigger);

        if (beaconEvent.getBeaconId() != null) {
            value.setPid(beaconEvent.getBeaconId().getBid());
        }
        if (beaconEvent.sendOnlyOnce || beaconEvent.getSuppressionTimeMillis() > 0){
            value.setKeepForever(true);
        }

        return value;
    }

    /*
    public static RealmResults<SugarAction> notSentScans(){
        return Select.from(SugarAction.class);//.where(SugarAction.class).eql(SugarFields.Action.sentToServerTimestamp2, SugarFields.Scan.NO_DATE);
    }*/

    public static boolean getCountForSuppressionTime(long lastAllowedPresentationTime, UUID actionUUID) {
        Select select = Select.from(SugarAction.class)
        .where(SugarFields.Action.actionId + "= ? AND " + SugarFields.Action.timeOfPresentation + ">= ?", new String[] {actionUUID.toString(), Long.toString(lastAllowedPresentationTime)});

        keepForever(select);
        return select.count() > 0;
    }

    /**
     * Keep forever ie. save!
     *
     * @param sugarActionSelect - The select statement you would like to save.
     */
    private static void keepForever(Select<SugarAction> sugarActionSelect) {
        if (sugarActionSelect.count() > 0) {
            List<SugarAction> values = sugarActionSelect.list();
            for (int i = 0; i < values.size(); i++) {
                values.get(i).setKeepForever(true);
            }
            SugarRecord.saveInTx(values);
        }
    }

    /**
     * Get the count for only once suppression.
     *
     * @param actionUUID - The beacon action UUID.
     *
     * @return - Select class object.
     */
    public static boolean getCountForShowOnlyOnceSuppression(UUID actionUUID){
        Select select = Select.from(SugarAction.class)
                .where(SugarFields.Action.actionId + "= ?", new String[] {actionUUID.toString()});
        keepForever(select);
        return select.count() > 0;
    }
}
