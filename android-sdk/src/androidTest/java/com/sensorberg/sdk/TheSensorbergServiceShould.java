package com.sensorberg.sdk;

import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.URLFactory;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.resolver.ResolverConfiguration;

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
public class TheSensorbergServiceShould {

    private static final String NEW_API_TOKEN = "SOME_NEW_API_TOKEN";

    private static final String DEFAULT_API_KEY = "DEFAULT_API_KEY";

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

    SensorbergService tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        tested = spy(new SensorbergService());
        tested.onCreate();
        fileManager = spy(fileManager);
        tested.fileManager = fileManager;
        tested.transport = Mockito.mock(Transport.class);

        Intent startIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        startIntent.putExtra(SensorbergServiceMessage.EXTRA_API_KEY, DEFAULT_API_KEY);

        tested.onStartCommand(startIntent, -1, -1);
    }

    @After
    public void tearDown() {
        fileManager.removeFile(SensorbergServiceMessage.SERVICE_CONFIGURATION);
    }

    @Test
    public void should_not_create_bootstrapper_from_null_disk_configuration() throws Exception {
        ServiceConfiguration diskConf = (ServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
        Assertions.assertThat(diskConf).isNull();
        tested.bootstrapper = null;

        tested.createBootstrapperFromDiskConfiguration();

        Assertions.assertThat(tested.bootstrapper).isNull();
    }

    @Test
    public void should_create_bootstrapper_from_existing_disk_configuration() throws Exception {
        tested.bootstrapper = null;

        ServiceConfiguration diskConf = new ServiceConfiguration(new ResolverConfiguration());
        diskConf.resolverConfiguration.setApiToken("123456");
        diskConf.resolverConfiguration.setAdvertisingIdentifier("123456");
        diskConf.resolverConfiguration.setResolverLayoutURL(new URL("http://resolver-new.sensorberg.com"));
        fileManager.write(diskConf, SensorbergServiceMessage.SERVICE_CONFIGURATION);

        tested.createBootstrapperFromDiskConfiguration();

        Assertions.assertThat(tested.bootstrapper).isNotNull();
    }

    @Test
    public void should_persist_the_settings_when_getting_a_new_API_token() throws Exception {
        Intent changeApiKeyMessageIntent = new Intent();
        changeApiKeyMessageIntent.putExtra(SensorbergServiceMessage.MSG_SET_API_TOKEN_TOKEN, NEW_API_TOKEN);
        changeApiKeyMessageIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SET_API_TOKEN);

        //TODO check if this is really optimal, to have persistence called twice
        tested.onStartCommand(changeApiKeyMessageIntent, -1, -1);
        verify(fileManager, times(2)).write(any(ServiceConfiguration.class), any(String.class));
    }

    @Test
    public void should_turn_debugging_on_in_transport_from_intent() {
        Intent serviceDebuggingOnIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceDebuggingOnIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_TYPE_ENABLE_LOGGING);

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
        Intent serviceDebuggingOffIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceDebuggingOffIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_TYPE_DISABLE_LOGGING);

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
        Intent serviceShutdownIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceShutdownIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SHUTDOWN);
        InternalApplicationBootstrapper bootstrapper = new InternalApplicationBootstrapper(transport, serviceScheduler, handlerManager, clock,
                bluetoothPlatform);
        bootstrapper.setApiToken(TestConstants.API_TOKEN_DEFAULT);
        bootstrapper = spy(bootstrapper);
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
        Intent serviceShutdownIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceShutdownIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SHUTDOWN);
        tested.bootstrapper = null;

        boolean response = tested.handleIntentEvenIfNoBootstrapperPresent(serviceShutdownIntent);

        Mockito.verify(tested.fileManager, times(1)).removeFile(SensorbergServiceMessage.SERVICE_CONFIGURATION);
        Assertions.assertThat(tested.bootstrapper).isNull();
        Assertions.assertThat(response).isTrue();
    }

    @Test
    public void test_loadOrCreateNewServiceConfiguration_creates_new_config_if_null() {
        ServiceConfiguration diskConf = (ServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
        Assertions.assertThat(diskConf).isNull();

        ServiceConfiguration diskConfNew = tested.loadOrCreateNewServiceConfiguration(fileManager);
        Assertions.assertThat(diskConfNew).isNotNull();
        Assertions.assertThat(diskConfNew.resolverConfiguration).isNotNull();
    }

    @Test
    public void test_updateDiskConfiguration_creates_new_disk_config_if_null() throws MalformedURLException {
        URL resolverURL = new URL("http://resolver-new.sensorberg.com");

        Intent serviceUpdateResolverIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceUpdateResolverIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_TYPE_SET_RESOLVER_ENDPOINT);
        serviceUpdateResolverIntent.putExtra(SensorbergServiceMessage.MSG_SET_RESOLVER_ENDPOINT_ENDPOINT_URL, resolverURL);

        tested.updateDiskConfiguration(serviceUpdateResolverIntent);

        Mockito.verify(tested, times(1)).loadOrCreateNewServiceConfiguration(fileManager);
    }

    @Test
    public void test_updateDiskConfiguration_persists_new_resolver_endpoint() throws MalformedURLException {
        URL resolverURL = new URL("http://resolver-new.sensorberg.com");

        Intent serviceUpdateResolverIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceUpdateResolverIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_TYPE_SET_RESOLVER_ENDPOINT);
        serviceUpdateResolverIntent.putExtra(SensorbergServiceMessage.MSG_SET_RESOLVER_ENDPOINT_ENDPOINT_URL, resolverURL);

        tested.updateDiskConfiguration(serviceUpdateResolverIntent);

        ServiceConfiguration diskConfNew = (ServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
        Assertions.assertThat(diskConfNew.resolverConfiguration.getResolverLayoutURL()).isEqualTo(resolverURL);
        Assertions.assertThat(URLFactory.getResolveURLString()).isEqualTo(resolverURL.toString());
    }

    @Test
    public void test_updateDiskConfiguration_persists_new_api_token() {
        String newApiToken = "123456";

        Intent serviceUpdateApiTokenIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceUpdateApiTokenIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SET_API_TOKEN);
        serviceUpdateApiTokenIntent.putExtra(SensorbergServiceMessage.MSG_SET_API_TOKEN_TOKEN, newApiToken);

        tested.updateDiskConfiguration(serviceUpdateApiTokenIntent);

        ServiceConfiguration diskConfNew = (ServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
        Assertions.assertThat(diskConfNew.resolverConfiguration.apiToken).isEqualTo(newApiToken);
    }

    @Test
    public void test_updateDiskConfiguration_persists_new_advertising_identifier() {
        String newAdvertisingIdentifier = "123456";

        Intent serviceUpdateAdvertisingIdentifierIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceUpdateAdvertisingIdentifierIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER);
        serviceUpdateAdvertisingIdentifierIntent
                .putExtra(SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER_ADVERTISING_IDENTIFIER, newAdvertisingIdentifier);

        tested.updateDiskConfiguration(serviceUpdateAdvertisingIdentifierIntent);

        ServiceConfiguration diskConfNew = (ServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
        Assertions.assertThat(diskConfNew.resolverConfiguration.getAdvertisingIdentifier()).isEqualTo(newAdvertisingIdentifier);
    }
}
