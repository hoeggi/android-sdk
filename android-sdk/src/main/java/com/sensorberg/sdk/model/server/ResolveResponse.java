package com.sensorberg.sdk.model.server;

import com.google.gson.annotations.SerializedName;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.utils.ListUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResolveResponse extends BaseResolveResponse implements Serializable {

    static final long serialVersionUID = 1L;

    private List<ResolveAction> actions = Collections.emptyList();
    private List<ResolveAction> instantActions = Collections.emptyList();

    @SerializedName("reportTrigger")
    public Long reportTriggerSeconds;

    public List<ResolveAction> resolve(ScanEvent scanEvent, long now) {
        ArrayList<ResolveAction> beaconEvents = new ArrayList<>();
        beaconEvents.addAll(getActionsFromLayout(scanEvent, now));
        beaconEvents.addAll(getInstantActions());
        return beaconEvents;
    }

    /**
     * used internally to create all the @{ResolveAction} from the @{actions} array
     * @return all matching BeaconEvents
     * @param scanEvent used to match
     */
    private List<ResolveAction> getActionsFromLayout(final ScanEvent scanEvent, final long now) {
        if (actions == null){
            return Collections.emptyList();
        }
        return ListUtils.filter(actions, new ListUtils.Filter<ResolveAction>() {
            @Override
            public boolean matches(ResolveAction resolveAction) {
                boolean matchTrigger = resolveAction.matchTrigger(scanEvent.getEventMask());
                if (matchTrigger) {
                    boolean matchBeacon = resolveAction.containsBeacon(scanEvent.getBeaconId());
                    if (matchBeacon){
                        return resolveAction.isValidNow(now);
                    }
                }
                return false;
            }
        });
    }

    /**
     *
     * @return all instantActions based on the @{instantAction} ResolveActionsArray.
     */
    public List<ResolveAction> getInstantActions() {
        if (instantActions == null){
            return Collections.emptyList();
        }
        return ListUtils.filter(instantActions, new ListUtils.Filter<ResolveAction>() {
            @Override
            public boolean matches(ResolveAction object) {
                return true;
            }
        });
    }

    public List<BeaconEvent> getInstantActionsAsBeaconEvent() {
        return ListUtils.map(getInstantActions(), ResolveAction.BEACON_EVENT_MAPPER);
    }


    private ResolveResponse(List<String> accountProximityUUIDs, List<ResolveAction> actions, List<ResolveAction> instantActions) {
        super(accountProximityUUIDs);
        this.actions = actions;
        this.instantActions = instantActions;
    }

    public static class Builder {
        List<String> accountProximityUUIDs = Collections.emptyList();
        List<ResolveAction> actions = Collections.emptyList();
        List<ResolveAction> instantActions = Collections.emptyList();

        public Builder() {
        }

        public Builder withAccountProximityUUIDs(List<String> accountProximityUUIDs) {
            this.accountProximityUUIDs = accountProximityUUIDs;
            return this;
        }

        public Builder withActions(List<ResolveAction> actions) {
            this.actions = actions;
            return this;
        }

        public Builder withInstantActions(List<ResolveAction> instantActions) {
            this.instantActions = instantActions;
            return this;
        }


        public ResolveResponse build() {
            return new ResolveResponse(accountProximityUUIDs, actions, instantActions);
        }
    }
}
