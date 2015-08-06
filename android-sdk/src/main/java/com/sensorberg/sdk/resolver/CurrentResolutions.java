package com.sensorberg.sdk.resolver;

import com.sensorberg.sdk.scanner.ScanEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CurrentResolutions implements Iterable<Resolution> {
    private final List<Resolution> resolutions = new ArrayList<>();

    public boolean contains(Resolution resolution) {
        return resolutions.contains(resolution);
    }

    public void add(Resolution resolution) {
        resolutions.add(resolution);
    }

    public void remove(Resolution resolution) {
        resolutions.remove(resolution);
    }

    @Override
    public Iterator<Resolution> iterator() {
        return resolutions.iterator();
    }

    public Resolution get(ScanEvent scanEvent) {
        for (Resolution resolution : this) {
            if (resolution.configuration.getScanEvent().equals(scanEvent)){
                return resolution;
            }
        }
        return null;
    }
}
