package com.sensorberg.sdk.di;

import android.app.Application;

import com.sensorberg.di.Component;
import com.sensorberg.di.ProvidersModule;

import javax.inject.Singleton;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Singleton
@dagger.Component(modules = {ProvidersModule.class})
public interface TestComponent extends Component {

    void inject(com.sensorberg.sdk.testUtils.TestPlatform testPlatform);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class Initializer {

        public static TestComponent init(Application app) {
            return DaggerTestComponent.builder()
                    .providersModule(new ProvidersModule(app))
                    .build();
        }
    }

}