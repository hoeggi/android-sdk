package com.sensorberg.sdk.model.sugarorm;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sensorbergorm.SugarRecord;
import com.sensorbergorm.query.Select;
import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.model.ISO8601TypeAdapter;
import com.sensorberg.sdk.resolver.BeaconEvent;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import com.sensorbergorm.query.Condition;

/**
 * Created by skraynick on 16-03-14.
 */
public class SugarAction extends SugarRecord {

    @Expose
    private String actionId;
    @Expose
    private long timeOfPresentation;
    @Expose
    private long sentToServerTimestamp;
    @Expose
    private long sentToServerTimestamp2;
    @Expose
    private long createdAt;
    @Expose
    private int trigger;
    @Expose
    private String pid;
    @Expose
    private boolean keepForever;

    /**
     * Default constructor as required by SugarORM.
     */
    public SugarAction() {
    }

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

    /**
     * List not sent scans.
     *
     * @return - A list of notSentScans.
     */
    public static List<SugarAction> notSentScans(){
        return Select.from(SugarAction.class)
                .where(Condition.prop(SugarFields.Action.sentToServerTimestamp2Column).eq(SugarFields.Action.NO_DATE))
                .list();
    }


    public static boolean getCountForSuppressionTime(long lastAllowedPresentationTime, UUID actionUUID) {
        Select select = Select.from(SugarAction.class)
                .where(Condition.prop(SugarFields.Action.timeOfPresentationColumn).gtOrEq(lastAllowedPresentationTime))
                .and(Condition.prop(SugarFields.Action.actionIdColumn).eq(actionUUID));

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
                .where(Condition.prop(SugarFields.Action.actionIdColumn).eq(actionUUID));
        keepForever(select);
        return select.count() > 0;
    }

    public static void markAsSent(List<SugarAction> actions, long now, long actionCacheTtl) {
        if (actions.size() > 0) {
            for (int i = actions.size() - 1; i >= 0; i--) {
                actions.get(i).setSentToServerTimestamp2(now);
                actions.get(i).save();
            }
        }
        removeAllOlderThan(now, actionCacheTtl);
    }

    /**
     * Removes all older items as specified by the parameters.
     *
     * @param now - Time Now ? millisecond?
     * @param time - Time in the past.
     */
    public static void removeAllOlderThan(long now, long time) {
        List<SugarAction> actionsToDelete = Select.from(SugarAction.class)
                .where(Condition.prop(SugarFields.Action.createdAtColumn).lt(now - time))
                .and(Condition.prop(SugarFields.Action.keepForeverColumn).eq(false))
                .and(Condition.prop(SugarFields.Action.sentToServerTimestamp2Column).notEq(SugarFields.Action.NO_DATE))
                .list();

        if (actionsToDelete.size() > 0){
            for (int i = actionsToDelete.size() - 1; i >= 0; i--) {
                //delete
                actionsToDelete.get(i).delete();
            }
        }
    }

    public static class SugarActionTypeAdapter extends TypeAdapter<SugarAction> {

        @Override
        public void write(JsonWriter out, SugarAction value) throws IOException {
            out.beginObject();
            out.name("eid").value(value.getActionId());
            out.name("trigger").value(value.getTrigger());
            out.name("pid").value(value.getPid());
            out.name("dt");
            ISO8601TypeAdapter.DATE_ADAPTER.write(out, new Date(value.getTimeOfPresentation()));
            out.endObject();
        }

        @Override
        public SugarAction read(JsonReader in) throws IOException {
            throw new IllegalArgumentException("You must not use this to read a RealmAction");
        }
    }
}
