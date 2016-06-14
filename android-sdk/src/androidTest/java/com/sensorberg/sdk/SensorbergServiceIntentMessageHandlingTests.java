package com.sensorberg.sdk;

import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;
import com.sensorberg.sdk.scanner.Scanner;
import com.sensorberg.sdk.scanner.ScannerEvent;

import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import util.TestConstants;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class SensorbergServiceIntentMessageHandlingTests {

    private SensorbergService tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        tested = spy(new SensorbergService());
        tested.onCreate();

        Intent startIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        startIntent.putExtra(SensorbergServiceMessage.EXTRA_START_SERVICE, 1);
        startIntent.putExtra(SensorbergServiceMessage.EXTRA_API_KEY, TestConstants.API_TOKEN_DEFAULT);

        tested.onStartCommand(startIntent, -1, -1);
        tested.bootstrapper = spy(tested.bootstrapper);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void should_handle_intent_with_generic_noop_message() {
        Assertions.assertThat(tested.bootstrapper).isNotNull();

        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, -1);
        tested.bootstrapper.scanner = spy(tested.bootstrapper.scanner);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(0)).updateBeaconLayout();
        Mockito.verify(tested.bootstrapper.scanner, Mockito.times(0)).handlePlatformMessage(any(Bundle.class));
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).updateSettings();
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).uploadHistory();
        Mockito.verify(tested, Mockito.times(0)).presentBeaconEvent(serviceIntent);
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).retryScanEventResolve(any(ResolutionConfiguration.class));
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).hostApplicationInForeground();
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).hostApplicationInBackground();
        Mockito.verify(tested, Mockito.times(0)).setApiToken(serviceIntent);
        Mockito.verify(tested, Mockito.times(0)).setResolverEndpoint(serviceIntent);
        Mockito.verify(tested, Mockito.times(0)).registerPresentationDelegate(serviceIntent);
        Mockito.verify(tested, Mockito.times(0)).unregisterPresentationDelegate(serviceIntent);
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).startScanning();
        Mockito.verify(tested, Mockito.times(0)).processBluetoothStateMessage(serviceIntent);
        Mockito.verify(tested, Mockito.times(0)).setAdvertisingIdentifier(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_beacon_layout_update_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_BEACON_LAYOUT_UPDATE);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).updateBeaconLayout();
    }

    @Test
    public void should_handle_intent_with_scanner_message() {
        Bundle bundle = new Bundle();
        bundle.putInt(Scanner.SCANNER_EVENT, ScannerEvent.UN_PAUSE_SCAN);
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SDK_SCANNER_MESSAGE);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_WHAT, bundle);

        tested.bootstrapper.scanner = spy(tested.bootstrapper.scanner);
        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper.scanner, Mockito.times(1)).handlePlatformMessage(bundle);
    }

    @Test
    public void should_handle_intent_with_update_settings_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SETTINGS_UPDATE);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).updateSettings();
    }

    @Test
    public void should_handle_intent_with_upload_history_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_UPLOAD_HISTORY);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).uploadHistory();
    }

    @Test
    public void should_handle_intent_with_beacon_action_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.GENERIC_TYPE_BEACON_ACTION);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).presentBeaconEvent(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_retry_scan_event_resolve_message() {
        ResolutionConfiguration configuration = new ResolutionConfiguration();
        configuration.setScanEvent(TestConstants.REGULAR_BEACON_SCAN_EVENT(DateTime.now().getMillis()));
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.GENERIC_TYPE_RETRY_RESOLVE_SCANEVENT);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_WHAT, configuration);

        tested.handleIntentMessage(serviceIntent);
        Mockito.verify(tested.bootstrapper, Mockito.times(1)).retryScanEventResolve(configuration);
    }

    @Test
    public void should_handle_intent_with_app_in_foreground_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_APPLICATION_IN_FOREGROUND);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).hostApplicationInForeground();
    }

    @Test
    public void should_handle_intent_with_app_in_background_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_APPLICATION_IN_BACKGROUND);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).hostApplicationInBackground();
    }

    @Test
    public void should_handle_intent_with_set_api_token_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SET_API_TOKEN);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).setApiToken(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_set_resolver_endpoint_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_TYPE_SET_RESOLVER_ENDPOINT);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).setResolverEndpoint(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_register_presentation_delegate_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_REGISTER_PRESENTATION_DELEGATE);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).registerPresentationDelegate(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_unregister_presentation_delegate_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_UNREGISTER_PRESENTATION_DELEGATE);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).unregisterPresentationDelegate(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_ping_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_PING);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).startScanning();
    }

    @Test
    public void should_handle_intent_with_bluetooth_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_BLUETOOTH);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).processBluetoothStateMessage(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_set_advertising_id_message() {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).setAdvertisingIdentifier(serviceIntent);
    }
}
