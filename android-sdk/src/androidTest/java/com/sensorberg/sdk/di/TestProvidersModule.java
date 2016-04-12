package com.sensorberg.sdk.di;

import android.app.Application;

import com.sensorberg.di.ProvidersModule;

import dagger.Module;

@Module
public class TestProvidersModule extends ProvidersModule {

    public TestProvidersModule(Application app) {
        super(app);
    }

//    @Provides
//    @Singleton
//    public Clock provideClock() {
    //TODO use this in tests, extract from TestPlatform
//        return new CustomClock();
//    }

}
