package com.sensorberg.sdk.testUtils;

import com.google.gson.Gson;

import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveResponse;

import android.content.Context;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Url;

public class SuccessfulRetrofitApiService extends RetrofitApiServiceImpl {

    public SuccessfulRetrofitApiService(Context ctx, Gson gson, PlatformIdentifier platformId) {
        super(ctx, gson, platformId);
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
        //TODO
        return null;
    }

    @Override
    public Call<ResolveResponse> getBeacon(@Url String beaconURLString, @Header("X-pid") String beaconId, @Header("X-qos") String networkInfo) {
        //TODO
        return null;
    }

    @Override
    public Call<ResolveResponse> publishHistory(@Url String beaconLayoutUrl, @Body HistoryBody body) {
        //TODO
        return null;
    }

    @Override
    public Call<SettingsResponse> getSettings() {
        //TODO
        return null;
    }
}
