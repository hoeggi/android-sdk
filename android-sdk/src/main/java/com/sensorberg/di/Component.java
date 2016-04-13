package com.sensorberg.di;

import com.sensorberg.sdk.InternalApplicationBootstrapper;
import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.internal.AndroidPlatform;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.PendingIntentStorage;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.sensorberg.sdk.scanner.BeaconMap;
import com.sensorberg.sdk.settings.Settings;

import android.app.Application;

import javax.inject.Singleton;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Singleton
@dagger.Component(modules = {ProvidersModule.class})
public interface Component {

    void inject(InternalApplicationBootstrapper bootstrapper);

    void inject(Settings beaconActionHistoryPublisher);

    void inject(PendingIntentStorage pendingIntentStorage);

    void inject(AndroidPlatform androidPlatform);

    void inject(BeaconActionHistoryPublisher beaconActionHistoryPublisher);

    void inject(OkHttpClientTransport okHttpClientTransport);

    void inject(SensorbergService sensorbergService);

    void inject(BeaconMap beaconMap);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class Initializer {

        public static Component init(Application app) {
            return DaggerComponent.builder()
                    .providersModule(new ProvidersModule(app))
                    .build();
        }
    }
}