package com.sensorberg.sdk.testUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.sensorberg.sdk.InternalApplicationBootstrapper;
import com.sensorberg.sdk.action.ActionFactory;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import org.fest.assertions.api.Assertions;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import util.TestConstants;
import util.Utils;

public class ResolveResponsePerformanceTest  extends AndroidTestCase {

    InternalApplicationBootstrapper tested;
    private NotificationManager notificationManager;

    private static final JsonObject ANY_IN_APP_JSON = new JsonObject();
    static {
        try {
            ANY_IN_APP_JSON.addProperty("url", "sensorberg://");
        } catch (Exception e) {
        }
    }

    private static final String ANY_UUID = UUID.randomUUID().toString();
    private static final String ANOTHER_UUID = UUID.randomUUID().toString();


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void /*test_*/a_big_amount_of_events() throws Exception {


        Log.wtf("MeasuredTime", "---------------------------------------------");
        Log.wtf("MeasuredTime", "---------------------------------------------");
        Log.wtf("MeasuredTimeCSV","items;JSONBytes;matchingknown;matchingunknown");

//        int itemcount[] = {100,200,400,800,1600,3200,6400,12800,25600,51200};
        int itemcount[] = {100,200,400,800,1600,3200,6400,12800,25600,51200};
        long bytesInJson[] = new long[itemcount.length];
        long bytesInJsonGzipped[] = new long[itemcount.length];
        long matchingKnown[] = new long[itemcount.length];
        long matchingUnknown[] = new long[itemcount.length];
        long jsonParseTimes[] = new long[itemcount.length];

        Gson gson = new GsonBuilder().create();

        for ( int j = 0 ; j < itemcount.length ; j++) {
            try {
                ArrayList<ResolveAction> actions = new ArrayList<>();

                for (int i = 0; i < itemcount[j]; i++) {
                    actions.add(getNewRandomAction());
                }
                actions.add(getNewAction(ScanEventType.ENTRY, TestConstants.ANY_BEACON_ID));

                ResolveResponse resolveResponse = new ResolveResponse.Builder().withActions(actions).build();

                String jsonString = gson.toJson(resolveResponse);
                long jsonCharSize = jsonString.getBytes().length;
                bytesInJson[j] = jsonCharSize;


                long gzipBytesSize = gzipByteSize(jsonString);
                bytesInJsonGzipped[j] = gzipBytesSize;


                //simulate the entry
                ScanEvent validScanEvent = new ScanEvent.Builder()
                        .withBeaconId(TestConstants.ANY_BEACON_ID)
                        .withEventMask(ScanEventType.ENTRY.getMask())
                        .build();
                ScanEvent invalidScanEvent = new ScanEvent.Builder()
                        .withBeaconId(TestConstants.ANY_OTHER_BEACON_ID)
                        .withEventMask(ScanEventType.ENTRY.getMask())
                        .build();

                File serializeableFile = new File(getContext().getCacheDir(), "resolveResponse.ser");
                if (serializeableFile.exists()){
                    assertTrue("Old file should be deleted", serializeableFile.delete());
                }

                long nanoStartWriteSerializeable = System.nanoTime();

                try (FileOutputStream fos = new FileOutputStream(serializeableFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos);
                     ObjectOutputStream oos = new ObjectOutputStream(bos);) {
                    oos.writeObject(resolveResponse);
                }

                long nanoEndWriteSerializeable = System.nanoTime();

                ResolveResponse deserialized;
                try (FileInputStream fis = new FileInputStream(serializeableFile);
                     BufferedInputStream bis = new BufferedInputStream(fis);
                     ObjectInputStream ois = new ObjectInputStream(bis)) {
                    deserialized = (ResolveResponse) ois.readObject();
                }
                long nanoEndDeserialize = System.nanoTime();


                Assertions.assertThat(deserialized).isEqualsToByComparingFields(resolveResponse);

//                File diskFolder = new File(getContext().getCacheDir(), "testrun-Cache" + System.currentTimeMillis());
//                DiskBasedCache diskBasedCache = new DiskBasedCache(diskFolder);
//                diskBasedCache.initialize();
//
//                Cache.Entry entry = new Cache.Entry();
//                entry.data = jsonString.getBytes();
//                entry.etag = "1";
//                entry.softTtl = 1000L;
//                entry.ttl = entry.softTtl;
//                entry.serverDate = System.currentTimeMillis();
//                entry.responseHeaders = Collections.emptyMap();
//
//                long startCacheWrite = System.nanoTime();
//                diskBasedCache.put("foo", entry);
//                long endCacheWrite = System.nanoTime();
//                diskBasedCache.get("foo");
//                long endCacheRead = System.nanoTime();
//
//                long nanoStartParse = System.nanoTime();
//                ResolveResponse rParsed = gson.fromJson(jsonString, ResolveResponse.class);
//                long nanoEndParse = System.nanoTime();
//                resolveResponse.resolve(validScanEvent, 0);
//                long nanoEndFirstEntry = System.nanoTime();
//                resolveResponse.resolve(invalidScanEvent, 0);
//                long nanoEndTest = System.nanoTime();
//
//
//                long jsonParseTimeMillis = TimeUnit.NANOSECONDS.toMillis(nanoEndParse - nanoStartParse);
//                long matchingExistingInMillis = TimeUnit.NANOSECONDS.toMillis(nanoEndFirstEntry - nanoEndParse);
//                long matchingNonExistentInMillis = TimeUnit.NANOSECONDS.toMillis(nanoEndTest - nanoEndFirstEntry);
//
//                long serializeTime = TimeUnit.NANOSECONDS.toMillis(nanoEndWriteSerializeable - nanoStartWriteSerializeable);
//                long deserializeTime = TimeUnit.NANOSECONDS.toMillis(nanoEndDeserialize - nanoEndWriteSerializeable);
//
//                long diskCacheSerializeTime = TimeUnit.NANOSECONDS.toMillis(endCacheWrite - startCacheWrite);
//                long diskCacheDeSerializeTime = TimeUnit.NANOSECONDS.toMillis(endCacheRead - endCacheWrite);
//
//
//                jsonParseTimes[j] = jsonParseTimeMillis;
//
//                matchingKnown[j] = matchingExistingInMillis;
//                matchingUnknown[j] = matchingNonExistentInMillis;


                Log.wtf("MeasuredTimeCSV", itemcount[j] + ";" + bytesInJson[j] + ";" + matchingKnown[j] + ";" + matchingUnknown[j]);
                Log.wtf("MeasuredTime", "---------------------------------------------");
                Log.wtf("MeasuredTime", "Testing over : " + itemcount[j] + " items.");
                Log.wtf("MeasuredTime", "JSON Size : " + jsonCharSize + " bytes.");
                Log.wtf("MeasuredTime", "JSON Size Gzipped : " + gzipBytesSize + " bytes.");
//                Log.wtf("MeasuredTime", "Actual gson JSON parsing of the response : " + jsonParseTimeMillis + " Millis.");
//
//                Log.wtf("MeasuredTime", "Actual writing it to disk as serializeable: " + serializeTime + " Millis.");
//                Log.wtf("MeasuredTime", "Actual reading from disk as serializeable: " + deserializeTime + " Millis.");
//
//                Log.wtf("MeasuredTime", "Actual writing it to volley diskcache: " + diskCacheSerializeTime + " Millis.");
//                Log.wtf("MeasuredTime", "Actual reading from volley diskcache: " + diskCacheDeSerializeTime + " Millis.");
//
//
//                Log.wtf("MeasuredTime", "Actual matching of a known Beacon Scan event : " + matchingExistingInMillis + " Millis.");
//                Log.wtf("MeasuredTime", "Actual matching of an unknown Beacon Scan event : " + matchingNonExistentInMillis + " Millis.");
                Log.wtf("MeasuredTime", "---------------------------------------------");
            }catch (OutOfMemoryError e){
                Log.wtf("MeasuredTime", e.getLocalizedMessage());
                matchingKnown[j] = Long.MAX_VALUE;
                matchingUnknown[j] = Long.MAX_VALUE;
            }
        }

        Log.wtf("MeasuredTime","items;JSONBytes;JSONBytesGZipped;jsonParseTime,matchingknown;matchingunknown");
        for (int k = 0; k < itemcount.length ; k++) {
            Log.wtf("MeasuredTime", itemcount[k] + ";" + bytesInJson[k] + ";" + bytesInJsonGzipped[k] + ";"  + jsonParseTimes[k] + ";" + matchingKnown[k] + ";" + matchingUnknown[k]);
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private long gzipByteSize(String jsonString) {
        try (ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
             GZIPOutputStream zos = new GZIPOutputStream(rstBao)) {
            zos.write(jsonString.getBytes());
            zos.finish();
            byte[] bytes = rstBao.toByteArray();
            return bytes.length;
        } catch (IOException e) {
            return Long.MAX_VALUE;
        }
    }

    private ResolveAction getNewAction(ScanEventType event, BeaconId beaconId) {
        return new ResolveAction.Builder()
                .withBeacons(Arrays.asList(beaconId.getBid()))
                .withType(ActionFactory.ServerType.IN_APP)
                .withTrigger(event.getMask())
                .withUuid(ANY_UUID)
                .withContent(ANY_IN_APP_JSON)
                .build();
    }

    private ResolveAction getNewRandomAction(){

        BeaconId bid;
        do {
            bid = Utils.getRandomBeaconId();
        } while (bid.equals(TestConstants.ANY_OTHER_BEACON_ID));

        return new ResolveAction.Builder()
                .withBeacons(Arrays.asList(bid.getBid()))
                .withType(ActionFactory.ServerType.IN_APP)
                .withTrigger(ScanEventType.ENTRY.getMask())
                .withUuid(ANY_UUID)
                .withContent(ANY_IN_APP_JSON)
                .build();
    }
}
