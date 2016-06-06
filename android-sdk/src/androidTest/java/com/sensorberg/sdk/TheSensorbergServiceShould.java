package com.sensorberg.sdk;

import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

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

        tested = new SensorbergService();
        tested.onCreate();
        fileManager = spy(fileManager);
        tested.fileManager = fileManager;
        tested.transport = Mockito.mock(Transport.class);

        Intent startIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        startIntent.putExtra(SensorbergService.EXTRA_API_KEY, DEFAULT_API_KEY);

        tested.onStartCommand(startIntent, -1, -1);
    }

    @Test
    public void should_persist_the_settings_when_getting_a_new_API_token() throws Exception {
        Intent changeApiKeyMessageIntent = new Intent();
        changeApiKeyMessageIntent.putExtra(SensorbergService.MSG_SET_API_TOKEN_TOKEN, NEW_API_TOKEN);
        changeApiKeyMessageIntent.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_SET_API_TOKEN);

        //TODO check if this is really optimal, to have persistence called twice
        tested.onStartCommand(changeApiKeyMessageIntent, -1, -1);
        verify(fileManager, times(2)).write(any(ServiceConfiguration.class), any(String.class));
    }

    @Test
    public void should_turn_debugging_on_in_transport_from_intent() {
        Intent serviceDebuggingOnIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceDebuggingOnIntent.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_TYPE_ENABLE_LOGGING);

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
        serviceDebuggingOffIntent.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_TYPE_DISABLE_LOGGING);

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
        serviceShutdownIntent.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_SHUTDOWN);
        InternalApplicationBootstrapper bootstrapper = new InternalApplicationBootstrapper(transport, serviceScheduler, handlerManager, clock,
                bluetoothPlatform);
        bootstrapper.setApiToken(TestConstants.API_TOKEN_DEFAULT);
        bootstrapper = spy(bootstrapper);
        tested.bootstrapper = bootstrapper;

        boolean response = tested.handleIntentEvenIfNoBootstrapperPresent(serviceShutdownIntent);

        Mockito.verify(tested.fileManager, times(1)).removeFile(SensorbergService.SERVICE_CONFIGURATION);
        Mockito.verify(bootstrapper, times(1)).unscheduleAllPendingActions();
        Mockito.verify(bootstrapper, times(1)).stopScanning();
        Mockito.verify(bootstrapper, times(1)).stopAllScheduledOperations();
        Assertions.assertThat(tested.bootstrapper).isNull();
        Assertions.assertThat(response).isTrue();
    }

    @Test
    public void should_handle_shutdown_message_with_null_bootstrapper() {
        Intent serviceShutdownIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceShutdownIntent.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_SHUTDOWN);
        tested.bootstrapper = null;

        boolean response = tested.handleIntentEvenIfNoBootstrapperPresent(serviceShutdownIntent);

        Mockito.verify(tested.fileManager, times(1)).removeFile(SensorbergService.SERVICE_CONFIGURATION);
        Assertions.assertThat(tested.bootstrapper).isNull();
        Assertions.assertThat(response).isTrue();
    }
}
