package com.sensorberg.sdk.model.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.action.ActionFactory;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.utils.ListUtils;
import com.sensorberg.utils.UUIDUtils;

import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.ToString;

@SuppressWarnings("WeakerAccess")
@ToString
public class ResolveAction implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final ListUtils.Mapper<ResolveAction, BeaconEvent> BEACON_EVENT_MAPPER = new ListUtils.Mapper<ResolveAction, BeaconEvent>() {
        public BeaconEvent map(ResolveAction resolveAction) {
            try {
                Action action = ActionFactory
                        .getAction(resolveAction.type, resolveAction.content, UUID.fromString(UUIDUtils.addUuidDashes(resolveAction.eid)),
                                resolveAction.delay * Constants.Time.ONE_SECOND);
                if (action == null) {
                    return null;
                }
                return new BeaconEvent.Builder()
                        .withAction(action)
                        .withSuppressionTime(resolveAction.suppressionTime * Constants.Time.ONE_SECOND)
                        .withSendOnlyOnce(resolveAction.sendOnlyOnce)
                        .withDeliverAtDate(resolveAction.deliverAt)
                        .withTrigger(resolveAction.trigger)
                        .build();
            } catch (JSONException e) {
                return null;
            }
        }
    };

    @Expose
    public String eid;

    @Expose
    public int trigger;

    @Expose
    public int type;

    @Expose
    public String name;

    @Expose
    public List<String> beacons;

    @Expose
    public long suppressionTime; //in seconds

    @Expose
    public boolean sendOnlyOnce;

    @Expose
    public long delay;

    @Expose
    public boolean reportImmediately;

    @Expose
    public JsonObject content;

    @Expose
    public List<Timeframe> timeframes;

    @Expose
    public Date deliverAt;

    @SuppressWarnings("WeakerAccess")
    public ResolveAction(String uuid, int trigger, int type, String name, List<String> beacons, long suppressionTime, long delay,
            boolean reportImmediately, JsonObject content, Date deliverAt) {
        this.eid = uuid;
        this.trigger = trigger;
        this.type = type;
        this.name = name;
        this.beacons = beacons;
        this.suppressionTime = suppressionTime;
        this.delay = delay;
        this.reportImmediately = reportImmediately;
        this.content = content;
        this.deliverAt = deliverAt;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(eid);
        out.writeInt(trigger);
        out.writeInt(type);
        out.writeObject(name);
        out.writeObject(beacons);
        out.writeLong(suppressionTime);
        out.writeLong(delay);
        out.writeBoolean(reportImmediately);
        out.writeObject(content.toString());
        out.writeObject(deliverAt);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException, JSONException {
        eid = (String) in.readObject();
        trigger = in.readInt();
        type = in.readInt();
        name = (String) in.readObject();
        //noinspection unchecked -> see writeObject
        beacons = (List<String>) in.readObject();
        suppressionTime = in.readLong();
        delay = in.readLong();
        reportImmediately = in.readBoolean();
        String jsonString = (String) in.readObject();
        if (jsonString != null) {
            JsonParser parser = new JsonParser();
            content = parser.parse(jsonString).getAsJsonObject();
        }
        deliverAt = (Date) in.readObject();
    }

    public boolean matchTrigger(int eventMask) {
        return (eventMask & trigger) == eventMask;
    }

    public boolean containsBeacon(BeaconId beaconId) {
        final String scanEventBid = beaconId.getBid();
        for (String bid : beacons) {
            boolean matchBid = scanEventBid.equalsIgnoreCase(bid);
            if (matchBid) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidNow(long now) {
        if (timeframes == null || timeframes.isEmpty()) {
            return true;
        }
        for (Timeframe timeframe : timeframes) {
            boolean valid = timeframe.valid(now);
            if (valid) {
                return true;
            }
        }
        return false;
    }

    public static class Builder {

        public String uuid = UUID.randomUUID().toString();

        public int trigger;

        public int type;

        public String name;

        public List<String> beacons;

        public long suppressionTime;

        public long delay;

        public boolean reportImmediately;

        public JsonObject content;

        private Date deliverAt;

        public Builder() {
        }

        public Builder withDeliverAt(Date deliverAt) {
            this.deliverAt = deliverAt;
            return this;
        }

        public Builder withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withTrigger(int trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder withType(int type) {
            this.type = type;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withBeacons(List<String> beacons) {
            this.beacons = beacons;
            return this;
        }

        public Builder withSuppressionTime(long suppressionTime) {
            this.suppressionTime = suppressionTime;
            return this;
        }

        public Builder withDelay(long delay) {
            this.delay = delay;
            return this;
        }

        public Builder withReportImmediately(boolean reportImmediately) {
            this.reportImmediately = reportImmediately;
            return this;
        }

        public Builder withContent(JsonObject content) {
            this.content = content;
            return this;
        }

        public ResolveAction build() {
            return new ResolveAction(uuid, trigger, type, name, beacons, suppressionTime, delay, reportImmediately, content, deliverAt);
        }
    }
}
