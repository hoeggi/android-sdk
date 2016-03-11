package com.sensorberg.sdk.internal;

import com.android.sensorbergVolley.DefaultRetryPolicy;
import com.android.sensorbergVolley.Request;
import com.android.sensorbergVolley.RequestQueue;
import com.android.sensorbergVolley.Response;
import com.android.sensorbergVolley.VolleyError;
import com.android.sensorbergVolley.toolbox.RequestFuture;
import com.sensorberg.android.networkstate.NetworkInfoBroadcastReceiver;
import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.internal.transport.HistoryCallback;
import com.sensorberg.sdk.internal.transport.SettingsCallback;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.model.realm.RealmAction;
import com.sensorberg.sdk.model.realm.RealmScan;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.utils.Objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.sensorberg.sdk.internal.URLFactory.getResolveURLString;
import static com.sensorberg.sdk.internal.URLFactory.getSettingsURLString;
import static com.sensorberg.utils.ListUtils.map;

public class OkHttpClientTransport implements Transport, Platform.DeviceInstallationIdentifierChangeListener, Platform.AdvertiserIdentifierChangeListener {

    private static final JSONObject NO_CONTENT = new JSONObject();

    private final Map<String, String> headers = new HashMap<>();

    private final RequestQueue queue;
    private final Platform platform;
    private final Settings settings;
    private BeaconReportHandler beaconReportHandler;
    private ProximityUUIDUpdateHandler proximityUUIDUpdateHandler = ProximityUUIDUpdateHandler.NONE;
    private String apiToken;

    private static final String INSTALLATION_IDENTIFIER = "X-iid";
    private static final String ADVERTISER_IDENTIFIER = "X-aid";

    public OkHttpClientTransport(Platform platform, Settings settings) {
        this.platform = platform;
        this.settings = settings;
        this.queue = platform.getVolleyQueue();
        this.headers.put("User-Agent", platform.getUserAgentString());
        this.headers.put(INSTALLATION_IDENTIFIER, platform.getDeviceInstallationIdentifier());
        this.headers.put(ADVERTISER_IDENTIFIER, platform.getAdvertiserIdentifier());
        platform.addAdvertiserIdentifierChangeListener(this);
        platform.addDeviceInstallationIdentifierChangeListener(this);
    }

    @Override
    public void deviceInstallationIdentifierChanged(String deviceInstallationIdentifier) {
        this.headers.put(INSTALLATION_IDENTIFIER, deviceInstallationIdentifier);
    }

    @Override
    public void advertiserIdentifierChanged(String advertiserIdentifier) {
        this.headers.put(ADVERTISER_IDENTIFIER, advertiserIdentifier);
    }

    @Override
    public void setBeaconReportHandler(BeaconReportHandler beaconReportHandler) {
        this.beaconReportHandler = beaconReportHandler;
    }

    @Override
    public void setProximityUUIDUpdateHandler(ProximityUUIDUpdateHandler proximityUUIDUpdateHandler) {
        if (proximityUUIDUpdateHandler != null) {
            this.proximityUUIDUpdateHandler = proximityUUIDUpdateHandler;
        } else {
            this.proximityUUIDUpdateHandler = ProximityUUIDUpdateHandler.NONE;
        }
    }

    @Override
    public void updateBeaconLayout() {
        Response.Listener<BaseResolveResponse> listener = new Response.Listener<BaseResolveResponse>() {
            @Override
            public void onResponse(BaseResolveResponse response) {
                if ( response != null ) {
                    proximityUUIDUpdateHandler.proximityUUIDListUpdated(response.getAccountProximityUUIDs());
                } else {
                    //noinspection unchecked not returning null
                    proximityUUIDUpdateHandler.proximityUUIDListUpdated(Collections.EMPTY_LIST);
                }
            }
        };
        //noinspection unchecked
        perform(Request.Method.GET, getResolveURLString(), null, listener, Response.ErrorListener.NONE, BaseResolveResponse.class, Collections.EMPTY_MAP, true);
    }

    @Override
    public void getBeacon(final ResolutionConfiguration resolutionConfiguration, final BeaconResponseHandler beaconResponseHandler) {
        String beaconURLString = getResolveURLString();

        Response.Listener<ResolveResponse> listener = new Response.Listener<ResolveResponse>() {
            @Override
            public void onResponse(ResolveResponse response) {
                if (response == null){
                    beaconResponseHandler.onFailure(new VolleyError("No Content, Invalid Api Key"));
                    return;
                }
                boolean reportImmediately = false;
                final List<ResolveAction> resolveActions =  response.resolve(resolutionConfiguration.getScanEvent(), platform.getClock().now());
                for (ResolveAction resolveAction : resolveActions) {
                    reportImmediately |= resolveAction.reportImmediately;
                }
                List<BeaconEvent> beaconEvents = map(resolveActions, ResolveAction.BEACON_EVENT_MAPPER);
                for (BeaconEvent beaconEvent : beaconEvents) {
                    beaconEvent.setBeaconId(resolutionConfiguration.getScanEvent().getBeaconId());
                }
                beaconResponseHandler.onSuccess(beaconEvents);
                if (reportImmediately){
                    beaconReportHandler.reportImmediately();
                }
                proximityUUIDUpdateHandler.proximityUUIDListUpdated(response.getAccountProximityUUIDs());
                if (response.reportTriggerSeconds != null){
                    settings.historyUploadIntervalChanged(TimeUnit.SECONDS.toMillis(response.reportTriggerSeconds));
                }

            }
        };
        Response.ErrorListener errorlistener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                beaconResponseHandler.onFailure(volleyError);
            }
        };

        perform(Request.Method.GET, beaconURLString, null, listener, errorlistener, ResolveResponse.class, beaconHeader(resolutionConfiguration.getScanEvent()), false);

    }

    private Map<String, String> beaconHeader(ScanEvent scanEvent) {
        Map<String, String> map = new HashMap<>();
        map.put("X-pid", scanEvent.getBeaconId().getBid());
        if (NetworkInfoBroadcastReceiver.latestNetworkInfo != null){
            map.put("X-qos", NetworkInfoBroadcastReceiver.getNetworkInfoString());
        }

        return map;
    }

    public void perform(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorlistener) {
        perform(Request.Method.GET, url, null, listener, errorlistener);
    }
    private void perform(int method, String url, Object body, Response.Listener<JSONObject> listener, Response.ErrorListener errorlistener) {
        //noinspection unchecked
        perform(method, url, body, listener, errorlistener, JSONObject.class, Collections.EMPTY_MAP, false);
    }

    private <T> void perform(int method, String url, Object body, Response.Listener<T> listener, Response.ErrorListener errorlistener, Class<T> clazz, Map<String, String> headers, boolean shouldAlwaysTryWithNetwork) {
        Map<String, String> requestHeaders = new HashMap<>(headers);
        requestHeaders.putAll(this.headers);

        if (platform.useSyncClient()){
            RequestFuture<T> future = RequestFuture.newFuture();
            HeadersJsonObjectRequest<T> request = new HeadersJsonObjectRequest<>(method, url, requestHeaders, body, future, future, clazz)
                    .setShouldAlwaysTryWithNetwork(shouldAlwaysTryWithNetwork);
            setupRetries(request);
            queue.add(request);
            //noinspection TryWithIdenticalCatches
            try {
                T response = future.get(30, TimeUnit.SECONDS); // this will block
                listener.onResponse(response);
            } catch (InterruptedException e) {
                errorlistener.onErrorResponse(new VolleyError(e));
            } catch (ExecutionException e) {
                errorlistener.onErrorResponse(new VolleyError(e));
            } catch (TimeoutException e) {
                errorlistener.onErrorResponse(new VolleyError(e));
            }
        } else {
            HeadersJsonObjectRequest<T> request = new HeadersJsonObjectRequest<>(method, url, requestHeaders, body, listener, errorlistener, clazz)
                    .setShouldAlwaysTryWithNetwork(shouldAlwaysTryWithNetwork);
            if (method == Request.Method.POST) {
                request.setShouldCache(false);
            }
            setupRetries(request);
            queue.add(request);
        }
    }

    private void setupRetries(Request request) {
        request.setRetryPolicy(new DefaultRetryPolicy((int) (30 * Constants.Time.ONE_SECOND), 3, 1.0f));
    }

    @Override
    public void setApiToken(String apiToken) {
        if (!Objects.equals(this.apiToken, apiToken)){
            this.queue.getCache().clear();
        }
        this.apiToken = apiToken;
        if (apiToken != null) {
            headers.put("Authorization", apiToken);
            headers.put("X-Api-Key", apiToken);
        } else {
            headers.remove("X-Api-Key");
            headers.remove("Authorization");
        }
    }

    @Override
    public void getSettings(final SettingsCallback settingsCallback) {

        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                if (response == null) {
                    settingsCallback.onSettingsFound(null);
                    return;
                }

                boolean success = response.optBoolean("success", true);
                if (success) {
                    try {
                        settingsCallback.onSettingsFound(response.getJSONObject("settings"));
                    } catch (JSONException e) {
                        settingsCallback.onFailure(new VolleyError(e));
                    }
                } else {
                    settingsCallback.onFailure(new VolleyError(new IllegalArgumentException("Server did not respond with success=true")));
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {


                if (volleyError.networkResponse != null) {
                    if (volleyError.networkResponse.statusCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        settingsCallback.nothingChanged();
                        return;
                    }
                    if (volleyError.networkResponse.statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        settingsCallback.onSettingsFound(NO_CONTENT);
                        return;
                    }
                }

                settingsCallback.onFailure(volleyError);
            }
        };
        perform(getSettingsURLString(this.apiToken), responseListener, errorListener);
    }

    @Override
    public void publishHistory(final List<RealmScan> scans, final List<RealmAction> actions, final HistoryCallback callback) {
        Response.Listener<ResolveResponse> responseListener = new Response.Listener<ResolveResponse>() {
            @Override
            public void onResponse(ResolveResponse response) {
                if (response == null){
                    callback.onFailure(new VolleyError("No Content, Invalid Api Key"));
                    return;
                }
                callback.onSuccess(scans, actions);
                callback.onInstantActions(response.getInstantActionsAsBeaconEvent());
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        };

        HistoryBody body = new HistoryBody(scans, actions, platform.getClock());

        //noinspection unchecked
        perform(Request.Method.POST, getResolveURLString(), body, responseListener, errorListener, ResolveResponse.class, Collections.EMPTY_MAP, false);

    }
}
