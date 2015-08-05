package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.internal.FileHelper;
import com.sensorberg.sdk.model.BeaconId;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BeaconMap {

    public interface Filter {
        boolean filter(EventEntry beaconEntry, BeaconId beaconId);
    }

    private final HashMap<BeaconId, EventEntry> storage;
    private final File file;

    public BeaconMap(File fileForPersistance) {
        this.file = fileForPersistance;
        if (fileForPersistance != null) {
            storage = FileHelper.readFile(fileForPersistance);

        } else {
            storage = new HashMap<>();
        }
    }

    public int size() {
        return storage.size();
    }

    public void clear() {
        storage.clear();
        deleteFile();
    }

    private void deleteFile() {
        if(file != null) {
            file.delete();
        }
    }

    public EventEntry get(BeaconId beaconId) {
        return storage.get(beaconId);
    }

    public void put(BeaconId beaconId, EventEntry entry) {
        storage.put(beaconId, entry);
        persist();
    }

    public void filter(Filter filter) {
        boolean modified = false;
        Iterator<Map.Entry<BeaconId, EventEntry>> iterator = storage.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<BeaconId, EventEntry> enteredBeacon = iterator.next();
            EventEntry beaconEntry = enteredBeacon.getValue();
            BeaconId beaconId = enteredBeacon.getKey();
            if(filter.filter(beaconEntry, beaconId)){
                iterator.remove();
                modified = true;
            }
        }
        if(modified){
            persist();
        }
    }

    private void persist() {
        if (file != null) {
            FileHelper.write(storage, file);
        }
    }
}
