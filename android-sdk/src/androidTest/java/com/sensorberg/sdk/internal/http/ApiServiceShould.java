package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.model.server.BaseResolveResponse;

import junit.framework.Assert;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Response;

@RunWith(AndroidJUnit4.class)
public class ApiServiceShould {

    @Inject
    Gson gson;

    @Inject
    Context mContext;

    @Inject
    @Named("androidPlatformIdentifier")
    PlatformIdentifier realPlatformIdentifier;

    @Inject
    @Named("realRetrofitApiService")
    RetrofitApiServiceImpl realRetrofitApiService;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    @Test
    public void apiservice_should_have_valid_useragent_in_header() throws Exception {
        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout("");
        Response<BaseResolveResponse> response = call.execute();

        Assertions.assertThat(response.raw().request().headers()).isNotNull();
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_USER_AGENT))
                .isEqualTo(realPlatformIdentifier.getUserAgentString());
    }

    @Test
    public void apiservice_should_have_advertiserid_in_header() throws Exception {
        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout("");
        Response<BaseResolveResponse> response = call.execute();

        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_ADVERTISER_IDENTIFIER))
                .isNull();

        Thread.sleep(2000); //we wait for google to give us the advertiser id

        Assertions.assertThat(realPlatformIdentifier.getAdvertiserIdentifier())
                .isNotNull();
        Response<BaseResolveResponse> responseWithAdvertiserId = call.clone().execute();

        Assertions.assertThat(responseWithAdvertiserId.raw().request().headers().get(Transport.HEADER_ADVERTISER_IDENTIFIER))
                .isEqualTo(realPlatformIdentifier.getAdvertiserIdentifier());
    }

    @Test
    public void apiservice_should_have_valid_installationid_in_header() throws Exception {
        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout("");
        Response<BaseResolveResponse> response = call.execute();

        Assertions.assertThat(response.raw().request().headers()).isNotNull();
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_INSTALLATION_IDENTIFIER))
                .isEqualTo(realPlatformIdentifier.getDeviceInstallationIdentifier());

    }

    @Test
    public void apiservice_should_have_apitoken_header() throws Exception {
        final String API_TOKEN = "test_api_token";
        realRetrofitApiService.setApiToken(API_TOKEN);
        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout("");
        Response<BaseResolveResponse> response = call.execute();

        Assertions.assertThat(response.raw().request().headers()).isNotNull();
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_XAPIKEY))
                .isEqualTo(API_TOKEN);
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_AUTHORIZATION))
                .isEqualTo(API_TOKEN);
    }

    @Test
    public void apiservice_should_retry_three_times() throws Exception {
        //TODO apiservice_should_retry_three_times
        Assert.fail();
//        final MockWebServer server = new MockWebServer();
//        server.start();
//        server.url("http://test.com/layout");
//        server.enqueue(new MockResponse()
//                .setResponseCode(404));
//
//        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout("");
//        Response<BaseResolveResponse> response = call.execute();
//
//        Assertions.assertThat(server.takeRequest().getPath()).isEqualTo("http://test.com/layout");
//        Assertions.assertThat(server.getRequestCount()).isEqualTo(1);
//        Assertions.assertThat(server.getRequestCount()).isEqualTo(3);
    }

    //    @Test
//    public void apiservice_should_have_apitoken_header() throws Exception {
//        final String API_TOKEN = "test_api_token";
//
//        RetrofitApiServiceImpl retrofitApiService = new SuccessfulRetrofitApiService(mContext, gson, realPlatformIdentifier);
//        retrofitApiService.setApiToken(API_TOKEN);
//
//        Call<BaseResolveResponse> call = retrofitApiService.updateBeaconLayout("layout");
//        Response<BaseResolveResponse> response = call.execute();
//
//        Assertions.assertThat(response.isSuccessful()).isTrue();
//        Assertions.assertThat(response.headers()).isNotNull();
//        Assertions.assertThat(response.headers().get(Transport.HEADER_XAPIKEY))
//                .isEqualTo(API_TOKEN);
//        Assertions.assertThat(response.headers().get(Transport.HEADER_AUTHORIZATION))
//                .isEqualTo(API_TOKEN);
//
////        Assertions.assertThat(server.takeRequest().getPath()).isEqualTo("http://test.com/");
//    }

}
