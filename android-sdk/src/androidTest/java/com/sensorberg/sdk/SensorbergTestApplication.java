package com.sensorberg.sdk;

import com.sensorberg.SensorbergApplication;
import com.sensorberg.di.Component;
import com.sensorberg.sdk.di.TestComponent;

public class SensorbergTestApplication extends SensorbergApplication {

    @Override
    public Component buildComponentAndInject() {
        return TestComponent.Initializer.init(this);
    }

}
