package com.sensorberg.sdk.service;

import com.sensorberg.sdk.ServiceConfiguration;
import com.sensorberg.sdk.internal.FileHelper;
import com.sensorberg.sdk.resolver.ResolverConfiguration;

import org.fest.assertions.api.Assertions;

import android.test.AndroidTestCase;

import java.io.File;

public class TheServiceConfiguration extends AndroidTestCase{

    private static final long[] VIBRATION = new long[]{1, 2, 3, 5, 6, 7};
    ServiceConfiguration tested;
    private ResolverConfiguration resolverConf;
    private String API_TOKEN = "SOMETHING";
    private String ADVERTISING_ID = "SOMETHING_ADVERTISING_ID";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        resolverConf = new ResolverConfiguration();
        resolverConf.setApiToken(API_TOKEN);
        resolverConf.setAdvertisingIdentifier(ADVERTISING_ID);

        tested = new ServiceConfiguration(resolverConf);
    }

    public void test_shoul_be_serializeable() throws Exception {
        File file = File.createTempFile("test" + System.currentTimeMillis(),"tmp");
        FileHelper.write(tested, file);

        ServiceConfiguration deserialized = (ServiceConfiguration) FileHelper.getContentsOfFileOrNull(file);

        Assertions.assertThat(deserialized).isNotNull();
        Assertions.assertThat(deserialized.resolverConfiguration.apiToken).isEqualTo(API_TOKEN);
        Assertions.assertThat(deserialized.resolverConfiguration.getAdvertisingIdentifier()).isEqualTo(ADVERTISING_ID);
        Assertions.assertThat(deserialized.isComplete()).isTrue();
    }
}
