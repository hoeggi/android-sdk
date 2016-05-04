package com.sensorberg.sdk.internal.transport;

import retrofit2.Call;
import retrofit2.Callback;

public abstract class CallbackWithRetry<T> implements Callback<T> {

    private static final int TOTAL_RETRIES = 3;

    private int retryCount = 0;

    public CallbackWithRetry() {
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (retryCount++ < TOTAL_RETRIES) {
            retry(call);
        }
    }

    private void retry(Call<T> call) {
        call.clone().enqueue(this);
    }
}
