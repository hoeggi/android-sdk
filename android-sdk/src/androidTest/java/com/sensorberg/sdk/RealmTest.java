package com.sensorberg.sdk;

import android.support.test.runner.AndroidJUnit4;

import com.sensorberg.sdk.model.realm.RealmAction;
import com.sensorberg.sdk.model.realm.RealmFields;
import com.sensorberg.sdk.model.realm.RealmScan;
import com.sensorberg.sdk.realm.migrations.Version0to1Migration;

import org.apache.commons.io.IOUtils;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import io.realm.Realm;
import io.realm.exceptions.RealmMigrationNeededException;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getTargetContext;

@RunWith(AndroidJUnit4.class)
public class RealmTest  {

    private File realmFile;
    private File folderForRealmFile;

    @Before
    public void setup(){
        folderForRealmFile = getContext().getCacheDir();
        realmFile = new File(folderForRealmFile, Realm.DEFAULT_REALM_NAME);

        if (realmFile.exists()){
            realmFile.delete();
        }
    }


    @Test
    public void realmShouldBeCreated(){

        Assertions.assertThat(realmFile).doesNotExist();

        Realm realm = Realm.getInstance(folderForRealmFile);
        realm.beginTransaction();
        RealmScan scan = realm.createObject(RealmScan.class);
        scan.setCreatedAt(1);
        scan.setProximityMajor(1337);
        scan.setProximityMinor(1337);


        RealmAction action = realm.createObject(RealmAction.class);
        action.setCreatedAt(1);
        action.setSentToServerTimestamp(2);
        action.setTrigger(1);
        action.setActionId(UUID.randomUUID().toString());

        realm.commitTransaction();
        realm.close();

        File realmFileAfter = new File(folderForRealmFile, Realm.DEFAULT_REALM_NAME);
        Assertions.assertThat(realmFileAfter).exists();


    }

    @Test
    public void shouldBeCreatedFromRawResources() throws IOException {
        InputStream resource = getContext().getResources().openRawResource(com.sensorberg.sdk.test.R.raw.default_version_0);

        realmFile.createNewFile();
        IOUtils.copy(resource, new FileOutputStream(realmFile));

        try{
            Realm realm = Realm.getInstance(folderForRealmFile);
            Assertions.assertThat(realm.allObjects(RealmScan.class)).hasSize(1);
            Assertions.assertThat(realm.allObjects(RealmAction.class)).hasSize(1);
        } catch (RealmMigrationNeededException e){
            //this is what we expect
        } catch (Exception e){
            Assertions.fail("there should only be a RealmMigrationNeededException");
        }
    }

    @Test
    public void shouldMigrate() throws IOException {
        InputStream resource = getContext().getResources().openRawResource(com.sensorberg.sdk.test.R.raw.default_version_0);

        realmFile.delete();
        realmFile.createNewFile();
        IOUtils.copy(resource, new FileOutputStream(realmFile));

        Realm.migrateRealmAtPath(realmFile.getPath(), new Version0to1Migration(), false);

        Realm realm = Realm.getInstance(folderForRealmFile);
        Assertions.assertThat(realm.allObjects(RealmScan.class)).hasSize(1);
        Assertions.assertThat(realm.allObjects(RealmAction.class)).hasSize(1);

        Assertions.assertThat(realm.allObjects(RealmScan.class).first().getSentToServerTimestamp2()).isEqualTo(RealmFields.Scan.NO_DATE);
        Assertions.assertThat(realm.allObjects(RealmAction.class).first().getSentToServerTimestamp2()).isEqualTo(RealmFields.Action.NO_DATE);

    }
}
