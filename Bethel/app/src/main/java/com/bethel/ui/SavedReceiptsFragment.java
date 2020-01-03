package com.bethel.ui;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.constants.ApiConstants;
import com.bethel.database.AppDatabase;
import com.bethel.interfaces.UpdateSaveReceiptCallback;
import com.bethel.model.CurrenciesListModel;
import com.bethel.model.ReceiptDetails;
import com.bethel.model.ReceiptModel;
import com.bethel.model.Transaction;
import com.bethel.model.User;
import com.bethel.swipelist.SwipeMenu;
import com.bethel.swipelist.SwipeMenuCreator;
import com.bethel.swipelist.SwipeMenuItem;
import com.bethel.swipelist.SwipeMenuListView;
import com.bethel.utils.MultiPartRequest;
import com.bethel.utils.ProgressBarHandler;
import com.bethel.utils.SharedPreferencesHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;

import static com.bethel.utils.CommonUtils.isNetworkAvailable;
import static com.bethel.utils.CommonUtils.isNetworkAvailableWithNoDialog;

public class SavedReceiptsFragment extends Fragment implements MultiPartRequest.MultiPartRequestListener {

    private List<ApplicationInfo> mAppList;
    private AppAdapter mAdapter;
    private SwipeMenuListView mListView;
    private double price,selectedPrice;
    private String category;
    private String desc,strDate;
    static String path;
    private TextView uploadallbtn;
    private Cursor cursor;
    ProgressBar progressBarall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uploaded_receipts,container,false);
    }
    public void applyFilters(){
        fetchReceipts();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareViews(getView());
    }

    private void prepareViews(View view) {
        mListView = (SwipeMenuListView) view.findViewById(R.id.listView);
        uploadallbtn=(TextView)view.findViewById(R.id.uploadallbtn);
        uploadallbtn.setVisibility(View.VISIBLE);
        progressBarall=(ProgressBar)view.findViewById(R.id.progressall);
        uploadallbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //     cursor.moveToFirst();
                if (isNetworkAvailable(getActivity())) {
                    receiptCount = 0;
                    uploadallbtn.setVisibility(View.GONE);
                    progressBarall.setVisibility(View.VISIBLE);
                    progressBarall.setProgress(3);
                 //   progressBarall.setProgress(20);
                    isUploadAll = true;
//                receiptCount=receiptDetailsList.getReceipts().size();
                    uploadAllReceipt();
                }

            }
        });




        fetchReceipts();
    }

    boolean isUploadAll=false;

    private void submitReceipt() {
//        JsonObject jsonRequest = new JsonObject();
//Integer.valueOf(SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, ApiConstants.TRIP_ID))
        HashMap<String, Object> values = new HashMap<>();
        values.put("trip_id", SharedPreferencesHandler.getStringValues(getActivity(), ApiConstants.TRIP_ID));
        if(price <= 0)
        {
            price = selectedPrice;
        }
        try {
            values.put("price_usd",  String.format("%.2f", price));
        } catch (Exception e) {
            values.put("price_usd", price);
        }
        values.put("price_usd", price);
        values.put("currency", currency);
        values.put("type", category);
        values.put("description", desc);
        values.put("receipt_date", strDate);
        values.put("first_name", SharedPreferencesHandler.getStringValues(getActivity(), "First_Name"));
        values.put("middle_name", SharedPreferencesHandler.getStringValues(getActivity(), "Middle_Name"));
        values.put("last_name", SharedPreferencesHandler.getStringValues(getActivity(), "Last_Name"));
        values.put("price_other_currency", selectedPrice);

        String url;
        url = ApiClient.ENDPOINT + "/add_receipt";
        hitMultipartRequest(getActivity(), url, this, values);
    }

    public static void hitMultipartRequest(Context context, String url, MultiPartRequest.MultiPartRequestListener receiver, HashMap<String, Object> bodyParams) {

        String fileExt = path.substring(path.lastIndexOf(".") + 1, path.toString().length());

        if (fileExt.equalsIgnoreCase("png")) {
            new MultiPartRequest(context, receiver, url,path, MultiPartRequest.MEDIA_TYPE_PNG, bodyParams).execute();
        } else if (fileExt.equalsIgnoreCase("jpg") || fileExt.equalsIgnoreCase("jpeg")) {
            new MultiPartRequest(context, receiver, url, path, MultiPartRequest.MEDIA_TYPE_JPEG, bodyParams).execute();
        }
    }
    public void fetchReceipts() {
        showProgress();
        AppDatabase appDatabase=new AppDatabase(getActivity());

        Cursor cursor=appDatabase.getRowCount(SharedPreferencesHandler.getStringValues(getActivity(),ApiConstants.TRIP_ID));
        ((ViewReceiptsActivity)getActivity()).updateSavedTabsCount(cursor.getCount());
        hideProgress();
        mListView.setVisibility(View.GONE);
        if(cursor.getCount()>0){
            uploadallbtn.setVisibility(View.VISIBLE);
            if(!isUploadAll){
                progressBarall.setVisibility(View.GONE);
            }
            ((TextView)getView().findViewById(R.id.nodatatv)).setVisibility(View.GONE);
            parseApiResponse(cursor);
        }else{
            uploadallbtn.setVisibility(View.GONE);
            progressBarall.setVisibility(View.GONE);
            ((TextView)getView().findViewById(R.id.nodatatv)).setVisibility(View.VISIBLE);
        }


    }

    /*  private void parseApiResponse(Cursor cursor) {
          this.cursor=cursor;
           setListviewValues(cursor);
           hideProgress();
      }*/
    ReceiptDetails receiptDetailsList;
    private void parseApiResponse(Cursor cursor) {
//       setListviewValues(cursor);

        receiptDetailsList = new ReceiptDetails();
        ArrayList<Transaction> transactions = new ArrayList<>();
        ArrayList<User> users = new ArrayList<>();
        ArrayList<ReceiptModel> receiptModels = new ArrayList<>();
        while (cursor.moveToNext()) {


            Transaction transaction = new Transaction();

//          transaction.setId(cursor.getString(cursor.getColumnIndex("id")));
//            transaction.setCreated(jsonTransactionObj.getString("created"));
            transaction.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            transaction.setForeign_currency(cursor.getString(cursor.getColumnIndex("currency")));
//            transaction.setForeign_currency_amount(jsonTransactionObj.getString("foreign_currency_amount"));
//            transaction.setIs_edited(jsonTransactionObj.getString("is_edited"));
            transaction.setReceipt(cursor.getString(cursor.getColumnIndex("imagepath")));
            transaction.setReceipt_date(cursor.getString(cursor.getColumnIndex("date")));
            transaction.setTrip_id(cursor.getString(cursor.getColumnIndex("trip_id")));
            transaction.setType(cursor.getString(cursor.getColumnIndex("category")));
            transaction.setUsd(cursor.getString(cursor.getColumnIndex("price")));
            transaction.setSno(cursor.getInt(cursor.getColumnIndex("sno")));
//            transaction.setUser_id(cursor.getString(cursor.getColumnIndex("price")));
            transactions.add(transaction);
            User user = new User();
            user.setFirst_name(cursor.getString(cursor.getColumnIndex("firstname")));
            user.setMiddle_name(cursor.getString(cursor.getColumnIndex("middlename")));
            user.setLast_name(cursor.getString(cursor.getColumnIndex("lastname")));
            users.add(user);
            ReceiptModel receiptModel = new ReceiptModel();
            receiptModel.setTransaction(transaction);
            receiptModel.setUser(user);

            if (SharedPreferencesHandler.getSharedPreferences(getActivity()).contains("selectcat")) {
                final List<String> items = new LinkedList<String>(Arrays.asList(SharedPreferencesHandler.getStringValues(getActivity(), "selectcat").split("\\s*,\\s*")));

                for (int i = 0; i < items.size(); i++) {
                    if (transaction.getType().equalsIgnoreCase(items.get(i))|items.get(i).equalsIgnoreCase("All")) {
                        {
                            if (SharedPreferencesHandler.getSharedPreferences(getActivity()).contains("showmyreceipts")) {
                                if (SharedPreferencesHandler.getBooleanValues(getActivity(), "showmyreceipts")) {
                                    if (SharedPreferencesHandler.getStringValues(getActivity(), "first_name").equalsIgnoreCase(user.getFirst_name())) {

                                        if (!SharedPreferencesHandler.getBooleanValues(getActivity(), "alldate")) {
                                            String dateformat = "yyyy-MM-dd"; //In which you need put here
                                            SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                            Date date1 = new Date();
                                            Date date2 = new Date();
                                            Date dateToCompare = new Date();
                                            try {
                                                date1 = sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(), "startDate"));
                                                date2 = sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(), "endDate"));
                                                dateToCompare = sdf.parse(transaction.getReceipt_date());
                                                if (dateToCompare.getTime() == date1.getTime()) {
                                                    receiptModels.add(receiptModel);
                                                } else if (dateToCompare.getTime() == date2.getTime()) {
                                                    receiptModels.add(receiptModel);
                                                } else if (dateToCompare.after(date1) && dateToCompare.before(date2)) {
                                                    receiptModels.add(receiptModel);
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            receiptModels.add(receiptModel);
                                        }
                                    }
                                } else {

                                    if (!SharedPreferencesHandler.getBooleanValues(getActivity(), "alldate")) {
                                        String dateformat = "yyyy-MM-dd"; //In which you need put here
                                        SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                        Date date1 = new Date();
                                        Date date2 = new Date();
                                        Date dateToCompare = new Date();
                                        try {
                                            date1 = sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(), "startDate"));
                                            date2 = sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(), "endDate"));
                                            dateToCompare = sdf.parse(transaction.getReceipt_date());
                                            if (dateToCompare.getTime() == date1.getTime()) {
                                                receiptModels.add(receiptModel);
                                            } else if (dateToCompare.getTime() == date2.getTime()) {
                                                receiptModels.add(receiptModel);
                                            } else if (dateToCompare.after(date1) && dateToCompare.before(date2)) {
                                                receiptModels.add(receiptModel);
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        receiptModels.add(receiptModel);
                                    }

                                }
                            } else {
                                if (!SharedPreferencesHandler.getBooleanValues(getActivity(), "alldate")) {
                                    String dateformat = "yyyy-MM-dd"; //In which you need put here
                                    SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                    Date date1 = new Date();
                                    Date date2 = new Date();
                                    Date dateToCompare = new Date();
                                    try {
                                        date1 = sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(), "startDate"));
                                        date2 = sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(), "endDate"));
                                        dateToCompare = sdf.parse(transaction.getReceipt_date());
                                        if (dateToCompare.getTime() == date1.getTime()) {
                                            receiptModels.add(receiptModel);
                                        } else if (dateToCompare == date2) {
                                            receiptModels.add(receiptModel);
                                        } else if (dateToCompare.after(date1) && dateToCompare.before(date2)) {
                                            receiptModels.add(receiptModel);
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    receiptModels.add(receiptModel);
                                }
                            }
                                   /*if (SharedPreferencesHandler.getStringValues(getActivity(), "selectcat").
                            equalsIgnoreCase("All") || SharedPreferencesHandler.getStringValues(getActivity(), "selectcat").
                            equalsIgnoreCase(""))*/ /*{

                    if (SharedPreferencesHandler.getSharedPreferences(getActivity()).contains("showmyreceipts")) {
                        if (SharedPreferencesHandler.getBooleanValues(getActivity(), "showmyreceipts")) {
                            if (SharedPreferencesHandler.getStringValues(getActivity(), "first_name").equalsIgnoreCase(user.getFirst_name())) {

                                if(!SharedPreferencesHandler.getBooleanValues(getActivity(),"alldate")){
                                    String dateformat = "yyyy-MM-dd"; //In which you need put here
                                    SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                    Date date1 = new Date();
                                    Date date2 = new Date();
                                    Date dateToCompare = new Date();
                                    try {
                                        date1= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"startDate"));
                                        date2= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"endDate"));
                                        dateToCompare= sdf.parse(transaction.getReceipt_date());
                                        if(dateToCompare.getTime()==date1.getTime()){
                                            receiptModels.add(receiptModel);
                                        }else if(dateToCompare.getTime()==date2.getTime()){
                                            receiptModels.add(receiptModel);
                                        }else if(dateToCompare.after(date1)&&dateToCompare.before(date2)){
                                            receiptModels.add(receiptModel);
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }else{
                                    receiptModels.add(receiptModel);
                                }
                            }
                        } else {

                            if(!SharedPreferencesHandler.getBooleanValues(getActivity(),"alldate")){
                                String dateformat = "yyyy-MM-dd"; //In which you need put here
                                SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                Date date1 = new Date();
                                Date date2 = new Date();
                                Date dateToCompare = new Date();
                                try {
                                    date1= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"startDate"));
                                    date2= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"endDate"));
                                    dateToCompare= sdf.parse(transaction.getReceipt_date());
                                    if(dateToCompare.getTime()==date1.getTime()){
                                        receiptModels.add(receiptModel);
                                    }else if(dateToCompare.getTime()==date2.getTime()){
                                        receiptModels.add(receiptModel);
                                    }else if(dateToCompare.after(date1)&&dateToCompare.before(date2)){
                                        receiptModels.add(receiptModel);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                receiptModels.add(receiptModel);
                            }

                        }
                    } else {
                        if(!SharedPreferencesHandler.getBooleanValues(getActivity(),"alldate")){
                            String dateformat = "yyyy-MM-dd"; //In which you need put here
                            SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                            Date date1 = new Date();
                            Date date2 = new Date();
                            Date dateToCompare = new Date();
                            try {
                                date1= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"startDate"));
                                date2= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"endDate"));
                                dateToCompare= sdf.parse(transaction.getReceipt_date());
                                if(dateToCompare.getTime()==date1.getTime()){
                                    receiptModels.add(receiptModel);
                                }else if(dateToCompare==date2){
                                    receiptModels.add(receiptModel);
                                }else if(dateToCompare.after(date1)&&dateToCompare.before(date2)){
                                    receiptModels.add(receiptModel);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }else{
                            receiptModels.add(receiptModel);
                        }
                    }
                }*/
                        }//else
                        /*{
                    if (transaction.getType().equalsIgnoreCase(SharedPreferencesHandler.getStringValues(getActivity(), "selectcat"))) {
                        if (SharedPreferencesHandler.getSharedPreferences(getActivity()).contains("showmyreceipts")) {
                            if (SharedPreferencesHandler.getBooleanValues(getActivity(), "showmyreceipts")) {
                                if (SharedPreferencesHandler.getStringValues(getActivity(), "first_name").equalsIgnoreCase(user.getFirst_name()))
                                {
                                    if(!SharedPreferencesHandler.getBooleanValues(getActivity(),"alldate")){
                                        String dateformat = "yyyy-MM-dd"; //In which you need put here
                                        SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                        Date date1 = new Date();
                                        Date date2 = new Date();
                                        Date dateToCompare = new Date();
                                        try {
                                            date1= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"startDate"));
                                            date2= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"endDate"));
                                            dateToCompare= sdf.parse(transaction.getReceipt_date());
                                            if(dateToCompare.getTime()==date1.getTime()){
                                                receiptModels.add(receiptModel);
                                            }else if(dateToCompare.getTime()==date2.getTime()){
                                                receiptModels.add(receiptModel);
                                            }else if(dateToCompare.after(date1)&&dateToCompare.before(date2)){
                                                receiptModels.add(receiptModel);
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }else{
                                        receiptModels.add(receiptModel);
                                    }
                                }
                            } else {
                                if(!SharedPreferencesHandler.getBooleanValues(getActivity(),"alldate")){
                                    String dateformat = "yyyy-MM-dd"; //In which you need put here
                                    SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                    Date date1 = new Date();
                                    Date date2 = new Date();
                                    Date dateToCompare = new Date();
                                    try {
                                        date1= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"startDate"));
                                        date2= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"endDate"));
                                        dateToCompare= sdf.parse(transaction.getReceipt_date());
                                        if(dateToCompare.getTime()==date1.getTime()){
                                            receiptModels.add(receiptModel);
                                        }else if(dateToCompare.getTime()==date2.getTime()){
                                            receiptModels.add(receiptModel);
                                        }else if(dateToCompare.after(date1)&&dateToCompare.before(date2)){
                                            receiptModels.add(receiptModel);
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }else{
                                    receiptModels.add(receiptModel);
                                }
                            }
                        } else {
                            if(!SharedPreferencesHandler.getBooleanValues(getActivity(),"alldate")){
                                String dateformat = "yyyy-MM-dd"; //In which you need put here
                                SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                Date date1 = new Date();
                                Date date2 = new Date();
                                Date dateToCompare = new Date();
                                try {
                                    date1= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"startDate"));
                                    date2= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"endDate"));
                                    dateToCompare= sdf.parse(transaction.getReceipt_date());
                                    if(dateToCompare.getTime()==date1.getTime()){
                                        receiptModels.add(receiptModel);
                                    }else if(dateToCompare.getTime()==date2.getTime()){
                                        receiptModels.add(receiptModel);
                                    }else if(dateToCompare.after(date1)&&dateToCompare.before(date2)){
                                        receiptModels.add(receiptModel);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                receiptModels.add(receiptModel);
                            }
                        }
                    }

                }*/
                    }
                }
            }else {
                if (SharedPreferencesHandler.getSharedPreferences(getActivity()).contains("showmyreceipts")) {
                    if (SharedPreferencesHandler.getBooleanValues(getActivity(), "showmyreceipts")) {
                        if (SharedPreferencesHandler.getStringValues(getActivity(), "first_name").equalsIgnoreCase(user.getFirst_name())) {
                            if (!SharedPreferencesHandler.getBooleanValues(getActivity(), "alldate")) {
                                String dateformat = "yyyy-MM-dd"; //In which you need put here
                                SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                Date date1 = new Date();
                                Date date2 = new Date();
                                Date dateToCompare = new Date();
                                try {
                                    date1 = sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(), "startDate"));
                                    date2 = sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(), "endDate"));
                                    dateToCompare = sdf.parse(transaction.getReceipt_date());
                                    if (dateToCompare.getTime() == date1.getTime()) {
                                        receiptModels.add(receiptModel);
                                    } else if (dateToCompare.getTime() == date2.getTime()) {
                                        receiptModels.add(receiptModel);
                                    } else if (dateToCompare.after(date1) && dateToCompare.before(date2)) {
                                        receiptModels.add(receiptModel);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                receiptModels.add(receiptModel);
                            }

                        }
                    }else{
                        if(!SharedPreferencesHandler.getBooleanValues(getActivity(),"alldate")){
                            String dateformat = "yyyy-MM-dd"; //In which you need put here
                            SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                            Date date1 = new Date();
                            Date date2 = new Date();
                            Date dateToCompare = new Date();
                            try {
                                date1= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"startDate"));
                                date2= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"endDate"));
                                dateToCompare= sdf.parse(transaction.getReceipt_date());
                                if(dateToCompare.getTime()==date1.getTime()){
                                    receiptModels.add(receiptModel);
                                }else if(dateToCompare.getTime()==date2.getTime()){
                                    receiptModels.add(receiptModel);
                                }else if(dateToCompare.after(date1)&&dateToCompare.before(date2)){
                                    receiptModels.add(receiptModel);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                receiptModels.add(receiptModel);
                            }
                        }else{
                            receiptModels.add(receiptModel);
                        }
                    }
                }else{
                    if(SharedPreferencesHandler.getStringValues(getActivity(),"startDate")!=null){
                        if(!SharedPreferencesHandler.getBooleanValues(getActivity(),"alldate")){
                            String dateformat = "yyyy-MM-dd"; //In which you need put here
                            SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                            Date date1 = new Date();
                            Date date2 = new Date();
                            Date dateToCompare = new Date();
                            try {
                                date1= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"startDate"));
                                date2= sdf.parse(SharedPreferencesHandler.getStringValues(getActivity(),"endDate"));
                                dateToCompare= sdf.parse(transaction.getReceipt_date());
                                if(dateToCompare.getTime()==date1.getTime()){
                                    receiptModels.add(receiptModel);
                                }else if(dateToCompare.getTime()==date2.getTime()){
                                    receiptModels.add(receiptModel);
                                }else if(dateToCompare.after(date1)&&dateToCompare.before(date2)){
                                    receiptModels.add(receiptModel);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                receiptModels.add(receiptModel);
                            }
                        }else{
                            receiptModels.add(receiptModel);
                        }
                    }else{
                        receiptModels.add(receiptModel);
                    }

                }
            }
        }

        receiptDetailsList.setReceipts(receiptModels);
        ((ViewReceiptsActivity)getActivity()).updateSavedTabsCount(receiptDetailsList.getReceipts().size());
        setListviewValues(receiptDetailsList);

        hideProgress();
        if(isUploadAll){
            // receiptCount++;
            receiptCount=0;

            uploadallbtn.setVisibility(View.GONE);
            if(progressBarall.getProgress()==0){

                progressBarall.setProgress(19);

            }else {
                if((progressBarall.getProgress()+19)<100){
                    progressBarall.setProgress((progressBarall.getProgress()+19));
                }
            }
            uploadAllReceipt();
        }else {
        //    uploadallbtn.setVisibility(View.GONE);
            isUploadAll = false;

        }

    }



  /*  private void setListviewValues(final ReceiptDetails receiptDetails) {


//        mListView.setVisibility(View.GONE);
        mAdapter = new AppAdapter(receiptDetails);

        mListView.setAdapter(mAdapter);
        mListView.setVisibility(View.VISIBLE);
        if(receiptDetails.getReceipts().size()>0){
            uploadallbtn.setVisibility(View.VISIBLE);
        }
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                //   menu.setViewType(R.layout.view_receipts);
                // create "open" item
                SwipeMenuItem receiptitem = new SwipeMenuItem(
                        getActivity());

                // set item background
                receiptitem.setBackground(new ColorDrawable(Color.parseColor("#AFAFAF")));
                // set item width
                receiptitem.setWidth(dp2px(90));
                receiptitem.setIcon(R.drawable.receipt);
                // add to menu
                menu.addMenuItem(receiptitem);

                SwipeMenuItem edititem = new SwipeMenuItem(
                        getActivity());

                // set item background
                edititem.setBackground(new ColorDrawable(Color.parseColor("#4498A1")));
                // set item width
                edititem.setWidth(dp2px(90));
                edititem.setIcon(R.drawable.edit);
                // add to menu
                menu.addMenuItem(edititem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.parseColor("#D1213E")));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.delete);
                // set item title
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mListView.setMenuCreator(creator);

        // step 2. listener item click event
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                ApplicationInfo item = mAppList.get(position);
                switch (index) {
                    case 0:
                        // open
                        // open(item);

                        Intent in1 = new Intent(getActivity(), PhotoViewerActivity.class);
                        cursor.moveToPosition(position);
                        in1.putExtra("isImage",cursor.getString(cursor.getColumnIndex("imagepath")));
                        //  in1.putExtra("image",byteArray);
                        startActivity(in1);
                        break;
                    case 1:
                        // Edit
                        // open(item);
                        Intent editIntent = new Intent(getActivity(), AddReceiptActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putBoolean("isEdit",true);
                        bundle.putBoolean("isCursor",true);
                        cursor.moveToPosition(position);
                        bundle.putString("desc",receiptDetails.getReceipts().get(position).getTransaction().getDescription());
                        bundle.putString("price",receiptDetails.getReceipts().get(position).getTransaction().getUsd());
                        bundle.putString("currency",receiptDetails.getReceipts().get(position).getTransaction().getForeign_currency());
                        bundle.putString("category",receiptDetails.getReceipts().get(position).getTransaction().getType());
                        bundle.putString("date",receiptDetails.getReceipts().get(position).getTransaction().getReceipt_date());
                        bundle.putString("imagepath",receiptDetails.getReceipts().get(position).getTransaction().getReceipt());
                        bundle.putInt("sno",receiptDetails.getReceipts().get(position).getTransaction().getSno());
                        bundle.putBoolean("isFromReceipt",true);
                        editIntent.putExtras(bundle);
                        startActivity(editIntent);
                        ((ViewReceiptsActivity)getActivity()).finish();
                        break;
                    case 2:
                        // delete
                        cursor.moveToPosition(position);
                        AppDatabase appDatabase=new AppDatabase(getActivity());
                        appDatabase.deleteRowValues(cursor.getString(cursor.getColumnIndex("sno")));
                        fetchReceipts();
                        ((ViewReceiptsActivity)getActivity()).updateSavedTabsCount(
                                appDatabase.getRowCount(SharedPreferencesHandler.getStringValues(getActivity(),ApiConstants.TRIP_ID)).getCount()
                        );

//					delete(item);
//                        deleteReceipt(receiptDetails.getReceipts().get(position).getTransaction().getId(),position,receiptDetails);
                        //receiptDetails.getReceipts().remove(position);
                        // mAdapter.notifyDataSetChanged();
                        //  mAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });
    }
*/

    class AppAdapter extends BaseAdapter {
        private   LayoutInflater inflater=null;
        ReceiptDetails receiptDetails;
        DecimalFormat f;
        public AppAdapter(ReceiptDetails receiptDetails){
            inflater = ( LayoutInflater )getActivity().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.receiptDetails=receiptDetails;
            f = new DecimalFormat("##.00");
        }

        @Override
        public int getCount() {
            return receiptDetails.getReceipts().size();
        }

        @Override
        public ApplicationInfo getItem(int position) {
            return mAppList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_list_app, null);
                holder = new ViewHolder();
                holder.tv_name=(TextView)convertView.findViewById(R.id.tv_name);
                holder.receiptdatetv=(TextView)convertView.findViewById(R.id.receiptdate);
                holder.pricetv=(TextView)convertView.findViewById(R.id.pricetv);
                holder.currencyname=(TextView)convertView.findViewById(R.id.currencyname);
                holder.receipttype=(TextView)convertView.findViewById(R.id.receipttype);
                holder.desctv=(TextView)convertView.findViewById(R.id.desctv);
                holder.btnUpload=(TextView)convertView.findViewById(R.id.uploadbtn);
                holder.progressBar=(ProgressBar)convertView.findViewById(R.id.progress);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
          /*  holder.tv_name.setText(receiptDetails.getReceipts().get(position).
                    getUser().getFirst_name() + " " + receiptDetails.getReceipts().get(position).
                    getUser().getMiddle_name() + " " + receiptDetails.getReceipts().get(position).
                    getUser().getLast_name());*/

            String name=receiptDetails.getReceipts().get(position).
                    getUser().getFirst_name() + " " + receiptDetails.getReceipts().get(position).
                    getUser().getMiddle_name() + " " + receiptDetails.getReceipts().get(position).
                    getUser().getLast_name();
            String storedName= SharedPreferencesHandler.getStringValues(getActivity(), "First_Name")+" "+
                    SharedPreferencesHandler.getStringValues(getActivity(), "Middle_Name")+" "+
                    SharedPreferencesHandler.getStringValues(getActivity(), "Last_Name");
            if(storedName.equalsIgnoreCase(name)){
                holder.tv_name.setText("Me");
            }else{

                holder.tv_name.setText(name);
            }
            holder.receipttype.setText(receiptDetails.getReceipts().get(position).
                    getTransaction().getType());
           /* holder.receiptdatetv.setText(receiptDetails.getReceipts().get(position).
                    getTransaction().getReceipt_date());
           */
            holder.receiptdatetv.setText(parseTodaysDate(receiptDetails.getReceipts().get(position).
                    getTransaction().getReceipt_date()));

            holder.pricetv.setText("$" + f.format(Double.valueOf(receiptDetails.getReceipts().get(position).
                    getTransaction().getUsd())));
            /*holder.currencyname.setText(receiptDetails.getReceipts().get(position).getTransaction().getForeign_currency());
            */
            holder.currencyname.setText("USD");

            holder.desctv.setText(receiptDetails.getReceipts().get(position).
                    getTransaction().getDescription());


            holder.btnUpload.setVisibility(View.VISIBLE);
            final ViewHolder finalHolder = holder;
            holder.btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isNetworkAvailableWithNoDialog(getActivity())){
//                        currency=cursor.getString(cursor.getColumnIndex("currency"));
                        currency=receiptDetails.getReceipts().get(position).getTransaction().getForeign_currency();
                        selectedPrice=  Double.valueOf(receiptDetails.getReceipts().get(position).getTransaction().getUsd());
                        category=receiptDetails.getReceipts().get(position).getTransaction().getType();
                        desc=receiptDetails.getReceipts().get(position).getTransaction().getDescription();
                        strDate=receiptDetails.getReceipts().get(position).getTransaction().getReceipt_date();
                        path=receiptDetails.getReceipts().get(position).getTransaction().getReceipt();
                        sno=receiptDetails.getReceipts().get(position).getTransaction().getSno();
                        finalHolder.progressBar.setVisibility(View.VISIBLE);
                        finalHolder.btnUpload.setVisibility(View.GONE);
                        finalHolder.progressBar.setProgress(10);
                       receiptCount=position;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finalHolder.progressBar.setProgress(70);
                            }
                        },3000);
                        addReceipt();
                    }else{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.datepicker));
                        builder.setMessage("We are having trouble uploading receipt right now.Please make sure you are connected to internet and try again.");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();
                    }


                }
            });




            return convertView;
        }
        public  String parseTodaysDate(String time) {



            String inputPattern = "yyyy-MM-dd hh:mm";

            String outputPattern = "MM/d/yy";

            time=(time.replace("am","")).replace("pm","").toString().trim();
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.US);

            Date date = null;
            String str = null;

            try {
                date = inputFormat.parse(time);
                str = outputFormat.format(date);

                Log.i("mini", "Converted Date Today:" + str);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return str;
        }
        class ViewHolder {
            TextView tv_name,receiptdatetv,pricetv,currencyname,receipttype,desctv,btnUpload;
            ProgressBar progressBar;
        }
        public   double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();
            long factor = (long) Math.pow(10, places);
            value = value * factor;
            long tmp = Math.round(value);
            return (double) tmp / factor;
        }
    }




    private void setListviewValues(final ReceiptDetails receiptDetails) {
        mAppList = getActivity().getPackageManager().getInstalledApplications(0);
      /*  mAdapter = new AppAdapter(cursor);
        mListView.setAdapter(mAdapter);*/
        mAdapter = new AppAdapter(receiptDetails);

        mListView.setAdapter(mAdapter);

        mListView.setVisibility(View.VISIBLE);
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                //   menu.setViewType(R.layout.view_receipts);
                // create "open" item
                SwipeMenuItem receiptitem = new SwipeMenuItem(
                        getActivity());

                // set item background
                receiptitem.setBackground(new ColorDrawable(Color.parseColor("#AFAFAF")));
                // set item width
                receiptitem.setWidth(dp2px(90));
                receiptitem.setIcon(R.drawable.receipt);
                // add to menu
                menu.addMenuItem(receiptitem);

                SwipeMenuItem edititem = new SwipeMenuItem(
                        getActivity());

                // set item background
                edititem.setBackground(new ColorDrawable(Color.parseColor("#4498A1")));
                // set item width
                edititem.setWidth(dp2px(90));
                edititem.setIcon(R.drawable.edit);
                // add to menu
                menu.addMenuItem(edititem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.parseColor("#D1213E")));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.delete);
                // set item title
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mListView.setMenuCreator(creator);

        // step 2. listener item click event
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                ApplicationInfo item = mAppList.get(position);
                switch (index) {
                    case 0:
                        // open
                        // open(item);

                        Intent in1 = new Intent(getActivity(), PhotoViewerActivity.class);
//                        cursor.moveToPosition(position);
                        in1.putExtra("isImage",receiptDetails.getReceipts().get(position).getTransaction().getReceipt());
                        //  in1.putExtra("image",byteArray);
                        startActivity(in1);
                        break;
                    case 1:
                        // Edit
                        // open(item);
                        Intent editIntent = new Intent(getActivity(), AddReceiptActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putBoolean("isEdit",true);
                        bundle.putBoolean("isCursor",true);
                        //cursor.moveToPosition(position);
                        bundle.putString("desc",receiptDetails.getReceipts().get(position).getTransaction().getDescription());
                        bundle.putString("price",receiptDetails.getReceipts().get(position).getTransaction().getUsd());
                        bundle.putString("currency",receiptDetails.getReceipts().get(position).getTransaction().getForeign_currency());
                        bundle.putString("category",receiptDetails.getReceipts().get(position).getTransaction().getType());
                        bundle.putString("date",receiptDetails.getReceipts().get(position).getTransaction().getReceipt_date());
                        bundle.putString("imagepath",receiptDetails.getReceipts().get(position).getTransaction().getReceipt());
                        bundle.putInt("sno",receiptDetails.getReceipts().get(position).getTransaction().getSno());
                        bundle.putBoolean("isFromReceipt",true);
                        editIntent.putExtras(bundle);
                        startActivity(editIntent);
                        ((ViewReceiptsActivity)getActivity()).finish();
                        break;
                    case 2:
                        // delete
                        // cursor.moveToPosition(position);
                        new android.app.AlertDialog.Builder(getActivity(),R.style.datepicker)
                                .setMessage("Are you sure you want to delete this receipt.")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        AppDatabase appDatabase=new AppDatabase(getActivity());
                                        appDatabase.deleteRowValues(receiptDetails.getReceipts().get(position).getTransaction().getSno());
                                        fetchReceipts();
                                        dialog.dismiss();

                                    }
                                })
                                .setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();


                       /* ((ViewReceiptsActivity)getActivity()).updateSavedTabsCount(
                                appDatabase.getRowCount(SharedPreferencesHandler.getStringValues(getActivity(),ApiConstants.TRIP_ID)).getCount()
                        );*/

//					delete(item);
//                        deleteReceipt(receiptDetails.getReceipts().get(position).getTransaction().getId(),position,receiptDetails);
                        //receiptDetails.getReceipts().remove(position);
                        // mAdapter.notifyDataSetChanged();
                        //  mAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });
    }



    private ProgressBarHandler progress;
    public void showProgress() {
        if(progress == null){
            progress = new ProgressBarHandler(getActivity());

        }
        progress.show();
    }

    public void hideProgress() {
        if(progress!=null)
            progress.hide();
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onBegin() {

    }

    @Override
    public void onError() {

    }

    void uploadAllReceipt(){
        if(isNetworkAvailableWithNoDialog(getActivity())){
//            currency=cursor.getString(cursor.getColumnIndex("currency"));
            currency=receiptDetailsList.getReceipts().get(receiptCount).getTransaction().getForeign_currency();
            //selectedPrice=  Double.valueOf(cursor.getString(cursor.getColumnIndex("price")));
            selectedPrice=Double.valueOf(receiptDetailsList.getReceipts().get(receiptCount).getTransaction().getUsd());
            //category=cursor.getString(cursor.getColumnIndex("category"));
            category= receiptDetailsList.getReceipts().get(receiptCount).getTransaction().getType();
            //   desc=cursor.getString(cursor.getColumnIndex("description"));
            desc=receiptDetailsList.getReceipts().get(receiptCount).getTransaction().getDescription();
//            strDate=cursor.getString(cursor.getColumnIndex("date"));
            strDate=receiptDetailsList.getReceipts().get(receiptCount).getTransaction().getReceipt_date();
            // path=cursor.getString(cursor.getColumnIndex("imagepath"));
            path=receiptDetailsList.getReceipts().get(receiptCount).getTransaction().getReceipt();
//            sno=cursor.getInt(cursor.getColumnIndex("sno"));
            sno=receiptDetailsList.getReceipts().get(receiptCount).getTransaction().getSno();

           // progressBarall.setProgress(20);
            addReceipt();
        }else{
            final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.datepicker));
            builder.setMessage("We are having trouble uploading receipt right now.Please make sure you are connected to internet and try again.");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }

    }
    int receiptCount=0;

    @Override
    public void onComplete(String response) {
        Log.e("","");
        hideProgress();
        AppDatabase appDatabase=new AppDatabase(getActivity());
        appDatabase.deleteRowValues(sno);
        try {
            ((UpdateSaveReceiptCallback)getActivity()).updateSaveFragment();
            ((ViewReceiptsActivity)getActivity()).addSaveCount();

        }catch (Exception e){
        e.printStackTrace();
        }
        fetchReceipts();

        //hideProgress();
    }

    /* class AppAdapter extends BaseAdapter {
         private   LayoutInflater inflater=null;
         Cursor cursor;
         public AppAdapter(Cursor cursor){
             inflater = ( LayoutInflater )getActivity().
                     getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             this.cursor=cursor;
         }

         @Override
         public int getCount() {
             return cursor.getCount();
         }

         @Override
         public ApplicationInfo getItem(int position) {
             return mAppList.get(position);
         }

         @Override
         public long getItemId(int position) {
             return position;
         }

         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             ViewHolder holder = null;

             if (convertView == null) {
                 convertView = inflater.inflate(R.layout.item_list_app, null);
                 holder = new ViewHolder();
                 holder.tv_name=(TextView)convertView.findViewById(R.id.tv_name);
                 holder.receiptdatetv=(TextView)convertView.findViewById(R.id.receiptdate);
                 holder.pricetv=(TextView)convertView.findViewById(R.id.pricetv);
                 holder.currencyname=(TextView)convertView.findViewById(R.id.currencyname);
                 holder.receipttype=(TextView)convertView.findViewById(R.id.receipttype);
                 holder.desctv=(TextView)convertView.findViewById(R.id.desctv);
                 holder.btnUpload=(TextView)convertView.findViewById(R.id.uploadbtn);
                 holder.progressBar=(ProgressBar)convertView.findViewById(R.id.progress);
                 convertView.setTag(holder);
             }
             else {
                 holder = (ViewHolder) convertView.getTag();
             }
             holder.btnUpload.setVisibility(View.VISIBLE);
             cursor.moveToPosition(position);
             holder.tv_name.setText(cursor.getString(cursor.getColumnIndex("firstname"))+" "+
                     cursor.getString(cursor.getColumnIndex("middlename"))+" "+
                     cursor.getString(cursor.getColumnIndex("lastname")));
             holder.receipttype.setText( cursor.getString(cursor.getColumnIndex("category")));
             holder.receiptdatetv.setText(cursor.getString(cursor.getColumnIndex("date")));
             holder.pricetv.setText("$"+round(Double.valueOf(cursor.getString(cursor.getColumnIndex("price"))),2));
             holder.currencyname.setText("USD");
             holder.desctv.setText(cursor.getString(cursor.getColumnIndex("description")));
             final ViewHolder finalHolder = holder;
             holder.btnUpload.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if(isNetworkAvailableWithNoDialog(getActivity())){
                         currency=cursor.getString(cursor.getColumnIndex("currency"));
                         selectedPrice=  Double.valueOf(cursor.getString(cursor.getColumnIndex("price")));
                         category=cursor.getString(cursor.getColumnIndex("category"));
                         desc=cursor.getString(cursor.getColumnIndex("description"));
                         strDate=cursor.getString(cursor.getColumnIndex("date"));
                         path=cursor.getString(cursor.getColumnIndex("imagepath"));
                         sno=cursor.getInt(cursor.getColumnIndex("sno"));
                         finalHolder.progressBar.setVisibility(View.VISIBLE);
                         finalHolder.btnUpload.setVisibility(View.GONE);
                         finalHolder.progressBar.setProgress(10);
                         new Handler().postDelayed(new Runnable() {
                             @Override
                             public void run() {
                                 finalHolder.progressBar.setProgress(70);
                             }
                         },3000);
                         addReceipt();
                     }else{
                         final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.datepicker));
                         builder.setMessage("We are having trouble uploading receipt right now.Please make sure you are connected to internet and try again.");
                         builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialogInterface, int i) {
                                 dialogInterface.dismiss();
                             }
                         });
                         builder.show();
                     }


                 }
             });
             return convertView;
         }

         class ViewHolder {
             TextView tv_name,receiptdatetv,pricetv,currencyname,receipttype,desctv,btnUpload;
             ProgressBar progressBar;
         }
         public   double round(double value, int places) {
             if (places < 0) throw new IllegalArgumentException();
             long factor = (long) Math.pow(10, places);
             value = value * factor;
             long tmp = Math.round(value);
             return (double) tmp / factor;
         }
     }
 */
  /*  private void deleteReceipt(String id, final int position, final ReceiptDetails receiptDetails) {
        showProgress();
        String url ="http://betheltripreceipts.com/services/delete_receipt";
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("receipt_id",id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        hideProgress();
                        try {
                            if(response.getString("status").equalsIgnoreCase("success")){
                             Toast.makeText(getActivity(),response.getString("message"),Toast.LENGTH_SHORT).show();
                                receiptDetails.getReceipts().remove(position);
                                // mAdapter.notifyDataSetChanged();
                            }else{
                                Toast.makeText(getActivity(),response.getString("message"),Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //    Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                        parseApiResponse(response);

                    }
                },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                hideProgress();
                                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });

        // Add the request to the RequestQueue.
        jsonObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkSingleton.getInstance(getActivity()).addToRequestQueue(jsonObjRequest);
    }
*/
    int sno;
    String currency;
    private void addReceipt() {
        if (isNetworkAvailableWithNoDialog(getActivity())) {
            if(!isUploadAll){
                showProgress();
            }
            new HttpGetRequest().execute("https://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote?format=json");
        }
    }


    private class HttpGetRequest extends AsyncTask<String, Void, String> {
        private static final String REQUEST_METHOD = "GET";
        private static final int READ_TIMEOUT = 15000;
        private static final int CONNECTION_TIMEOUT = 15000;
        @Override
        protected String doInBackground(String... params){
            String stringUrl = params[0];
            String result;
            String inputLine;
            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection =(HttpURLConnection)
                        myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();
                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                result = stringBuilder.toString();
            }
            catch(IOException e){
                e.printStackTrace();
                result = null;
            }
            return result;
        }
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            try {
                JSONObject jObj = new JSONObject(result);
                JSONObject list = jObj.getJSONObject("list");
                JSONArray resources = list.getJSONArray("resources");

                for(int i=0;i<resources.length();i++)
                {
                    JSONObject inner = resources.getJSONObject(i);
                    JSONObject resource = inner.getJSONObject("resource");
                    JSONObject fields = resource.getJSONObject("fields");
                    String name = fields.getString("name");
                    String _price = fields.getString("price");

                    if (name.equalsIgnoreCase("USD/"+currency)) {
                        Double currencyPrice =
                                Double.valueOf(_price);
                        price = selectedPrice/ currencyPrice;

                        break;
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            submitReceipt();

        }
    }



}