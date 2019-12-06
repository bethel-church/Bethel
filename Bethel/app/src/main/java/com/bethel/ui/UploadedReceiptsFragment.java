package com.bethel.ui;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bethel.R;
import com.bethel.api.NetworkSingleton;
import com.bethel.constants.ApiConstants;
import com.bethel.database.AppDatabase;
import com.bethel.model.ReceiptDetails;
import com.bethel.model.ReceiptModel;
import com.bethel.model.Transaction;
import com.bethel.model.User;
import com.bethel.swipelist.SwipeMenu;
import com.bethel.swipelist.SwipeMenuCreator;
import com.bethel.swipelist.SwipeMenuItem;
import com.bethel.swipelist.SwipeMenuListView;
import com.bethel.utils.CommonUtils;
import com.bethel.utils.ProgressBarHandler;
import com.bethel.utils.SharedPreferencesHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UploadedReceiptsFragment extends Fragment{

    private List<ApplicationInfo> mAppList;
    private AppAdapter mAdapter;
    @BindView(R.id.listView) SwipeMenuListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uploaded_receipts, container, false);
        ButterKnife.bind(this, view);

        return view;
    }


    public void applyFilters(){
        // mListView.setVisibility(View.GONE);
        fetchReceipts();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareViews(getView());
    }

    private void prepareViews(View view) {

        // mListView.setVisibility(View.GONE);
        fetchReceipts();
        //AppDatabase appDatabase=new AppDatabase(getActivity());
        //appDatabase.getRowCount(SharedPreferencesHandler.getStringValues(getActivity(),ApiConstants.TRIP_ID));
    }



    public void fetchReceipts() {
        showProgress();
        String url ="http://betheltripreceipts.com/services/get_all_receipts";
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("trip_id", SharedPreferencesHandler.getStringValues(getActivity(),ApiConstants.TRIP_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(CommonUtils.isNetworkAvailableWithNoDialog(getActivity())) {
            JsonObjectRequest jsonObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //    Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                            parseApiResponse(response);

                        }
                    },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
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
        }else{
            // ((ViewReceiptsActivity)getActivity()).updateSavedTabsCount(cursor.getCount());
            new FetchReceiptsFromDb().execute();
        }
    }


    class FetchReceiptsFromDb extends AsyncTask<Void,Void,Void>{
        Cursor cursor;
        AppDatabase appDatabase;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            appDatabase=new AppDatabase(getActivity());

        }

        @Override
        protected Void doInBackground(Void... voids) {
           /* if(SharedPreferencesHandler.getSharedPreferences(getActivity()).contains("selectcat")){
                cursor=appDatabase.getUploadedRowCount(SharedPreferencesHandler.getStringValues(getActivity(),ApiConstants.TRIP_ID),
                        SharedPreferencesHandler.getStringValues(getActivity(),"selectcat"));
            }else{*/
                cursor=appDatabase.getUploadedRowCount(SharedPreferencesHandler.getStringValues(getActivity(),ApiConstants.TRIP_ID),"");
           // }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((ViewReceiptsActivity)getActivity()).updateTabsCount(cursor.getCount());
            if(cursor.getCount()<0){
                CommonUtils.toast("No Receipts Available");
            }else {
                parseApiResponse(cursor);
            }

        }
    }


    private void parseApiResponse(Cursor cursor) {
//       setListviewValues(cursor);

        ReceiptDetails receiptDetailsList = new ReceiptDetails();
        ArrayList<Transaction> transactions = new ArrayList<>();
        ArrayList<User> users = new ArrayList<>();
        ArrayList<ReceiptModel> receiptModels = new ArrayList<>();
        while (cursor.moveToNext()) {


            Transaction transaction = new Transaction();

            transaction.setId(cursor.getString(cursor.getColumnIndex("id")));
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
                final List<String> items = new LinkedList<String>( Arrays.asList(SharedPreferencesHandler.getStringValues(getActivity(), "selectcat").split("\\s*,\\s*")));

                for(int i=0;i<items.size();i++) {
                    if (transaction.getType().equalsIgnoreCase(items.get(i))||items.get(i).equalsIgnoreCase("All")) {
                        {

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
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                receiptModels.add(receiptModel);
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
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            receiptModels.add(receiptModel);
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
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        receiptModels.add(receiptModel);
                                    }
                                }else{
                                    receiptModels.add(receiptModel);
                                }
                            }
                        }
                    }
                }

/*                if (SharedPreferencesHandler.getStringValues(getActivity(), "selectcat").
                        equalsIgnoreCase("All") || SharedPreferencesHandler.getStringValues(getActivity(), "selectcat").
                        equalsIgnoreCase("")) {

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
                }else {
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
            } else {
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
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        receiptModels.add(receiptModel);
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
        ( (ViewReceiptsActivity)getActivity()).updateTabsCount(receiptDetailsList.getReceipts().size());
        setListviewValues(receiptDetailsList);

        hideProgress();

    }

    private void setListviewValues(final Cursor cursor) {
        mAppList = getActivity().getPackageManager().getInstalledApplications(0);

        CursorAdapter mAdapter = new CursorAdapter(cursor);
        mListView.setAdapter(mAdapter);
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
                switch (index) {
                    case 0:
                        // open
                        // open(item);
                        if(CommonUtils.isNetworkAvailableWithNoDialog(getActivity())){
                            Intent in1 = new Intent(getActivity(), PhotoViewerActivity.class);
                            cursor.moveToPosition(position);
                            in1.putExtra("isImage",cursor.getString(cursor.getColumnIndex("imagepath")));
                            //  in1.putExtra("image",byteArray);
                            startActivity(in1);
                        }else{
                            CommonUtils.toast("No Internet Available.");
                        }

                        break;
                    case 1:
                        // Edit
                        // open(item);
                        if(CommonUtils.isNetworkAvailableWithNoDialog(getActivity())) {

                            Intent editIntent = new Intent(getActivity(), AddReceiptActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isEdit", true);
                            bundle.putBoolean("isUploaded", true);
                            bundle.putBoolean("isCursor", false);

                            cursor.moveToPosition(position);
                            bundle.putSerializable("receiptmodel", (Serializable) receiptDetails.getReceipts().get(position));
                            bundle.putString("desc", receiptDetails.getReceipts().get(position).getTransaction().getDescription());
                            bundle.putString("price", receiptDetails.getReceipts().get(position).getTransaction().getUsd());
                            bundle.putString("currency", receiptDetails.getReceipts().get(position).getTransaction().getForeign_currency());
                            bundle.putString("category", receiptDetails.getReceipts().get(position).getTransaction().getType());
                            bundle.putString("date", receiptDetails.getReceipts().get(position).getTransaction().getReceipt_date());
                            bundle.putString("imagepath", receiptDetails.getReceipts().get(position).getTransaction().getReceipt());
                            bundle.putInt("sno", cursor.getInt(cursor.getColumnIndex("sno")));
                            bundle.putBoolean("isFromReceipt", true);
                            editIntent.putExtras(bundle);
                            startActivity(editIntent);
                            ((ViewReceiptsActivity) getActivity()).finish();
                        }else{
                            CommonUtils.toast("No Internet Available.");
                        }
                        break;
                    case 2:
                        // delete
                        new android.app.AlertDialog.Builder(getActivity(),R.style.datepicker)
                                .setMessage("Are you sure you want to delete this receipt.")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(CommonUtils.isNetworkAvailableWithNoDialog(getActivity())) {
                                          //  cursor.moveToPosition(position);
                                            deleteReceipt(receiptDetails.getReceipts().get(position).getTransaction().getId(),position,receiptDetails);

                                        }else{
                                            CommonUtils.toast("No Internet Available.");
                                        }

                                    }
                                })
                                .setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();



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





    class CursorAdapter extends BaseAdapter {
        private   LayoutInflater inflater=null;
        Cursor cursor;
        DecimalFormat f;
        public CursorAdapter(Cursor cursor){
            inflater = ( LayoutInflater )getActivity().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.cursor=cursor;
            f = new DecimalFormat("##.00");
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
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            cursor.moveToPosition(position);
            holder.tv_name.setText(cursor.getString(cursor.getColumnIndex("firstname"))+" "+
                    cursor.getString(cursor.getColumnIndex("middlename"))+" "+
                    cursor.getString(cursor.getColumnIndex("lastname")));
            holder.receipttype.setText( cursor.getString(cursor.getColumnIndex("category")));

            holder.receiptdatetv.setText(parseTodaysDate(cursor.getString(cursor.getColumnIndex("date"))));


            holder.pricetv.setText("$"+f.format(Double.valueOf(cursor.getString(cursor.getColumnIndex("price")))));
            holder.currencyname.setText(cursor.getString(cursor.getColumnIndex("currency")));
            holder.desctv.setText(cursor.getString(cursor.getColumnIndex("description")));
            return convertView;
        }
        public  String parseTodaysDate(String time) {



            String inputPattern = "yyyy-MM-dd hh:mm a";

            String outputPattern = "dd-MM-yyyy hh:mm a";

            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

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
            TextView tv_name,receiptdatetv,pricetv,currencyname,receipttype,desctv;

        }
        public   double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();
            long factor = (long) Math.pow(10, places);
            value = value * factor;
            long tmp = Math.round(value);
            return (double) tmp / factor;
        }
    }
    ReceiptDetails receiptDetails;

    private void parseApiResponse(JSONObject response) {
        try{
            if(response!=null) {
                receiptDetails = new ReceiptDetails();
                receiptDetails.setStatus(response.getString("status"));
                JSONArray jsonArray = response.getJSONArray("receipts");
                //((ViewReceiptsActivity) getActivity()).updateTabsCount(jsonArray.length());
                ArrayList<Transaction> transactions = new ArrayList<>();
                ArrayList<User> users = new ArrayList<>();
                ArrayList<ReceiptModel> receiptModels = new ArrayList<>();
                final AppDatabase appDatabase = new AppDatabase(getActivity());
                appDatabase.clearUploadedRowValues();
                //progressDialog = new ProgressDialog(getActivity());
                //progressDialog.setMessage("Please wait...");
                //progressDialog.setCancelable(false);
                // progressDialog.show();
                count = jsonArray.length();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject jsonTransactionObj = jsonObject.getJSONObject("Transaction");
                    final Transaction transaction = new Transaction();
                    transaction.setId(jsonTransactionObj.getString("id"));
                    transaction.setCreated(jsonTransactionObj.getString("created"));
                    transaction.setDescription(jsonTransactionObj.getString("description"));
                    transaction.setForeign_currency(jsonTransactionObj.getString("foreign_currency"));
                    transaction.setForeign_currency_amount(jsonTransactionObj.getString("foreign_currency_amount"));
                    transaction.setIs_edited(jsonTransactionObj.getString("is_edited"));
                    transaction.setReceipt(jsonTransactionObj.getString("receipt"));
                    transaction.setReceipt_date(jsonTransactionObj.getString("receipt_date"));
                    transaction.setTrip_id(jsonTransactionObj.getString("trip_id"));
                    transaction.setType(jsonTransactionObj.getString("type"));
                    transaction.setUsd(jsonTransactionObj.getString("usd"));
                    transaction.setUser_id(jsonTransactionObj.getString("user_id"));
                    transactions.add(transaction);
                    JSONObject jsonuserObj = jsonObject.getJSONObject("User");
                    final User user = new User();
                    user.setFirst_name(jsonuserObj.getString("first_name"));
                    user.setMiddle_name(jsonuserObj.getString("middle_name"));
                    user.setLast_name(jsonuserObj.getString("last_name"));
                    users.add(user);
                    ReceiptModel receiptModel = new ReceiptModel();
                    receiptModel.setTransaction(transaction);
                    receiptModel.setUser(user);
                 /*   appDatabase.insertDataInStringDatabase(transaction.getReceipt_date(),
                            SharedPreferencesHandler.getStringValues(getActivity(), ApiConstants.TRIP_ID),
                            transaction.getForeign_currency() + "",
                            transaction.getType(),
                            transaction.getDescription(),
                            transaction.getUsd(),
                            transaction.getReceipt(),
                            user.getFirst_name(),
                            user.getMiddle_name(),
                            user.getLast_name());*/
                 /*   final
                    transaction.setReceipt(path);*/
                    /*Glide.with(getActivity())
                            .load(transaction.getReceipt()).asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Receipts/" +
                                            System.currentTimeMillis() + ".jpg";
                                    //  transaction.setReceipt(path);
                                   // new SaveImageTask(resource, path, appDatabase, transaction, user).execute();
                                }
                            });*/


                  /*  if (SharedPreferencesHandler.getSharedPreferences(getActivity()).contains("selectcat")) {
                        if (SharedPreferencesHandler.getStringValues(getActivity(), "selectcat").
                                equalsIgnoreCase("All") || SharedPreferencesHandler.getStringValues(getActivity(), "selectcat").
                                equalsIgnoreCase("")) {
                            receiptModels.add(receiptModel);
                         *//*   if (SharedPreferencesHandler.getSharedPreferences(getActivity()).contains("showmyreceipts")) {
                                if (SharedPreferencesHandler.getBooleanValues(getActivity(), "showmyreceipts")) {
                                    if (SharedPreferencesHandler.getStringValues(getActivity(), "first_name").equalsIgnoreCase(user.getFirst_name())) {
                                        receiptModels.add(receiptModel);
                                    }
                                }else{
                                    receiptModels.add(receiptModel);
                                }
                            } else{
                                receiptModels.add(receiptModel);
                            }*//*
                        } else {
                            if (jsonTransactionObj.getString("type").equalsIgnoreCase(SharedPreferencesHandler.getStringValues(getActivity(), "selectcat"))) {
                           *//*     if (SharedPreferencesHandler.getSharedPreferences(getActivity()).contains("showmyreceipts")) {
                                    if (SharedPreferencesHandler.getBooleanValues(getActivity(), "showmyreceipts")) {
                                        if (SharedPreferencesHandler.getStringValues(getActivity(), "first_name").equalsIgnoreCase(user.getFirst_name())) {
                                            receiptModels.add(receiptModel);
                                        }
                                    }else{
                                        receiptModels.add(receiptModel);
                                    }
                                } else{
                                    receiptModels.add(receiptModel);
                                }
*//*receiptModels.add(receiptModel);
                            }
                        }
                    }else {
                       *//* if (SharedPreferencesHandler.getSharedPreferences(getActivity()).contains("showmyreceipts")) {
                            if (SharedPreferencesHandler.getBooleanValues(getActivity(), "showmyreceipts")) {
                                if (SharedPreferencesHandler.getStringValues(getActivity(), "first_name").equalsIgnoreCase(user.getFirst_name())) {
                                    receiptModels.add(receiptModel);
                                }
                            }else{
                                receiptModels.add(receiptModel);
                            }
                        } else{
                            receiptModels.add(receiptModel);
                        }*//*
                        receiptModels.add(receiptModel);

                    }*/
                    if (SharedPreferencesHandler.getStringValues(getActivity(), "USERTYPE").equalsIgnoreCase(ApiConstants.USERTYPE_STUDENT)) {
                        String name = user.getFirst_name() + " " + user.getMiddle_name() + " " + user.getLast_name();
                        String savedName = SharedPreferencesHandler.getStringValues(getActivity(), "first_name") + " " +
                                SharedPreferencesHandler.getStringValues(getActivity(), "middle_name") + " " +
                                SharedPreferencesHandler.getStringValues(getActivity(), "last_name");
                        if (name.equalsIgnoreCase(savedName)) {
                            receiptModels.add(receiptModel);
                            appDatabase.insertDataInStringDatabase(transaction.getReceipt_date(),
                                    SharedPreferencesHandler.getStringValues(getActivity(), ApiConstants.TRIP_ID),
                                    transaction.getForeign_currency() + "",
                                    transaction.getType(),
                                    transaction.getDescription(),
                                    transaction.getUsd(),
                                    transaction.getReceipt(),
                                    user.getFirst_name(),
                                    user.getMiddle_name(),
                                    user.getLast_name(), transaction.getId());
                            receiptDetails.setReceipts(receiptModels);
                            Log.e("receipts", "");
                        }
                    } else {
                        receiptModels.add(receiptModel);
                        appDatabase.insertDataInStringDatabase(transaction.getReceipt_date(),
                                SharedPreferencesHandler.getStringValues(getActivity(), ApiConstants.TRIP_ID),
                                transaction.getForeign_currency() + "",
                                transaction.getType(),
                                transaction.getDescription(),
                                transaction.getUsd(),
                                transaction.getReceipt(),
                                user.getFirst_name(),
                                user.getMiddle_name(),
                                user.getLast_name(), transaction.getId());
                        receiptDetails.setReceipts(receiptModels);
                        Log.e("receipts", "");

                    }
                }

                try {
                    if (receiptDetails.getReceipts().size() > 0) {
                        ((ViewReceiptsActivity) getActivity()).updateTabsCount(receiptDetails.getReceipts().size());
                        new FetchReceiptsFromDb().execute();
                    } else {
                        ((ViewReceiptsActivity) getActivity()).updateTabsCount(0);
                        CommonUtils.toast("No Receipts Available");
                    }
                    // setListviewValues(receiptDetails);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    ((ViewReceiptsActivity) getActivity()).updateTabsCount(0);
                    CommonUtils.toast("No Receipts Available");
                }
                hideProgress();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }


    ProgressDialog progressDialog;
    int count,downloadedCount;
    class SaveImageTask extends AsyncTask<Byte, Void, String> {
        Bitmap bitmap;
        String path;
        AppDatabase appDatabase;
        Transaction transaction;
        User user;
        public SaveImageTask(Bitmap bitmap,String path,AppDatabase appDatabase,Transaction transaction,
                             User user) {
            this.bitmap = bitmap;
            this.path=path;
            this.appDatabase=appDatabase;
            this.user =user;
            this.transaction=transaction;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // showProgress();
            /*if(!progressDialog.isShowing()){
                progressDialog.show();

            }*/
            //   showProgress();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                appDatabase.insertDataInStringDatabase(transaction.getReceipt_date(),
                        SharedPreferencesHandler.getStringValues(getActivity(), ApiConstants.TRIP_ID),
                        transaction.getForeign_currency() + "",
                        transaction.getType(),
                        transaction.getDescription(),
                        transaction.getUsd(),
                        s,
                        user.getFirst_name(),
                        user.getMiddle_name(),
                        user.getLast_name(),transaction.getId());
                //  downloadedCount++;
                // if(count==downloadedCount){
                //      hideProgress();
                //    mListView.setVisibility(View.VISIBLE);
                //    ((ViewReceiptsActivity)getActivity()).isClickAvailable=true;
                //  ((ViewReceiptsActivity)getActivity()).setTabChangeListener();
                //}
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Byte... params) {

            File root = new File(Environment.getExternalStorageDirectory(), "Receipts");
            if (!root.exists()) {
                root.mkdirs();
            }

            File pictureFile = new File(path);
            if (pictureFile == null) {
                return null;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();
                return path;
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
        }


    }


    private void setListviewValues(final ReceiptDetails receiptDetails) {
        mAppList = getActivity().getPackageManager().getInstalledApplications(0);


//        mListView.setVisibility(View.GONE);
        mAdapter = new AppAdapter(receiptDetails);

        mListView.setAdapter(mAdapter);
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
                        in1.putExtra("isImage",receiptDetails.getReceipts().get(position).getTransaction().getReceipt());
                        //  in1.putExtra("image",byteArray);
                        startActivity(in1);
                        break;
                    case 1:
                        // Edit
                        // open(item);
                        if(CommonUtils.isNetworkAvailableWithNoDialog(getActivity())) {

                            Intent editIntent = new Intent(getActivity(), AddReceiptActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isEdit", true);
                            bundle.putBoolean("isCursor", false);
                            bundle.putBoolean("isUploaded", true);
                            bundle.putSerializable("receiptmodel", (Serializable) receiptDetails.getReceipts().get(position));
                            bundle.putBoolean("isFromReceipt", true);
                            editIntent.putExtras(bundle);

                            startActivity(editIntent);
                            ((ViewReceiptsActivity) getActivity()).finish();
                        }else{
                            Toast.makeText(getActivity(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case 2:
                        // delete
                        new android.app.AlertDialog.Builder(getActivity(),R.style.datepicker)
                                .setMessage("Are you sure you want to delete this receipt.")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(CommonUtils.isNetworkAvailableWithNoDialog(getActivity())) {
                                           // cursor.moveToPosition(position);
                                            deleteReceipt(receiptDetails.getReceipts().get(position).getTransaction().getId(),position,receiptDetails);

                                        }else{
                                            CommonUtils.toast("No Internet Available.");
                                        }

                                    }
                                })
                                .setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();


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
        progress.hide();
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    class AppAdapter extends BaseAdapter {
        private   LayoutInflater inflater=null;
        ReceiptDetails receiptDetails;
        DecimalFormat f ;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_list_app, null);
                holder = new ViewHolder();
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                holder.receiptdatetv = (TextView) convertView.findViewById(R.id.receiptdate);
                holder.pricetv = (TextView) convertView.findViewById(R.id.pricetv);
                holder.currencyname = (TextView) convertView.findViewById(R.id.currencyname);
                holder.receipttype = (TextView) convertView.findViewById(R.id.receipttype);
                holder.desctv = (TextView) convertView.findViewById(R.id.desctv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            /*holder.tv_name.setText(receiptDetails.getReceipts().get(position).
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
            holder.receiptdatetv.setText(parseTodaysDate(receiptDetails.getReceipts().get(position).
                    getTransaction().getReceipt_date()));

/*
            holder.pricetv.setText("$" + f.format(Double.valueOf(receiptDetails.getReceipts().get(position).
                    getTransaction().getUsd())));*/

            holder.pricetv.setText("$" + f.format(Double.valueOf(receiptDetails.getReceipts().get(position).
                    getTransaction().getUsd())));
//            holder.currencyname.setText(receiptDetails.getReceipts().get(position).getTransaction().getForeign_currency());
            holder.currencyname.setText("USD");

            holder.desctv.setText(receiptDetails.getReceipts().get(position).
                    getTransaction().getDescription());




            return convertView;
        }

        class ViewHolder {
            TextView tv_name,receiptdatetv,pricetv,currencyname,receipttype,desctv;


        }
        public   double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();
            long factor = (long) Math.pow(10, places);
            value = value * factor;
            long tmp = Math.round(value);
            return (double) tmp / factor;
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
    }

    private void deleteReceipt(String id, final int position, final ReceiptDetails receiptDetails) {
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
                               /* receiptDetails.getReceipts().remove(position);
                                ((ViewReceiptsActivity)getActivity()).updateTabsCount(receiptDetails.getReceipts().size());
                                mAdapter.notifyDataSetChanged();*/
                              //  AppDatabase appDatabase=new AppDatabase(getActivity());
                                //appDatabase.deleteUploadedRowValues(cursor.getString(cursor.getColumnIndex("sno")));
                                fetchReceipts();
                                ((ViewReceiptsActivity)getActivity()).updateTabsCount(
                                        receiptDetails.getReceipts().size()
                                );
                            }else{
                                Toast.makeText(getActivity(),response.getString("message"),Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //    Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                        // parseApiResponse(response);

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

}