package com.sensorberg;

import android.support.multidex.MultiDexApplication;

import com.sensorberg.di.Component;

import lombok.Getter;
import lombok.Setter;

public class SensorbergApplication extends MultiDexApplication {

    @Getter
    @Setter
    private static Component component;

    @Override
    public void onCreate() {
        super.onCreate();
        setComponent(buildComponentAndInject());
    }

    public Component buildComponentAndInject() {
        return Component.Initializer.init(this);
    }
}
