package com.sensorberg.sdk.internal.transport;

import com.google.gson.Gson;

import com.sensorberg.android.networkstate.NetworkInfoBroadcastReceiver;
import com.sensorberg.sdk.internal.URLFactory;
import com.sensorberg.sdk.internal.interfaces.BeaconHistoryUploadIntervalListener;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Setter;
import okhttp3.Cache;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.sensorberg.sdk.internal.URLFactory.getResolveURLString;
import static com.sensorberg.sdk.internal.URLFactory.getSettingsURLString;
import static com.sensorberg.utils.ListUtils.map;

public class RetrofitApiTransport implements Transport,
        PlatformIdentifier.DeviceInstallationIdentifierChangeListener,
        PlatformIdentifier.AdvertiserIdentifierChangeListener {

    private static final int CONNECTION_TIMEOUT = 30; //seconds

    private static final long HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 5 * 1024 * 1024; //5MB

    private final Gson mGson;

    private final PlatformIdentifier mPlatformIdentifier;

    private final Clock mClock;

    private final boolean mShouldUseSyncClient;

    private Context mContext;

    private HttpLoggingInterceptor.Level mApiServiceLogLevel = HttpLoggingInterceptor.Level.NONE;

    private RetrofitApiService mApiService;

    @Setter
    private BeaconHistoryUploadIntervalListener beaconHistoryUploadIntervalListener = BeaconHistoryUploadIntervalListener.NONE;

    private String mApiToken;

    private ProximityUUIDUpdateHandler mProximityUUIDUpdateHandler = ProximityUUIDUpdateHandler.NONE;

    private BeaconReportHandler mBeaconReportHandler;

    public RetrofitApiTransport(Context context, Gson gson, Clock clk, PlatformIdentifier platformId, boolean useSyncClient) {
        mContext = context;
        mGson = gson;
        mApiService = getApiService();
        mPlatformIdentifier = platformId;

        mClock = clk;
        mShouldUseSyncClient = useSyncClient;

        platformId.addAdvertiserIdentifierChangeListener(this);
        platformId.addDeviceInstallationIdentifierChangeListener(this);
    }

    private RetrofitApiService getApiService() {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(URLFactory.getResolveURLString())
                .client(getOkHttpClient(mContext))
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .build();

        return restAdapter.create(RetrofitApiService.class);
    }

    Interceptor headerAuthorizationInterceptor = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            Headers.Builder headersBuilder = request.headers()
                    .newBuilder()
                    .add(HEADER_USER_AGENT, mPlatformIdentifier.getUserAgentString())
                    .add(HEADER_INSTALLATION_IDENTIFIER, mPlatformIdentifier.getDeviceInstallationIdentifier());

            if (mPlatformIdentifier.getAdvertiserIdentifier() != null) {
                headersBuilder.add(HEADER_ADVERTISER_IDENTIFIER, mPlatformIdentifier.getAdvertiserIdentifier());
            }

            if (mApiToken != null) {
                headersBuilder
                        .add(HEADER_AUTHORIZATION, mApiToken)
                        .add(HEADER_XAPIKEY, mApiToken);
            }

            request = request.newBuilder().headers(headersBuilder.build()).build();
            return chain.proceed(request);
        }
    };

    private OkHttpClient getOkHttpClient(Context context) {
        OkHttpClient.Builder okClientBuilder = new OkHttpClient.Builder();

        okClientBuilder.addInterceptor(headerAuthorizationInterceptor);

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(mApiServiceLogLevel);
        okClientBuilder.addInterceptor(httpLoggingInterceptor);

        final File baseDir = context.getCacheDir();
        if (baseDir != null) {
            final File cacheDir = new File(baseDir, "HttpResponseCache");
            okClientBuilder.cache(new Cache(cacheDir, HTTP_RESPONSE_DISK_CACHE_MAX_SIZE));
        }

        okClientBuilder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        okClientBuilder.readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        okClientBuilder.writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);

        return okClientBuilder.build();
    }

    @Override
    public void advertiserIdentifierChanged(String advertiserIdentifier) {
        //we don't care, it's always dynamic now
    }

    @Override
    public void deviceInstallationIdentifierChanged(String deviceInstallationIdentifier) {
        //we don't care, it's always dynamic now
    }

    @Override
    public void setBeaconReportHandler(BeaconReportHandler beaconReportHandler) {
        mBeaconReportHandler = beaconReportHandler;
    }

    @Override
    public void setProximityUUIDUpdateHandler(ProximityUUIDUpdateHandler proximityUUIDUpdateHandler) {
        if (proximityUUIDUpdateHandler != null) {
            mProximityUUIDUpdateHandler = proximityUUIDUpdateHandler;
        } else {
            mProximityUUIDUpdateHandler = ProximityUUIDUpdateHandler.NONE;
        }
    }

    @Override
    public void getBeacon(final ResolutionConfiguration resolutionConfiguration, final BeaconResponseHandler beaconResponseHandler) {
        String networkInfo = NetworkInfoBroadcastReceiver.latestNetworkInfo != null
                ? NetworkInfoBroadcastReceiver.getNetworkInfoString() : "";

        Call<ResolveResponse> call = mApiService
                .getBeacon(getResolveURLString(), resolutionConfiguration.getScanEvent().getBeaconId().getBid(), networkInfo);

        call.enqueue(new Callback<ResolveResponse>() {
            @Override
            public void onResponse(Call<ResolveResponse> call, Response<ResolveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean reportImmediately = false;
                    List<ResolveAction> resolveActions = response.body().resolve(resolutionConfiguration.getScanEvent(), mClock.now());
                    for (ResolveAction resolveAction : resolveActions) {
                        reportImmediately |= resolveAction.reportImmediately;
                    }
                    List<BeaconEvent> beaconEvents = map(resolveActions, ResolveAction.BEACON_EVENT_MAPPER);
                    for (BeaconEvent beaconEvent : beaconEvents) {
                        beaconEvent.setBeaconId(resolutionConfiguration.getScanEvent().getBeaconId());
                    }
                    beaconResponseHandler.onSuccess(beaconEvents);
                    if (reportImmediately) {
                        mBeaconReportHandler.reportImmediately();
                    }
                    mProximityUUIDUpdateHandler.proximityUUIDListUpdated(response.body().getAccountProximityUUIDs());
                    if (response.body().reportTriggerSeconds != null) {
                        beaconHistoryUploadIntervalListener
                                .historyUploadIntervalChanged(TimeUnit.SECONDS.toMillis(response.body().reportTriggerSeconds));
                    }
                } else {
                    beaconResponseHandler.onFailure(new Throwable("No Content, Invalid Api Key"));
                }
            }

            @Override
            public void onFailure(Call<ResolveResponse> call, Throwable t) {
                beaconResponseHandler.onFailure(t);
            }
        });

    }

    @Override
    public void setApiToken(String apiToken) {
        mApiToken = apiToken;
    }

    @Override
    public void loadSettings(final TransportSettingsCallback transportSettingsCallback) {
        Call<SettingsResponse> call = mApiService.getSettings(getSettingsURLString(mApiToken));

        call.enqueue(new Callback<SettingsResponse>() {
            @Override
            public void onResponse(Call<SettingsResponse> call, Response<SettingsResponse> response) {
                if (response.isSuccessful()) {
                    if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                        transportSettingsCallback.onSettingsFound(null);
                    } else {
                        transportSettingsCallback.onSettingsFound(response.body().getSettings());
                    }
                } else {
                    if (response.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        transportSettingsCallback.nothingChanged();
                    } else {
                        transportSettingsCallback.onFailure(new Exception());
                    }
                }
            }

            @Override
            public void onFailure(Call<SettingsResponse> call, Throwable t) {
                transportSettingsCallback.onFailure(new Exception(t));
            }
        });
    }

    @Override
    public void publishHistory(final List<SugarScan> scans, final List<SugarAction> actions, final TransportHistoryCallback callback) {

        HistoryBody body = new HistoryBody(scans, actions, mClock);
        Call<ResolveResponse> call = mApiService.publishHistory(getResolveURLString(), body);

        call.enqueue(new Callback<ResolveResponse>() {
            @Override
            public void onResponse(Call<ResolveResponse> call, Response<ResolveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(scans, actions);
                    callback.onInstantActions(response.body().getInstantActionsAsBeaconEvent());
                } else {
                    callback.onFailure(new Exception("No Content, Invalid Api Key"));
                }
            }

            @Override
            public void onFailure(Call<ResolveResponse> call, Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }

    @Override
    public void updateBeaconLayout() {

        Call<BaseResolveResponse> call = mApiService.updateBeaconLayout(getResolveURLString());

        call.enqueue(new Callback<BaseResolveResponse>() {
            @Override
            public void onResponse(Call<BaseResolveResponse> call, Response<BaseResolveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mProximityUUIDUpdateHandler.proximityUUIDListUpdated(response.body().getAccountProximityUUIDs());
                } else {
                    mProximityUUIDUpdateHandler.proximityUUIDListUpdated(Collections.EMPTY_LIST);
                }
            }

            @Override
            public void onFailure(Call<BaseResolveResponse> call, Throwable t) {
                mProximityUUIDUpdateHandler.proximityUUIDListUpdated(Collections.EMPTY_LIST);
            }
        });
    }

    @Override
    public void setLoggingEnabled(boolean enabled) {
        synchronized (mGson) {

            if (enabled) {
                mApiServiceLogLevel = HttpLoggingInterceptor.Level.BODY;
            } else {
                mApiServiceLogLevel = HttpLoggingInterceptor.Level.NONE;
            }

            if (mApiService != null) {
                mApiService = getApiService();
            }
        }
    }

}
