package com.sensorberg.sdk.model.sugarorm;

import com.orm.SugarRecord;

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
}
