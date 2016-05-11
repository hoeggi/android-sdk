package com.sensorberg.sdk.di;

import com.sensorberg.di.Component;
import com.sensorberg.sdk.TheInternalApplicationBootstrapperShould;
import com.sensorberg.sdk.TheInternalBootstrapperIntegration;
import com.sensorberg.sdk.TheSensorbergServiceShould;
import com.sensorberg.sdk.internal.TheIntentSchedulingBeUpdateable;
import com.sensorberg.sdk.internal.TheIntentSchedulingShould;
import com.sensorberg.sdk.internal.http.ApiServiceShould;
import com.sensorberg.sdk.internal.http.HttpStackShouldCacheTheSettings;
import com.sensorberg.sdk.internal.http.TransportShould;
import com.sensorberg.sdk.model.server.ResolveActionTest;
import com.sensorberg.sdk.model.server.TheResolveResponse;
import com.sensorberg.sdk.model.sugar.TheSugarActionObjectShould;
import com.sensorberg.sdk.model.sugar.TheSugarHistoryBodyShould;
import com.sensorberg.sdk.model.sugar.TheSugarScanobjectShould;
import com.sensorberg.sdk.resolver.TheResolveResponseShould;
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

    void inject(TheResolverShould theResolverShould);

    void inject(TheBeaconActionHistoryPublisherShould theBeaconActionHistoryPublisherShould);

    void inject(TheBeconHistorySynchronousIntegrationTest theBeconHistorySynchronousIntegrationTest);

    void inject(TransportShould transportShould);

    void inject(HttpStackShouldCacheTheSettings httpStackShouldCacheTheSettings);

    void inject(TheSettingsShould theSettingsShould);

    void inject(ResolveActionTest resolveActionTest);

    void inject(TheResolveResponse theResolveResponse);

    void inject(TheSugarActionObjectShould theSugarActionObjectShould);

    void inject(TheSugarHistoryBodyShould theSugarHistoryBodyShould);

    void inject(TheSugarScanobjectShould theSugarScanobjectShould);

    void inject(TheResolveResponseShould theResolveResponseShould);

    void inject(ApiServiceShould apiServiceShould);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class Initializer {

        public static TestComponent init(Application app) {
            return DaggerTestComponent.builder()
                    .testProvidersModule(new TestProvidersModule(app))
                    .build();
        }
    }
}