package com.sensorberg.di;

import android.app.Application;

import com.sensorberg.sdk.InternalApplicationBootstrapper;
import com.sensorberg.sdk.internal.AndroidPlatform;
import com.sensorberg.sdk.internal.PendingIntentStorage;
import com.sensorberg.sdk.internal.PersistentIntegerCounter;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.sensorberg.sdk.settings.Settings;

import javax.inject.Singleton;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Singleton
@dagger.Component(modules = {ProvidersModule.class})
public interface Component {

    void inject(InternalApplicationBootstrapper bootstrapper);

    void inject(Settings beaconActionHistoryPublisher);

    void inject(PendingIntentStorage pendingIntentStorage);

    void inject(PersistentIntegerCounter persistentIntegerCounter);

    void inject(AndroidPlatform androidPlatform);

    void inject(BeaconActionHistoryPublisher beaconActionHistoryPublisher);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class Initializer {

        public static Component init(Application app) {
            return DaggerComponent.builder()
                    .providersModule(new ProvidersModule(app))
                    .build();
        }
    }
}