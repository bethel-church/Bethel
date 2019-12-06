package com.bethel.ui;


import android.app.Activity;
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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
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
import com.bethel.interfaces.UpdateReceiptFilter;
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

import butterknife.ButterKnife;

public class ViewMembersTrips extends Activity implements UpdateReceiptFilter {

    private List<ApplicationInfo> mAppList;
    private AppAdapter mAdapter;
    private SwipeMenuListView mListView;
    private String memberId,firstName,middleName,lastName;
    private RelativeLayout headerView;
    private TextView headerLabel,mFilterTotal;

    static Context context;
    public static Context getInstance(){
        return context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.uploaded_receipts);
        ButterKnife.bind(this);
        memberId=getIntent().getExtras().getString("memberId");
        firstName=getIntent().getExtras().getString("firstname");
        middleName=getIntent().getExtras().getString("middlename");
        lastName=getIntent().getExtras().getString("lastname");
        prepareViews();
    }

    public void applyFilters(){
        // mListView.setVisibility(View.GONE);
        fetchReceipts();
    }


    private void prepareViews() {
        mListView = (SwipeMenuListView) findViewById(R.id.listView);
        headerView=(RelativeLayout)findViewById(R.id.headerview);
        headerView.setVisibility(View.VISIBLE);
        headerLabel=(TextView)findViewById(R.id.top_label);
        mFilterTotal=(TextView)findViewById(R.id.totalfiltertv);
        headerLabel.setText(getIntent().getExtras().getString("name"));
        ((TextView)findViewById(R.id.filterlabel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ViewMembersTrips.this,FilterReceiptsActivity.class);
                intent.putExtra("isTripMember",true);
                intent.putExtra("firstname",firstName);
                intent.putExtra("middlename",middleName);
                intent.putExtra("lastname",lastName);
                startActivityForResult(intent,100);
            }
        });
        // mListView.setVisibility(View.GONE);
        //((TextView)findViewById(R.id.totalspenttv)).setVisibility(View.VISIBLE);
        fetchReceipts();
        //AppDatabase appDatabase=new AppDatabase(ViewMembersTrips.this);
        //appDatabase.getRowCount(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,ApiConstants.TRIP_ID));
    }


double total;
    public void fetchReceipts() {
        showProgress();
        String url ="http://betheltripreceipts.com/services/get_all_receipts";
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("trip_id", SharedPreferencesHandler.getStringValues(this, ApiConstants.TRIP_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(CommonUtils.isNetworkAvailableWithNoDialog(ViewMembersTrips.this)) {
            JsonObjectRequest jsonObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //    Toast.makeText(ViewMembersTrips.this, response.toString(), Toast.LENGTH_SHORT).show();
                            parseApiResponse(response);

                        }
                    },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    hideProgress();
                                    Toast.makeText(ViewMembersTrips.this, error.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

            // Add the request to the RequestQueue.
            jsonObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetworkSingleton.getInstance(ViewMembersTrips.this).addToRequestQueue(jsonObjRequest);
        }else{
            // ((ViewReceiptsActivity)ViewMembersTrips.this).updateSavedTabsCount(cursor.getCount());
            new FetchReceiptsFromDb().execute();
        }
    }

    @Override
    public void applyReceipts() {
        fetchReceipts();
    }


    class FetchReceiptsFromDb extends AsyncTask<Void,Void,Void> {
        Cursor cursor;
        AppDatabase appDatabase;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            appDatabase=new AppDatabase(ViewMembersTrips.this);

        }

        @Override
        protected Void doInBackground(Void... voids) {
          /*  if(SharedPreferencesHandler.getSharedPreferences(ViewMembersTrips.this).contains("selectcat")){
                cursor=appDatabase.getUploadedRowCount(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,ApiConstants.TRIP_ID),
                        SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"selectcat"));
            }else{*/
                cursor=appDatabase.getUploadedRowCount(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,ApiConstants.TRIP_ID),"");
           // }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(cursor.getCount()<0){
                CommonUtils.toast("No Receipts Available");
            }else {
                parseApiResponse(cursor);
            }

        }
    }


    private void parseApiResponse(Cursor cursor) {
//       setListviewValues(cursor);
     //   total=0;
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
           // total=total+Double.valueOf(cursor.getString(cursor.getColumnIndex("price")));
            transactions.add(transaction);
            User user = new User();
            user.setFirst_name(cursor.getString(cursor.getColumnIndex("firstname")));
            user.setMiddle_name(cursor.getString(cursor.getColumnIndex("middlename")));
            user.setLast_name(cursor.getString(cursor.getColumnIndex("lastname")));
            users.add(user);
            ReceiptModel receiptModel = new ReceiptModel();
            receiptModel.setTransaction(transaction);
            receiptModel.setUser(user);

            if (SharedPreferencesHandler.getSharedPreferences(ViewMembersTrips.this).contains("selectcat")) {
              /*  if (SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, "selectcat").
                        equalsIgnoreCase("All") || SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, "selectcat").
                        equalsIgnoreCase("")) {*/
                    final List<String> items = new LinkedList<String>( Arrays.asList(SharedPreferencesHandler.getStringValues(this, "selectcat").split("\\s*,\\s*")));

                    for(int i=0;i<items.size();i++) {
                        if (transaction.getType().equalsIgnoreCase(items.get(i))||items.get(i).equalsIgnoreCase("All")) {
                            {

                    if (SharedPreferencesHandler.getSharedPreferences(ViewMembersTrips.this).contains("showmyreceipts")) {
                        if (SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this, "showmyreceipts")) {
                            if (firstName.equalsIgnoreCase(user.getFirst_name())) {

                                if(!SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this,"alldate")){
                                    String dateformat = "yyyy-MM-dd"; //In which you need put here
                                    SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                    Date date1 = new Date();
                                    Date date2 = new Date();
                                    Date dateToCompare = new Date();
                                    try {
                                        date1= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"startDate"));
                                        date2= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"endDate"));
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

                            if(!SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this,"alldate")){
                                String dateformat = "yyyy-MM-dd"; //In which you need put here
                                SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                Date date1 = new Date();
                                Date date2 = new Date();
                                Date dateToCompare = new Date();
                                try {
                                    date1= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"startDate"));
                                    date2= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"endDate"));
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
                        if(!SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this,"alldate")){
                            String dateformat = "yyyy-MM-dd"; //In which you need put here
                            SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                            Date date1 = new Date();
                            Date date2 = new Date();
                            Date dateToCompare = new Date();
                            try {
                                date1= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"startDate"));
                                date2= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"endDate"));
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
                /*}else {
                    if (transaction.getType().equalsIgnoreCase(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, "selectcat"))) {
                        if (SharedPreferencesHandler.getSharedPreferences(ViewMembersTrips.this).contains("showmyreceipts")) {
                            if (SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this, "showmyreceipts")) {
                                if (firstName.equalsIgnoreCase(user.getFirst_name()))
                                {
                                    if(!SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this,"alldate")){
                                        String dateformat = "yyyy-MM-dd"; //In which you need put here
                                        SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                        Date date1 = new Date();
                                        Date date2 = new Date();
                                        Date dateToCompare = new Date();
                                        try {
                                            date1= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"startDate"));
                                            date2= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"endDate"));
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
                                if(!SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this,"alldate")){
                                    String dateformat = "yyyy-MM-dd"; //In which you need put here
                                    SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                    Date date1 = new Date();
                                    Date date2 = new Date();
                                    Date dateToCompare = new Date();
                                    try {
                                        date1= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"startDate"));
                                        date2= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"endDate"));
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
                            if(!SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this,"alldate")){
                                String dateformat = "yyyy-MM-dd"; //In which you need put here
                                SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                                Date date1 = new Date();
                                Date date2 = new Date();
                                Date dateToCompare = new Date();
                                try {
                                    date1= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"startDate"));
                                    date2= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"endDate"));
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
            } }}}else {
                if (SharedPreferencesHandler.getSharedPreferences(ViewMembersTrips.this).contains("showmyreceipts")) {
                    if (SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this, "showmyreceipts")) {
                        if (firstName.equalsIgnoreCase(user.getFirst_name())) {
                            if (!SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this, "alldate")) {
                                String dateformat = "yyyy-MM-dd"; //In which you need put here
                                SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
                                if(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, "startDate")!=null){
                                    Date date1 = new Date();
                                    Date date2 = new Date();
                                    Date dateToCompare = new Date();
                                    try {
                                        date1 = sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, "startDate"));
                                        date2 = sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, "endDate"));
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

                            } else {
                                receiptModels.add(receiptModel);
                            }

                        }
                    }else{
                        if(!SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this,"alldate")){
                            String dateformat = "yyyy-MM-dd"; //In which you need put here
                            SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                            Date date1 = new Date();
                            Date date2 = new Date();
                            Date dateToCompare = new Date();
                            try {
                                date1= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"startDate"));
                                date2= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"endDate"));
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
                    if(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"startDate")!=null){
                        if(!SharedPreferencesHandler.getBooleanValues(ViewMembersTrips.this,"alldate")){
                            String dateformat = "yyyy-MM-dd"; //In which you need put here
                            SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

                            Date date1 = new Date();
                            Date date2 = new Date();
                            Date dateToCompare = new Date();
                            try {
                                date1= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"startDate"));
                                date2= sdf.parse(SharedPreferencesHandler.getStringValues(ViewMembersTrips.this,"endDate"));
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
       // DecimalFormat f = new DecimalFormat("##.00");
      //  ((TextView)findViewById(R.id.totalspenttv)).setText("$ "+f.format(total)+"\n Total Spent");
       // ((TextView)findViewById(R.id.totalspenttv)).setVisibility(View.VISIBLE);
        receiptDetailsList.setReceipts(receiptModels);
        setListviewValues(receiptDetailsList);

        hideProgress();

    }

    private void setListviewValues(final Cursor cursor) {
        mAppList = ViewMembersTrips.this.getPackageManager().getInstalledApplications(0);

        CursorAdapter mAdapter = new CursorAdapter(cursor);
        mListView.setAdapter(mAdapter);
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                //   menu.setViewType(R.layout.view_receipts);
                // create "open" item
                SwipeMenuItem receiptitem = new SwipeMenuItem(
                        ViewMembersTrips.this);

                // set item background
                receiptitem.setBackground(new ColorDrawable(Color.parseColor("#AFAFAF")));
                // set item width
                receiptitem.setWidth(dp2px(90));
                receiptitem.setIcon(R.drawable.receipt);
                // add to menu
                menu.addMenuItem(receiptitem);

                SwipeMenuItem edititem = new SwipeMenuItem(
                        ViewMembersTrips.this);

                // set item background
                edititem.setBackground(new ColorDrawable(Color.parseColor("#4498A1")));
                // set item width
                edititem.setWidth(dp2px(90));
                edititem.setIcon(R.drawable.edit);
                // add to menu
                menu.addMenuItem(edititem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        ViewMembersTrips.this);
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
                        if(CommonUtils.isNetworkAvailableWithNoDialog(ViewMembersTrips.this)){
                            Intent in1 = new Intent(ViewMembersTrips.this, PhotoViewerActivity.class);
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
                        if(CommonUtils.isNetworkAvailableWithNoDialog(ViewMembersTrips.this)) {

                            Intent editIntent = new Intent(ViewMembersTrips.this, AddReceiptActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isEdit", true);
                            bundle.putBoolean("isUploaded", true);
                            bundle.putBoolean("isCursor", false);
                            bundle.putBoolean("isTripMember",true);
                            bundle.putString("firstname",receiptDetails.getReceipts().get(position).getUser().getFirst_name());
                            bundle.putString("middlename",receiptDetails.getReceipts().get(position).getUser().getMiddle_name());
                            bundle.putString("lastname",receiptDetails.getReceipts().get(position).getUser().getLast_name());
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
                            startActivityForResult(editIntent,100);
                          //  finish();
                        }else{
                            CommonUtils.toast("No Internet Available.");
                        }
                        break;
                    case 2:
                        // delete
                        new android.app.AlertDialog.Builder(ViewMembersTrips.this,R.style.datepicker)
                                .setMessage("Are you sure you want to delete this receipt.")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(CommonUtils.isNetworkAvailableWithNoDialog(ViewMembersTrips.this)) {
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




double filterTotal=0.0;
    class CursorAdapter extends BaseAdapter {
        private   LayoutInflater inflater=null;
        Cursor cursor;
        DecimalFormat f;
        public CursorAdapter(Cursor cursor){
            inflater = ( LayoutInflater )ViewMembersTrips.this.
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
            total=0;
            if(response!=null) {
                receiptDetails = new ReceiptDetails();
                receiptDetails.setStatus(response.getString("status"));
                JSONArray jsonArray = response.getJSONArray("receipts");
                //((ViewReceiptsActivity) ViewMembersTrips.this).updateTabsCount(jsonArray.length());
                ArrayList<Transaction> transactions = new ArrayList<>();
                ArrayList<User> users = new ArrayList<>();
                ArrayList<ReceiptModel> receiptModels = new ArrayList<>();
                final AppDatabase appDatabase = new AppDatabase(ViewMembersTrips.this);
                appDatabase.clearUploadedRowValues();
                //progressDialog = new ProgressDialog(ViewMembersTrips.this);
                //progressDialog.setMessage("Please wait...");
                //progressDialog.setCancelable(false);
                // progressDialog.show();
                count = jsonArray.length();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject jsonTransactionObj = jsonObject.getJSONObject("Transaction");
                    if(jsonTransactionObj.getString("user_id").equalsIgnoreCase(memberId)) {
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
                       total=total+Double.valueOf(jsonTransactionObj.getString("usd"));
                        JSONObject jsonuserObj = jsonObject.getJSONObject("User");
                        final User user = new User();
                        user.setFirst_name(jsonuserObj.getString("first_name"));
                        user.setMiddle_name(jsonuserObj.getString("middle_name"));
                        user.setLast_name(jsonuserObj.getString("last_name"));
                        users.add(user);
                        ReceiptModel receiptModel = new ReceiptModel();
                        receiptModel.setTransaction(transaction);
                        receiptModel.setUser(user);
                        receiptModels.add(receiptModel);
                        appDatabase.insertDataInStringDatabase(transaction.getReceipt_date(),
                                SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, ApiConstants.TRIP_ID),
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
                DecimalFormat f = new DecimalFormat("##.00");
                ((TextView)findViewById(R.id.totalspenttv)).setText("$ "+f.format(total)+"\n Total Spent");
                ((TextView)findViewById(R.id.totalspenttv)).setVisibility(View.VISIBLE);
                if(total==0){
                    ((TextView)findViewById(R.id.totalspenttv)).setVisibility(View.GONE);
                }
                new FetchReceiptsFromDb().execute();
                // setListviewValues(receiptDetails);
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
                        SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, ApiConstants.TRIP_ID),
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
                //    ((ViewReceiptsActivity)ViewMembersTrips.this).isClickAvailable=true;
                //  ((ViewReceiptsActivity)ViewMembersTrips.this).setTabChangeListener();
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
        mAppList = ViewMembersTrips.this.getPackageManager().getInstalledApplications(0);
        if(SharedPreferencesHandler.getSharedPreferences(ViewMembersTrips.this).getBoolean("isFilterSet",false)){
            for(int i=0;i<receiptDetails.getReceipts().size();i++){
                filterTotal=filterTotal+Double.valueOf(receiptDetails.getReceipts().get(i).getTransaction().getUsd());
            }
            DecimalFormat f = new DecimalFormat("##.00");
            mFilterTotal.setText("$ "+f.format(filterTotal)+"\n Filter Total");
            mFilterTotal.setVisibility(View.VISIBLE);
            if(filterTotal==0 || filterTotal==total)
            {
                mFilterTotal.setVisibility(View.GONE);
            }
        }

//        mListView.setVisibility(View.GONE);
        mAdapter = new AppAdapter(receiptDetails);
      filterTotal=0.0;
        mListView.setAdapter(mAdapter);
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                //   menu.setViewType(R.layout.view_receipts);
                // create "open" item
                SwipeMenuItem receiptitem = new SwipeMenuItem(
                        ViewMembersTrips.this);

                // set item background
                receiptitem.setBackground(new ColorDrawable(Color.parseColor("#AFAFAF")));
                // set item width
                receiptitem.setWidth(dp2px(90));

                receiptitem.setIcon(R.drawable.receipt);
                // add to menu
                menu.addMenuItem(receiptitem);

                SwipeMenuItem edititem = new SwipeMenuItem(
                        ViewMembersTrips.this);

                // set item background
                edititem.setBackground(new ColorDrawable(Color.parseColor("#4498A1")));
                // set item width
                edititem.setWidth(dp2px(90));
                edititem.setIcon(R.drawable.edit);
                // add to menu
                menu.addMenuItem(edititem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        ViewMembersTrips.this);
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

                        Intent in1 = new Intent(ViewMembersTrips.this, PhotoViewerActivity.class);
                        in1.putExtra("isImage",receiptDetails.getReceipts().get(position).getTransaction().getReceipt());
                        //  in1.putExtra("image",byteArray);
                        startActivity(in1);
                        break;
                    case 1:
                        // Edit
                        // open(item);
                        if(CommonUtils.isNetworkAvailableWithNoDialog(ViewMembersTrips.this)) {
                            Intent editIntent = new Intent(ViewMembersTrips.this, AddReceiptActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isEdit", true);
                            bundle.putBoolean("isCursor", false);
                            bundle.putBoolean("isUploaded", true);
                            bundle.putBoolean("isTripMember",true);
                            bundle.putString("firstname",receiptDetails.getReceipts().get(position).getUser().getFirst_name());
                            bundle.putString("middlename",receiptDetails.getReceipts().get(position).getUser().getMiddle_name());
                            bundle.putString("lastname",receiptDetails.getReceipts().get(position).getUser().getLast_name());
                            bundle.putSerializable("receiptmodel", (Serializable) receiptDetails.getReceipts().get(position));
                            bundle.putBoolean("isFromReceipt", true);
                            editIntent.putExtras(bundle);

                            startActivityForResult(editIntent,100);
                         //   finish();
                        }else{
                            Toast.makeText(ViewMembersTrips.this,"No Internet Connection",Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case 2:
                        // delete
                        new android.app.AlertDialog.Builder(ViewMembersTrips.this,R.style.datepicker)
                                .setMessage("Are you sure you want to delete this receipt.")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(CommonUtils.isNetworkAvailableWithNoDialog(ViewMembersTrips.this)) {
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
            progress = new ProgressBarHandler(ViewMembersTrips.this);

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
            inflater = ( LayoutInflater )ViewMembersTrips.this.
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
            String name=receiptDetails.getReceipts().get(position).
                    getUser().getFirst_name() + " " + receiptDetails.getReceipts().get(position).
                    getUser().getMiddle_name() + " " + receiptDetails.getReceipts().get(position).
                    getUser().getLast_name();
            String storedName= SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, "First_Name")+" "+
            SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, "Middle_Name")+" "+
                    SharedPreferencesHandler.getStringValues(ViewMembersTrips.this, "Last_Name");
           // if(storedName.equalsIgnoreCase(name)){

             //   holder.tv_name.setText("Me");
            //}else{

                holder.tv_name.setText(name);
            //}
            holder.receipttype.setText(receiptDetails.getReceipts().get(position).
                    getTransaction().getType());
/*
            holder.receiptdatetv.setText(receiptDetails.getReceipts().get(position).
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
                                Toast.makeText(ViewMembersTrips.this,response.getString("message"),Toast.LENGTH_SHORT).show();
                               /* receiptDetails.getReceipts().remove(position);
                                ((ViewReceiptsActivity)ViewMembersTrips.this).updateTabsCount(receiptDetails.getReceipts().size());
                                mAdapter.notifyDataSetChanged();*/
                                //  AppDatabase appDatabase=new AppDatabase(ViewMembersTrips.this);
                                //appDatabase.deleteUploadedRowValues(cursor.getString(cursor.getColumnIndex("sno")));
                                fetchReceipts();
                            }else{
                                Toast.makeText(ViewMembersTrips.this,response.getString("message"),Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //    Toast.makeText(ViewMembersTrips.this, response.toString(), Toast.LENGTH_SHORT).show();
                        // parseApiResponse(response);

                    }
                },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                hideProgress();
                                Toast.makeText(ViewMembersTrips.this, error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });

        // Add the request to the RequestQueue.
        jsonObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkSingleton.getInstance(ViewMembersTrips.this).addToRequestQueue(jsonObjRequest);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==100){
            fetchReceipts();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
