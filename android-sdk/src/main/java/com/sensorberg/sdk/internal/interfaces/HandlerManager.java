package com.sensorberg.sdk.internal.interfaces;

import com.sensorberg.sdk.internal.RunLoop;

public interface HandlerManager {

    RunLoop getScannerRunLoop(RunLoop.MessageHandlerCallback callback);

    RunLoop getResolverRunLoop(RunLoop.MessageHandlerCallback callback);

    RunLoop getBeaconPublisherRunLoop(RunLoop.MessageHandlerCallback callback);

}
