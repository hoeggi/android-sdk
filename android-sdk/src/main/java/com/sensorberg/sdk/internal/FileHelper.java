package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.scanner.EventEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class FileHelper {
    public static HashMap<BeaconId, EventEntry> readFile(File file) {
        HashMap<BeaconId, EventEntry> value;
        try {
            //noinspection unchecked if it fails, see catch block
            value = (HashMap<BeaconId, EventEntry>) getContentsOfFileOrNull(file);
            if (value == null){
                return new HashMap<>();
            }
        } catch (ClassCastException e){
            return new HashMap<>();
        }
        return value;
    }

    public static Object getContentsOfFileOrNull(File file){
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        }  catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        finally {
            Closeables.close(fis);
            Closeables.close(ois);
        }
    }

    public static boolean write(Serializable object, File file) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Closeables.close(fos);
            Closeables.close(oos);
        }
        return false;
    }
}
