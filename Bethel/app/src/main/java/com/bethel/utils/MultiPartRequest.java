package com.bethel.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by kamal on 11/6/2016.
 */

public class MultiPartRequest extends AsyncTask<String, String, String> {
    public static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    public static final MediaType MEDIA_TYPE_PDF = MediaType.parse("application/pdf");
    public static final MediaType MEDIA_TYPE_TXT_DOC = MediaType.parse("text/plain");
    final int TIMEOUT_MILLISEC = 20 * 1000;  // = 20 seconds
    Context context;
    MultiPartRequestListener listener;
    String fileName, filePath, url;
    MediaType mediaType;
    private HashMap<String, Object> bodyParams = new HashMap<>();

    public MultiPartRequest(Context context, MultiPartRequestListener listener, String url, String filePath, MediaType mediaType, HashMap<String, Object> bodyParams) {
        this.context = context;
        this.listener = listener;

        this.filePath = filePath;
        this.mediaType = mediaType;
        this.fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        this.url = url ;//+ "?fileName=" + this.fileName;
        this.bodyParams = bodyParams;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onBegin();
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
//            String oauthToken = OauthPreferences.getOauthToken(context, "oauth_token") + " " + OauthHeaderUtils.getOauthTokenValue(Encryptables.MOBILE_NO, context);

//            OkHttpClient client = new OkHttpClient();
//            client.setConnectTimeout(3, TimeUnit.MINUTES);
//            client.setReadTimeout(3, TimeUnit.MINUTES);

//            Request request = new Request.Builder()
//                    .url(url)
//                    .post(createBody())
//                    .addHeader("Content-Type", "multipart/form-data; boundary=---011000010111000001101001")
//                    .addHeader("Authorization", oauthToken)
//                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.MINUTES)
                    .writeTimeout(3, TimeUnit.MINUTES)
                    .readTimeout(3, TimeUnit.MINUTES)
                    .build();

            MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("title", "Square Logo")
                    .addFormDataPart("receipt_image", "receipt.jpg",
                            RequestBody.create(MEDIA_TYPE_JPEG,new File(filePath)));
            for (Map.Entry<String, Object> entry : bodyParams.entrySet()) {
            requestBodyBuilder.addFormDataPart(entry.getKey(), entry.getValue().toString());
        }
            RequestBody requestBody = requestBodyBuilder.build();
//                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));

//            String result;
//            StringBuilder responseString = new StringBuilder();
//
//            while ((result = reader.readLine()) != null) {
//                responseString = responseString.append(result);
//            }
            Log.d("MultiPartRequest", "********** " + response.body().toString());
            return response.body().toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MultiPartRequest", "********** " + e.getMessage());
            listener.onError();
        }
        return null;
    }



//    public void run() throws Exception {
//        // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
//
//
//        Response response = client.newCall(request).execute();
//        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//        System.out.println(response.body().string());
//    }
    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        listener.onComplete(response);
    }

//    private RequestBody createBody() {
//        MultipartBuilder mb = new MultipartBuilder()
//                .type(MultipartBuilder.FORM)
//                .addFormDataPart("receipt_image", "receipt.jpg", RequestBody.create(mediaType, new File(filePath)));
//
//        for (Map.Entry<String, Object> entry : bodyParams.entrySet()) {
//            mb.addFormDataPart(entry.getKey(), entry.getValue().toString());
//        }
//
//        return mb.build();
//    }

    public interface MultiPartRequestListener {
        public void onBegin();

        public void onError();

        public void onComplete(String response);
    }
}

