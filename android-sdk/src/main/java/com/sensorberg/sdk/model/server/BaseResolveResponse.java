package com.sensorberg.sdk.model.server;

import java.util.Collections;
import java.util.List;

public class BaseResolveResponse  {

    private List<String> accountProximityUUIDs = Collections.emptyList();

    @SuppressWarnings("unchecked")
    public List<String> getAccountProximityUUIDs() {
        return accountProximityUUIDs == null ? Collections.EMPTY_LIST : accountProximityUUIDs;
    }

    protected BaseResolveResponse(List<String> accountProximityUUIDs) {
        this.accountProximityUUIDs = accountProximityUUIDs;
    }
}
