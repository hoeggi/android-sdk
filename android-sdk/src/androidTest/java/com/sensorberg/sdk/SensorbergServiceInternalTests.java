package com.sensorberg.sdk;

import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.URLFactory;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class SensorbergServiceInternalTests {

    private static final String NEW_API_TOKEN = "SOME_NEW_API_TOKEN";

    @Inject
    FileManager fileManager;

    @Inject
    ServiceScheduler serviceScheduler;

    @Inject
    @Named("realHandlerManager")
    HandlerManager handlerManager;

    @Inject
    @Named("realClock")
    Clock clock;

    @Inject
    @Named("androidBluetoothPlatform")
    BluetoothPlatform bluetoothPlatform;

    @Inject
    @Named("realTransport")
    Transport transport;

    private SensorbergService tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        tested = spy(new SensorbergService());
        tested.onCreate();
        fileManager = spy(fileManager);
        tested.fileManager = fileManager;
        tested.transport = Mockito.mock(Transport.class);

        Intent startIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        startIntent.putExtra(SensorbergServiceMessage.EXTRA_API_KEY, TestConstants.API_TOKEN_DEFAULT);

        tested.onStartCommand(startIntent, -1, -1);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void startSensorbergService_with_no_api_key_and_null_bootstrapper_should_stop_service() {
        tested.bootstrapper = null;

        int returnNotSticky = tested.startSensorbergService(null);

        Assertions.assertThat(returnNotSticky).isEqualTo(SensorbergService.START_NOT_STICKY);
        Mockito.verify(tested, times(1)).stopSensorbergService();
    }

    @Test
    public void startSensorbergService_with_api_key_and_null_bootstrapper_should_create_bootstrapper() {
        tested.bootstrapper = null;

        int returnNotSticky = tested.startSensorbergService(TestConstants.API_TOKEN_DEFAULT);

        Assertions.assertThat(returnNotSticky).isEqualTo(SensorbergService.START_STICKY);
        Assertions.assertThat(tested.bootstrapper).isNotNull();
    }

    @Test
    public void startSensorbergService_with_api_key_and_bootstrapper_should_start_scanning() {
        tested.bootstrapper = createSpyBootstrapper();

        int returnNotSticky = tested.startSensorbergService(TestConstants.API_TOKEN_DEFAULT);

        Assertions.assertThat(returnNotSticky).isEqualTo(SensorbergService.START_STICKY);
        Assertions.assertThat(tested.bootstrapper).isNotNull();
        verify(tested.bootstrapper, times(1)).startScanning();
    }

    @Test
    public void restartSensorbergService_should_not_do_anything_if_no_disk_configuration() {
        fileManager.removeFile(SensorbergServiceMessage.SERVICE_CONFIGURATION);
        int returnSticky = tested.restartSensorbergService();

        Assertions.assertThat(returnSticky).isEqualTo(SensorbergService.START_STICKY);
        Mockito.verify(tested, times(1)).createBootstrapperFromDiskConfiguration();
        Assertions.assertThat(tested.bootstrapper).isNull();
    }

    @Test
    public void restartSensorbergService_should_start_scanning_if_disk_configuration() throws Exception {
        tested.fileManager = fileManager;
        fileManager.write(TestConstants.getDiskConfiguration(), SensorbergServiceMessage.SERVICE_CONFIGURATION);

        int returnSticky = tested.restartSensorbergService();

        Assertions.assertThat(returnSticky).isEqualTo(SensorbergService.START_STICKY);
        Mockito.verify(tested, times(1)).createBootstrapperFromDiskConfiguration();
        Assertions.assertThat(tested.bootstrapper).isNotNull();
    }

    //TODO fix this, blocks all other tests
//    @Test
//    public void stopSensorbergService_should_stop_service() {
//        int returnNotSticky = tested.stopSensorbergService();
//
//        Assertions.assertThat(returnNotSticky).isEqualTo(SensorbergService.START_NOT_STICKY);
//        Mockito.verify(tested, times(1)).stopSelf();
//    }

    @Test
    public void should_not_create_bootstrapper_from_null_disk_configuration() throws Exception {
        fileManager.removeFile(SensorbergServiceMessage.SERVICE_CONFIGURATION);
        SensorbergServiceConfiguration diskConf = (SensorbergServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
        Assertions.assertThat(diskConf).isNull();
        tested.bootstrapper = null;

        tested.createBootstrapperFromDiskConfiguration();

        Assertions.assertThat(tested.bootstrapper).isNull();
    }

    @Test
    public void should_create_bootstrapper_from_existing_disk_configuration() throws Exception {
        fileManager.write(TestConstants.getDiskConfiguration(), SensorbergServiceMessage.SERVICE_CONFIGURATION);

        InternalApplicationBootstrapper bootstrapper = tested.createBootstrapperFromDiskConfiguration();

        Assertions.assertThat(bootstrapper).isNotNull();
    }

    @Test
    public void should_persist_the_settings_when_getting_a_new_API_token() throws Exception {
        Intent changeApiKeyMessageIntent = SensorbergServiceIntents.getApiTokenIntent(InstrumentationRegistry.getContext(), NEW_API_TOKEN);

        tested.onStartCommand(changeApiKeyMessageIntent, -1, -1);
        verify(fileManager, times(2)).write(any(SensorbergServiceConfiguration.class), any(String.class));
    }

    @Test
    public void should_turn_debugging_on_in_transport_from_intent() {
        Intent serviceDebuggingOnIntent = SensorbergServiceIntents.getServiceLoggingIntent(InstrumentationRegistry.getContext(), true);

        try {
            tested.handleDebuggingIntent(serviceDebuggingOnIntent, InstrumentationRegistry.getContext());
            Assertions.fail("Should've gone into catch branch!");
        } catch (Exception e) {
            Assertions.assertThat(e).isInstanceOf(RuntimeException.class);
            Assertions.assertThat(e.getMessage()).containsIgnoringCase("Looper.prepare()");
        }

        Mockito.verify(tested.transport, times(1)).setLoggingEnabled(true);
        Assertions.assertThat(Logger.log).isInstanceOf(Logger.VerboseLogger.class);
    }

    @Test
    public void should_turn_debugging_off_in_transport_from_intent() {
        Intent serviceDebuggingOffIntent = SensorbergServiceIntents.getServiceLoggingIntent(InstrumentationRegistry.getContext(), false);

        try {
            tested.handleDebuggingIntent(serviceDebuggingOffIntent, InstrumentationRegistry.getContext());
            Assertions.fail("Should've gone into catch branch!");
        } catch (Exception e) {
            Assertions.assertThat(e).isInstanceOf(RuntimeException.class);
            Assertions.assertThat(e.getMessage()).containsIgnoringCase("Looper.prepare()");
        }

        Mockito.verify(tested.transport, times(1)).setLoggingEnabled(false);
        Assertions.assertThat(Logger.log).isEqualTo(Logger.QUIET_LOG);
    }

    @Test
    public void should_handle_shutdown_message_with_existing_bootstrapper() {
        Intent serviceShutdownIntent = SensorbergServiceIntents.getShutdownServiceIntent(InstrumentationRegistry.getContext());
        InternalApplicationBootstrapper bootstrapper = createSpyBootstrapper();
        tested.bootstrapper = bootstrapper;

        boolean response = tested.handleIntentEvenIfNoBootstrapperPresent(serviceShutdownIntent);

        Mockito.verify(tested.fileManager, times(1)).removeFile(SensorbergServiceMessage.SERVICE_CONFIGURATION);
        Mockito.verify(bootstrapper, times(1)).unscheduleAllPendingActions();
        Mockito.verify(bootstrapper, times(1)).stopScanning();
        Mockito.verify(bootstrapper, times(1)).stopAllScheduledOperations();
        Assertions.assertThat(tested.bootstrapper).isNull();
        Assertions.assertThat(response).isTrue();
    }

    @Test
    public void should_handle_shutdown_message_with_null_bootstrapper() {
        Intent serviceShutdownIntent = SensorbergServiceIntents.getShutdownServiceIntent(InstrumentationRegistry.getContext());
        tested.bootstrapper = null;

        boolean response = tested.handleIntentEvenIfNoBootstrapperPresent(serviceShutdownIntent);

        Mockito.verify(tested.fileManager, times(1)).removeFile(SensorbergServiceMessage.SERVICE_CONFIGURATION);
        Assertions.assertThat(tested.bootstrapper).isNull();
        Assertions.assertThat(response).isTrue();
    }

    @Test
    public void test_loadOrCreateNewServiceConfiguration_creates_new_config_if_null() {
        fileManager.removeFile(SensorbergServiceMessage.SERVICE_CONFIGURATION);
        SensorbergServiceConfiguration diskConf = (SensorbergServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
        Assertions.assertThat(diskConf).isNull();

        SensorbergServiceConfiguration diskConfNew = tested.loadOrCreateNewServiceConfiguration(fileManager);
        Assertions.assertThat(diskConfNew).isNotNull();
        Assertions.assertThat(diskConfNew.resolverConfiguration).isNotNull();
    }

    @Test
    public void test_updateDiskConfiguration_creates_new_disk_config_if_null() throws MalformedURLException {
        URL resolverURL = new URL("http://resolver-new.sensorberg.com");
        Intent serviceUpdateResolverIntent = SensorbergServiceIntents.getResolverEndpointIntent(InstrumentationRegistry.getContext(), resolverURL);

        tested.updateDiskConfiguration(serviceUpdateResolverIntent);

        Mockito.verify(tested, times(1)).loadOrCreateNewServiceConfiguration(fileManager);
    }

    @Test
    public void test_updateDiskConfiguration_persists_new_resolver_endpoint() throws MalformedURLException {
        URL resolverURL = new URL("http://resolver-new.sensorberg.com");
        Intent serviceUpdateResolverIntent = SensorbergServiceIntents.getResolverEndpointIntent(InstrumentationRegistry.getContext(), resolverURL);

        tested.updateDiskConfiguration(serviceUpdateResolverIntent);

        SensorbergServiceConfiguration diskConfNew = (SensorbergServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
        Assertions.assertThat(diskConfNew.resolverConfiguration.getResolverLayoutURL()).isEqualTo(resolverURL);
        Assertions.assertThat(URLFactory.getResolveURLString()).isEqualTo(resolverURL.toString());
    }

    @Test
    public void test_updateDiskConfiguration_persists_new_api_token() {
        String newApiToken = "123456";
        Intent serviceUpdateApiTokenIntent = SensorbergServiceIntents.getApiTokenIntent(InstrumentationRegistry.getContext(), newApiToken);

        tested.updateDiskConfiguration(serviceUpdateApiTokenIntent);

        SensorbergServiceConfiguration diskConfNew = (SensorbergServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
        Assertions.assertThat(diskConfNew.resolverConfiguration.apiToken).isEqualTo(newApiToken);
    }

    @Test
    public void test_updateDiskConfiguration_persists_new_advertising_identifier() {
        String newAdvertisingIdentifier = "123456";
        Intent serviceUpdateAdvertisingIdentifierIntent = SensorbergServiceIntents
                .getAdvertisingIdentifierIntent(InstrumentationRegistry.getContext(), newAdvertisingIdentifier);

        tested.updateDiskConfiguration(serviceUpdateAdvertisingIdentifierIntent);

        SensorbergServiceConfiguration diskConfNew = (SensorbergServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
        Assertions.assertThat(diskConfNew.resolverConfiguration.getAdvertisingIdentifier()).isEqualTo(newAdvertisingIdentifier);
    }

    private InternalApplicationBootstrapper createSpyBootstrapper() {
        InternalApplicationBootstrapper bootstrapper = new InternalApplicationBootstrapper(transport, serviceScheduler, handlerManager, clock,
                bluetoothPlatform);
        bootstrapper.setApiToken(TestConstants.API_TOKEN_DEFAULT);
        bootstrapper = spy(bootstrapper);
        return bootstrapper;
    }

    @Test
    public void should_present_valid_beacon_event() {
        //TODO
//        Intent serviceIntent = SensorbergServiceIntents
//                .getBeaconActionIntent(InstrumentationRegistry.getContext(), TestConstants.getBeaconEvent(), 0);
//        tested.bootstrapper = createSpyBootstrapper();
//
//        tested.presentBeaconEvent(serviceIntent);

//        Mockito.verify(tested.bootstrapper, Mockito.times(1)).presentEventDirectly(any(BeaconEvent.class), anyInt());
    }
}
