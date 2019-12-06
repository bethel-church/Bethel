package com.bethel.api;

import android.util.Log;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Kuljeet singh
 * This class represents the exceptions that occur during the runtime
 * due to the retrofit exceptions(which are most n/w related exceptions)
 */
public class CancelableCallback<T> implements Callback<T> {

    private final Callback callback;

    private boolean canceled;

    public CancelableCallback(Callback callback) {
        this.callback = callback;
        canceled = false;
    }

    public void cancel() {
        canceled = true;
    }


    @Override
    public void success(T t, Response response) {
        if (!canceled) {
            callback.success(t, response);
        } else {
            Log.d(getClass().getSimpleName(), "no callback sent");
        }


    }

    @Override
    public void failure(RetrofitError retrofitError)

    {
        Log.d(getClass().getSimpleName(), "Failed!!!!");
        if (!canceled) {
            callback.failure(retrofitError);
        } else {
            Log.d(getClass().getSimpleName(), "no callback sent");
        }

    }
}