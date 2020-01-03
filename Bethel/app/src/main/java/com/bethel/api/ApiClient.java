package com.bethel.api;

import com.bethel.model.CurrenciesListModel;
import com.bethel.model.CurrencyModel;
import com.bethel.model.Example;
import com.bethel.model.GenricModel;
import com.bethel.model.ReceiptDetails;
import com.bethel.model.TripModel;
import com.bethel.model.UserModel;
import com.bethel.model.studentbudget.StudentBudgetDetails;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
//import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by Kuljeet Singh on 26/11/14.
 * Usage
 * Retrofit api client
 */
public class ApiClient {
    private static final String ENDPOINT = "http://betheltripreceipts.com/services";

    private static ApiInterface apiInterface;

    public static ApiInterface getApiClient() {
        if (apiInterface == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.serializeNulls();

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(ENDPOINT)
//                    .setClient((Client) getApiClient())
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setErrorHandler(new CustomErrorHandler())
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            request.addHeader("Content-Type","text/html");
                        }
                    })
                    .build();
            apiInterface = restAdapter.create(ApiInterface.class);
        }
        return apiInterface;
    }



    public interface ApiInterface {


        @POST("/add_receipt")
        void addReceipt(@Body JsonObject body,
                        CancelableCallback<UserModel> callback);

        @POST("/get_trip_list")
        void getTripList(@Body JsonObject body,
                         CancelableCallback<TripModel> callback);
        @POST("/login")
        void getLoginData(@Body JsonObject body,
                          CancelableCallback<UserModel> callback);

        @POST("/get_trip_currencies")
        void getTripCurrencyList(@Body JsonObject body,
                                 CancelableCallback<CurrencyModel> callback);

        @POST("/set_trip_currencies")
        void setTripCurrencyList(@Body JsonObject body,
                                 CancelableCallback<GenricModel> callback);

        @POST("/set_trip_budget")
        void setTripBudget(@Body JsonObject body,
                           CancelableCallback<GenricModel> callback);
        @POST("/get_student_budget_details")
        void getStudentBudgetDetails(@Body JsonObject body,
                                     CancelableCallback<StudentBudgetDetails> callback);
        @POST("/get_budget_details")
        void getBudgetDetails(@Body JsonObject body,
                              CancelableCallback<Example> callback);
        @GET("/quote?format=json")
        void getCurrencyList(CancelableCallback<CurrenciesListModel> callback);

        @POST("/get_all_receipts")
        void getAllReceiptsList(@Body JsonObject body,
                                CancelableCallback<ReceiptDetails> callback);

        @POST("/get_user_details")
        void getAllTripMembers(@Body JsonObject body,
                               CancelableCallback<UserModel> callback);

        @POST("/isArchived")
        void getArchiveStatus(@Body JsonObject body,
                                 CancelableCallback<CurrencyModel> callback);

    }
}
