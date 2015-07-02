package com.sensorberg.sdk.realm.migrations;

import com.sensorberg.sdk.model.realm.RealmAction;
import com.sensorberg.sdk.model.realm.RealmFields;
import com.sensorberg.sdk.model.realm.RealmScan;

import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;

public class Version0to1Migration  implements RealmMigration{
    @Override
    public long execute(Realm realm, long version) {
        if (version == 0){
            {
                Table scansTable = realm.getTable(RealmScan.class);
                long scansIndex = scansTable.addColumn(ColumnType.INTEGER, RealmFields.Scan.sentToServerTimestamp2);
                for (int i = 0; i < scansTable.size(); i++) {
                    scansTable.setLong(scansIndex, i, RealmFields.Scan.NO_DATE);
                }
            }
            {
                Table actionsTable = realm.getTable(RealmAction.class);
                long actionsIndex = actionsTable.addColumn(ColumnType.INTEGER, RealmFields.Scan.sentToServerTimestamp2);
                for (int i = 0; i < actionsTable.size(); i++) {
                    actionsTable.setLong(actionsIndex, i, RealmFields.Scan.NO_DATE);
                }
            }

            version++;
        }
        return version;
    }
}
