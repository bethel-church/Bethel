package com.bethel.api;

import com.bethel.R;
import com.bethel.base.Application;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Kuljeet singh
 * This handles most common network related errors.Throws them if unknown.
 */
public class CustomErrorHandler implements ErrorHandler {

    @Override
    public Throwable handleError(RetrofitError retrofitError) {
        retrofitError.printStackTrace();
        String message = Application.getContext().getString(R.string.something_went);
        String title = "Oops!";
        if (retrofitError.getKind().equals(RetrofitError.Kind.NETWORK)) {
            title = "No Internet Connection!";
            message = Application.getContext().getString(R.string.network_failed);
        } else {
            try {
                Response r = retrofitError.getResponse();
                if (r != null && r.getStatus() == 408) {
                    title = "Alert!";
                    message = Application.getContext().getString(R.string.timed_out);
                } else if (r.getStatus() == 500) {
                    message = Application.getContext().getString(R.string.internal_error);
                    title = "Sorry!";
                } else if (r.getStatus() == 404) {
                    title = Application.getContext().getString(R.string.alert);
                    message = Application.getContext().getString(R.string.something_went);
                }
            } catch (Exception exception) {
                // something went wrong
                message = Application.getContext().getString(R.string.something_went);
                title = Application.getContext().getString(R.string.alert);
            }
        }

        return new Exception(message + "-" + title);
    }
}

