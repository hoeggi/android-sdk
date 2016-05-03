package com.sensorberg.sdk.testUtils;

import com.google.gson.Gson;

import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.interfaces.RetrofitApiService;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveResponse;

import android.content.Context;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Url;
import retrofit2.mock.BehaviorDelegate;
import retrofit2.mock.MockRetrofit;
import retrofit2.mock.NetworkBehavior;

public class SuccessfulRetrofitApiService extends RetrofitApiServiceImpl {

    MockRetrofitApiService mMockRetrofitApiService;

    public SuccessfulRetrofitApiService(Context ctx, Gson gson, PlatformIdentifier platformId) {
        super(ctx, gson, platformId, "http://test.com");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test.com")
                .client(getOkHttpClient(ctx))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Create a MockRetrofit object with a NetworkBehavior which manages the fake behavior of calls.
        NetworkBehavior behavior = NetworkBehavior.create();
        MockRetrofit mockRetrofit = new MockRetrofit.Builder(retrofit)
                .networkBehavior(behavior)
                .build();

        BehaviorDelegate<RetrofitApiService> delegate = mockRetrofit.create(RetrofitApiService.class);
        mMockRetrofitApiService = new MockRetrofitApiService(delegate);
    }

    @Override
    public void setApiToken(String mApiToken) {
        super.setApiToken(mApiToken);
    }

    @Override
    public void setLoggingEnabled(boolean enabled) {
        super.setLoggingEnabled(enabled);
    }

    @Override
    public Call<BaseResolveResponse> updateBeaconLayout(@Url String beaconLayoutUrl) {
        return mMockRetrofitApiService.updateBeaconLayout(beaconLayoutUrl);
    }

    @Override
    public Call<ResolveResponse> getBeacon(@Url String beaconURLString, @Header("X-pid") String beaconId, @Header("X-qos") String networkInfo) {
        return mMockRetrofitApiService.getBeacon(beaconURLString, beaconId, networkInfo);
    }

    @Override
    public Call<ResolveResponse> publishHistory(@Url String beaconLayoutUrl, @Body HistoryBody body) {
        return mMockRetrofitApiService.publishHistory(beaconLayoutUrl, body);
    }

    @Override
    public Call<SettingsResponse> getSettings() {
        return mMockRetrofitApiService.getSettings("");
    }


    static final class MockRetrofitApiService implements RetrofitApiService {
        private final BehaviorDelegate<RetrofitApiService> delegate;

        public MockRetrofitApiService(BehaviorDelegate<RetrofitApiService> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Call<BaseResolveResponse> updateBeaconLayout(@Url String beaconLayoutUrl) {
            //TODO
            ResolveResponse response = new ResolveResponse.Builder().build();
            return delegate.returningResponse(response).updateBeaconLayout(beaconLayoutUrl);
        }

        @Override
        public Call<ResolveResponse> getBeacon(@Url String beaconURLString, @Header("X-pid") String beaconId, @Header("X-qos") String networkInfo) {
            //TODO
            ResolveResponse response = new ResolveResponse.Builder().build();
            return delegate.returningResponse(response).getBeacon(beaconURLString, beaconId, networkInfo);
        }

        @Override
        public Call<ResolveResponse> publishHistory(@Url String beaconLayoutUrl, @Body HistoryBody body) {
            //TODO
            ResolveResponse response = new ResolveResponse.Builder().build();
            return delegate.returningResponse(response).publishHistory(beaconLayoutUrl, body);
        }

        @Override
        public Call<SettingsResponse> getSettings(@Url String settingsUrl) {
            //TODO
            SettingsResponse response = new SettingsResponse();
            return delegate.returningResponse(response).getSettings(settingsUrl);
        }
    }
}
