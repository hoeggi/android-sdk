package com.sensorberg.sdk.di;

import android.app.Application;

import com.sensorberg.di.ProvidersModule;

import dagger.Module;

@Module
public class TestProvidersModule extends ProvidersModule {

//    private static final String SENSORBERG_PREFERENCE_IDENTIFIER = "com.sensorberg.preferences";

    public TestProvidersModule(Application app) {
        super(app);
    }

//    @Provides
//    @Singleton
//    public Context provideApplicationContext() {
//        return application;
//    }
//
//    @Provides
//    @Singleton
//    public SharedPreferences provideSettingsSharedPrefs(Context context) {
//        return context.getSharedPreferences(SENSORBERG_PREFERENCE_IDENTIFIER, Context.MODE_PRIVATE);
//    }

}
