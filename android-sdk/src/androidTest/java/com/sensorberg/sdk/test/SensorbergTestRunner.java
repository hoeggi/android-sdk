package com.sensorberg.sdk.test;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorbergorm.SugarContext;
import com.sensorberg.sdk.internal.URLFactory;

import net.danlew.android.joda.JodaTimeAndroid;
import net.danlew.android.joda.TimeZoneChangedReceiver;

import org.joda.time.DateTimeZone;
import org.junit.Before;

import java.util.TimeZone;

import util.TestDatabaseHelper;

public class SensorbergTestRunner extends android.support.test.runner.AndroidJUnitRunner {

    @Override
    public void onCreate(Bundle arguments) {
        MultiDex.install(getTargetContext());

        super.onCreate(arguments);
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());

        if (com.sensorberg.sdk.BuildConfig.RESOLVER_URL != null) {
            URLFactory.setLayoutURL(com.sensorberg.sdk.BuildConfig.RESOLVER_URL);
        }
        JodaTimeAndroid.init(getContext());
        TestDatabaseHelper testDatabaseHelper = new TestDatabaseHelper(getContext(), "sensorbergORM.db", null, 1);
        testDatabaseHelper.getWritableDatabase();
//        TimeZoneChangedReceiver receiver = new TimeZoneChangedReceiver();
//        Intent timeZoneIntent = new Intent();
//        timeZoneIntent.putExtra("time-zone", "GMT+00:00");
//        receiver.onReceive(getContext(), timeZoneIntent);

        DateTimeZone e = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+01:00"));
        DateTimeZone.setDefault(e);
    }
}