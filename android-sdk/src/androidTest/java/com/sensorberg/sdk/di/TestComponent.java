package com.sensorberg.sdk.di;

import com.sensorberg.di.Component;
import com.sensorberg.sdk.TheInternalApplicationBootstrapperShould;
import com.sensorberg.sdk.TheInternalBootstrapperIntegration;
import com.sensorberg.sdk.TheSensorbergServiceShould;
import com.sensorberg.sdk.internal.TheIntentSchedulingBeUpdateable;
import com.sensorberg.sdk.internal.TheIntentSchedulingShould;
import com.sensorberg.sdk.internal.http.OkHttpClientTransportWithRetries;
import com.sensorberg.sdk.internal.http.OkHttpUserAgentTest;
import com.sensorberg.sdk.internal.http.OkVolleyShouldCacheTheSettings;
import com.sensorberg.sdk.internal.http.OkVolleyShouldCacheTheSettingsWithEtags;
import com.sensorberg.sdk.internal.http.TransportShould;
import com.sensorberg.sdk.model.realm.TheRealmActionObjectShould;
import com.sensorberg.sdk.resolver.TheResolverShould;
import com.sensorberg.sdk.scanner.ScannerWithLongScanTime;
import com.sensorberg.sdk.scanner.TheBackgroundScannerShould;
import com.sensorberg.sdk.scanner.TheBeaconActionHistoryPublisherIntegrationShould;
import com.sensorberg.sdk.scanner.TheBeaconActionHistoryPublisherShould;
import com.sensorberg.sdk.scanner.TheBeaconMapShould;
import com.sensorberg.sdk.scanner.TheBeconHistorySynchronousIntegrationTest;
import com.sensorberg.sdk.scanner.TheBluetoothChangesShould;
import com.sensorberg.sdk.scanner.TheDefaultScanner;
import com.sensorberg.sdk.scanner.TheDefaultScannerSetupShould;
import com.sensorberg.sdk.scanner.TheForegroundScannerShould;
import com.sensorberg.sdk.scanner.TheScannerWithRestoredStateShould;
import com.sensorberg.sdk.scanner.TheScannerWithTimeoutsShould;
import com.sensorberg.sdk.scanner.TheScannerWithoutPausesShould;
import com.sensorberg.sdk.service.TheServiceConfiguration;
import com.sensorberg.sdk.settings.TheSettingsShould;

import android.app.Application;

import javax.inject.Singleton;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Singleton
@dagger.Component(modules = {TestProvidersModule.class})
public interface TestComponent extends Component {

    void inject(com.sensorberg.sdk.testUtils.TestPlatform testPlatform);

    void inject(ScannerWithLongScanTime scannerWithLongScanTime);

    void inject(TheScannerWithRestoredStateShould theScannerWithRestoredStateShould);

    void inject(TheForegroundScannerShould theForegroundScannerShould);

    void inject(TheBluetoothChangesShould theBluetoothChangesShould);

    void inject(TheDefaultScannerSetupShould theDefaultScannerSetupShould);

    void inject(TheScannerWithTimeoutsShould theScannerWithTimeoutsShould);

    void inject(TheScannerWithoutPausesShould theScannerWithoutPausesShould);

    void inject(TheSensorbergServiceShould theSensorbergServiceShould);

    void inject(TheDefaultScanner theDefaultScanner);

    void inject(TheBackgroundScannerShould theBackgroundScannerShould);

    void inject(TheBeaconMapShould theBeaconMapShould);

    void inject(TheServiceConfiguration theServiceConfiguration);

    void inject(TheInternalApplicationBootstrapperShould theInternalApplicationBootstrapperShould);

    void inject(TheInternalBootstrapperIntegration theInternalBootstrapperIntegration);

    void inject(TheIntentSchedulingBeUpdateable theIntentSchedulingBeUpdateable);

    void inject(TheIntentSchedulingShould theIntentSchedulingShould);

    void inject(TheBeaconActionHistoryPublisherIntegrationShould theBeaconActionHistoryPublisherIntegrationShould);

    void inject(TheRealmActionObjectShould theRealmActionObjectShould);

    void inject(TheResolverShould theResolverShould);

    void inject(TheBeaconActionHistoryPublisherShould theBeaconActionHistoryPublisherShould);

    void inject(TheBeconHistorySynchronousIntegrationTest theBeconHistorySynchronousIntegrationTest);

    void inject(TransportShould transportShould);

    void inject(OkHttpClientTransportWithRetries okHttpClientTransportWithRetries);

    void inject(OkHttpUserAgentTest okHttpUserAgentTest);

    void inject(OkVolleyShouldCacheTheSettings okVolleyShouldCacheTheSettings);

    void inject(OkVolleyShouldCacheTheSettingsWithEtags okVolleyShouldCacheTheSettingsWithEtags);

    void inject(TheSettingsShould theSettingsShould);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class Initializer {

        public static TestComponent init(Application app) {
            return DaggerTestComponent.builder()
                    .testProvidersModule(new TestProvidersModule(app))
                    .build();
        }
    }

}