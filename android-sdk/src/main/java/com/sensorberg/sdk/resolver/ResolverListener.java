package com.sensorberg.sdk.resolver;

import java.util.List;

public interface ResolverListener {

    ResolverListener NONE = new ResolverListener() {
        @Override
        public void onResolutionFailed(Resolution resolution, Throwable cause) {

        }

        @Override
        public void onResolutionsFinished(List<BeaconEvent> events) {

        }
    };

    void onResolutionFailed(Resolution resolution, Throwable cause);

    void onResolutionsFinished(List<BeaconEvent> events);
}
