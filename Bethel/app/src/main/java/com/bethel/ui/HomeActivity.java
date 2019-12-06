package com.bethel.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bethel.R;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.api.NetworkSingleton;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.database.AppDatabase;
import com.bethel.model.CurrencyModel;
import com.bethel.model.CurrencyModel1;
import com.bethel.model.Example;
import com.bethel.model.ReceiptDetails;
import com.bethel.model.UserModel;
import com.bethel.model.studentbudget.StudentBudgetDetails;
import com.bethel.utils.CommonUtils;
import com.bethel.utils.SharedPreferencesHandler;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.bethel.utils.CommonUtils.isNetworkAvailableWithNoDialog;
import static com.bethel.utils.CommonUtils.toast;

public class HomeActivity extends BaseActivity {
    private static final String USER_TYPE = "user.type.model";
    UserModel.TripsEntity.UserEntity userModel;
    @BindView(R.id.designation)
    TextView userNameLabel;
    @BindView(R.id.trip_name)
    TextView tripNameLabel;
    @BindView(R.id.total_budget)
    TextView totalBudgetLabel;
    @BindView(R.id.total_spend)
    TextView totalSpendLabel;
    @BindView(R.id.total_remaining)
    TextView totalRemainingLabel;
    @BindView(R.id.settings_btn)
    TextView settingsButton;
    @BindView(R.id.activity_home_trip_members)
    FrameLayout mTripMembers;
    @BindView(R.id.viewreceiptsfl)
    FrameLayout mViewReceipts;

    String budget;
    String total,spent ;
    @BindView(R.id.camera_container)
    FrameLayout mAddReceipt;
    public static boolean isRecieptAdded;
    private boolean viewReceipt;

    public static Intent createIntent(Context context, UserModel.TripsEntity.UserEntity userModel,String budget) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(USER_TYPE, userModel);
        intent.putExtra("budget",budget);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeButtonEnabled(true);

        mGetCurrenciesCallBack = new CancelableCallback<>(new Callback<CurrencyModel>() {

            @Override
            public void success(CurrencyModel genricModel, Response response) {

                for (int i = 0; i < genricModel.getTrips().size(); i++) {
                    ApiConstants.SELECTED_CURRENCYLIST.add(genricModel.getTrips().get(i).getCurrency().getCurrency());
                }


                try{
                    ArrayList<CurrencyModel1> currencyModel1List = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(readFromAssets(HomeActivity.this,"country_code.txt"));
                    if(jsonArray.length() > 0)
                    {
                        for(int count=0;count<jsonArray.length(); count++)
                        {
                            JSONObject jsonObject = jsonArray.getJSONObject(count);
                            CurrencyModel1 currencyModel1 = new CurrencyModel1();
                            String name = jsonObject.getString("name");
                            String code= jsonObject.getString("code");
                            for(int selectedCount=0 ; selectedCount<ApiConstants.SELECTED_CURRENCYLIST.size();selectedCount++)
                            {
                                if(code.equalsIgnoreCase(ApiConstants.SELECTED_CURRENCYLIST.get(selectedCount).toString()))
                                {
                                    currencyModel1.setChecked(true);
                                    currencyModel1.setCode(code);
                                    currencyModel1.setName(name);
                                    currencyModel1List.add(currencyModel1);
                                    break;
                                }
                            }

                        }
                        if(ApiConstants.LEADER_SELECTED_CURRENCYLIST!=null){
                            ApiConstants.LEADER_SELECTED_CURRENCYLIST.clear();
                        }
                        if(currencyModel1List!=null){
                            CurrencyModel1 defaultCurrencyModel = new CurrencyModel1();
                            defaultCurrencyModel.setName("U.S Dollar");
                            defaultCurrencyModel.setCode("USD");
                            defaultCurrencyModel.setDisabled(true);
                            currencyModel1List.add(0,defaultCurrencyModel);

                            ApiConstants.LEADER_SELECTED_CURRENCYLIST.addAll(currencyModel1List);

                        }
                        hideProgress();
                        onUserSelection();
//                setRecyclerList(currencyModel1List);
//                        hideProgress();

                    }
                }catch (JSONException e)
                {

                }catch (IOException e)
                {
                    e.printStackTrace();
                }




            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                toast(getString(R.string.something_went));
            }
        });


        if(getIntent().getExtras()!=null){
            userModel = (UserModel.TripsEntity.UserEntity) getIntent().getSerializableExtra(USER_TYPE);
            budget=getIntent().getExtras().getString("budget");
        }else{
            userModel=new UserModel.TripsEntity.UserEntity();
            userModel.setId(SharedPreferencesHandler.getStringValues(this,"user_id"));
            userModel.setCreated(SharedPreferencesHandler.getStringValues(this,"created_date"));
            userModel.setType(SharedPreferencesHandler.getStringValues(this,"usertype"));
            userModel.setFirst_name(SharedPreferencesHandler.getStringValues(this,"first_name"));
            userModel.setMiddle_name(SharedPreferencesHandler.getStringValues(this,"middle_name"));
            userModel.setLast_name(SharedPreferencesHandler.getStringValues(this,"last_name"));
            userModel.setTrip_id(SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.TRIP_ID));
            budget=SharedPreferencesHandler.getStringValues(this,ApiConstants.Budget_Total);
            spent=SharedPreferencesHandler.getStringValues(this,ApiConstants.Budget_spent);

            getTripCurrencies();
        }

        SharedPreferencesHandler.setStringValues(this,"USERTYPE",userModel.getType());
        SharedPreferencesHandler.setStringValues(HomeActivity.this, "First_Name", userModel.getFirst_name());
        SharedPreferencesHandler.setStringValues(HomeActivity.this, "Middle_Name", userModel.getMiddle_name());
        SharedPreferencesHandler.setStringValues(HomeActivity.this, "Last_Name", userModel.getLast_name());

        SharedPreferencesHandler.setBooleanValues(HomeActivity.this,"isLoggedIn",true);
        String name="";
        if(userModel.getFirst_name()!=null){
            name=userModel.getFirst_name();
        }
        if(userModel.getMiddle_name()!=null){
            name=name+" "+userModel.getMiddle_name();
        }
        if(userModel.getLast_name()!=null){
            name=name+" "+userModel.getLast_name();
        }
        userNameLabel.setText(name);
        tripNameLabel.setText(SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.TRIP_NAME));
        mTripMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (android.os.Build.VERSION.SDK_INT >= 23){
                   showTripMembers=true;
                    checkWriteExternalPermission();
                }else{
                    startActivity(new Intent(HomeActivity.this,ViewTripMembers.class));
                    showTripMembers=false;
                }*/
                selectedValue=4;
                getArchiveStatus();

            }
        });

        if (SharedPreferencesHandler.getStringValues(HomeActivity.this,"usertype").equalsIgnoreCase(ApiConstants.USERTYPE_LEADER)) {
            mTripMembers.setVisibility(View.VISIBLE);
        }else{
            mTripMembers.setVisibility(View.GONE);
        }
        mViewReceipts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectedValue=2;
                getArchiveStatus();
              /*  if (android.os.Build.VERSION.SDK_INT >= 23){
                    viewReceipt=true;
                    checkWriteExternalPermission();
                }else{
                    startActivity(new Intent(HomeActivity.this,ViewReceiptsActivity.class));
                }*/
            }
        });
        if(userModel.getType().equalsIgnoreCase(ApiConstants.USERTYPE_STUDENT))
        {
            settingsButton.setVisibility(View.INVISIBLE);
            spent = SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.Budget_spent);
            totalSpendLabel.setText("$ "+spent);
            totalBudgetLabel.setVisibility(View.INVISIBLE);
            totalRemainingLabel.setVisibility(View.INVISIBLE);
            mTripMembers.setVisibility(View.INVISIBLE);
            ((TextView)findViewById(R.id.checkamouttv)).setVisibility(View.INVISIBLE);
            ((TextView)findViewById(R.id.remainingtv)).setVisibility(View.INVISIBLE);
        }else{
            mTripMembers.setVisibility(View.VISIBLE);
            settingsButton.setVisibility(View.VISIBLE);
            if(!budget.toString().trim().equalsIgnoreCase("")){
                total=budget;
            }else {
                total= SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.Budget_Total);

            }
            spent = SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.Budget_spent);
            Double truncatedTotal=new BigDecimal(total ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            totalBudgetLabel.setText(Html.fromHtml("<b>$ " +truncatedTotal));
            String t="<b>$ " +spent;
            totalSpendLabel.setText(Html.fromHtml(t));
            double value=Float.valueOf(total) - Float.valueOf(spent);
            value=value+0.0;
            Double truncatedDouble=new BigDecimal(value ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            totalRemainingLabel.setText(Html.fromHtml("<b>$" +truncatedDouble ));
        }
        settingsButton.setVisibility(View.INVISIBLE);
    }


    /**
     *   Camera Permission check
     */
    public void checkCameraPermission(){
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)) {
            // This condition checks whether user has earlier denied the permission or not just by clicking on deny in the permission dialog.
            //Remember not on the never ask check box then deny in the permission dialog
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                /**
                 * Show an explanation to user why this permission in needed by us. Here using alert dialog to show the permission use.
                 */
                new AlertDialog.Builder(this,R.style.datepicker)
                        .setTitle("Permission Required")
                        .setMessage("This permission was denied earlier by you." +
                                " This permission is required to click receipts.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(HomeActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        2);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {
                // Just ask for the permission for first time. This block will come into play when user is trying to use feature which requires permission grant.
                //So for the first time user will be into this else block. So just ask for the permission you need by showing default permission dialog
                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 2);
            }
        }else {
            // If permission is already granted by user then we will be into this else block. So do whatever is required here
            //            Toast.makeText(this,"Permission Already granted",Toast.LENGTH_LONG).show();

            if(showTripMembers){
                startActivity(new Intent(HomeActivity.this,ViewTripMembers.class));
                showTripMembers=false;
            }else {
                SharedPreferences sp = getSharedPreferences("betheltrips", Activity.MODE_PRIVATE);
                if (viewReceipt) {
                    startActivity(new Intent(HomeActivity.this, ViewReceiptsActivity.class));
                } else {
                    if (sp.getBoolean("show", true)) {
                        startActivity(new Intent(this, HelpActivity.class));
                    } else {
                        startActivity(new Intent(this, CameraActivity.class));
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //Menu Resource, Menu
        if (userModel.getType().equalsIgnoreCase(ApiConstants.USERTYPE_STUDENT)) {
            getMenuInflater().inflate(R.menu.logout, menu);
        }else{
            getMenuInflater().inflate(R.menu.chat_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem settingsmenu = menu.findItem(R.id.settingsmenu);
   /*     if (userModel.getType().equalsIgnoreCase(ApiConstants.USERTYPE_STUDENT)) {
            settingsmenu.setVisible(false);
        }else{
            settingsmenu.setVisible(true);
        }*/
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingsmenu:
                selectedValue=3;
                getArchiveStatus();
             /*   startActivity(LeaderSettingsActivity.createIntent(HomeActivity.this, userModel,total));
             */   return true;
            case R.id.logoutmenu:
                logoutDialog();
                return true;
            case R.id.logoutmenubtn:
                logoutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /**
     *   Write external Permission check
     */
    private void  checkWriteExternalPermission(){
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {
            // This condition checks whether user has earlier denied the permission or not just by clicking on deny in the permission dialog.
            //Remember not on the never ask check box then deny in the permission dialog
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                /**
                 * Show an explanation to user why this permission in needed by us. Here using alert dialog to show the permission use.
                 */
                new AlertDialog.Builder(this,R.style.datepicker)
                        .setTitle("Permission Required")
                        .setMessage("This permission was denied earlier by you." +
                                " This permission is required to store receipt images.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(HomeActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        4);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {
                //Just ask for the permission for first time. This block will come into play when user is trying to use feature which requires permission grant.
                //So for the first time user will be into this else block. So just ask for the permission you need by showing default permission dialog
                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
            }
        }else {
            // If permission is already granted by user then we will be into this else block. So do whatever is required here
            //            Toast.makeText(this,"Permission Already granted",Toast.LENGTH_LONG).show();
            checkCameraPermission();
        }
    }


    /**
     *  This method will be invoked when user allows or deny's a permission from the permission dialog so take actions accordingly.
     * @param requestCode : returns requestCode
     * @param permissions : returns permissions
     * @param grantResults : returns grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 4:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Toast.makeText(this, "Write Storage Permission granted", Toast.LENGTH_LONG).show();
                    checkCameraPermission();
                } else {
                    String permission = permissions[0];
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                    if (!showRationale) {
                        //We will be in this block when User has ticked the never show dialog and denied the permission from the permission dialog
                        //Here we can not request the permission again if user has denied the permission with never ask option enabled.
                        //Only way is to show a imfo dialog and ask user to grant the permission from settings.
                        //   Toast.makeText(this, "Write Storage Permission Denied with never show options.Please manually enable the permission and re-start the app to use this feature.", Toast.LENGTH_LONG).show();
                    } else {
                        //  Toast.makeText(this, "Write Storage Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case 2:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Toast.makeText(this, "Camera Permission granted", Toast.LENGTH_LONG).show();

                    if(showTripMembers){
                        startActivity(new Intent(HomeActivity.this,ViewTripMembers.class));
                    }else {
                        SharedPreferences sp = getSharedPreferences("betheltrips", Activity.MODE_PRIVATE);
                        if (viewReceipt) {
                            startActivity(new Intent(HomeActivity.this, ViewReceiptsActivity.class));
                        } else {
                            if (sp.getBoolean("show", true)) {
                                startActivity(new Intent(this, HelpActivity.class));
                            } else {
                                startActivity(new Intent(this, CameraActivity.class));
                            }
                        }
                    }
                } else {
                    String permission = permissions[0];
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                    if (!showRationale) {
                        //We will be in this block when User has ticked the never show dialog and denied the permission from the permission dialog
                        //Here we can not request the permission again if user has denied the permission with never ask option enabled.
                        //Only way is to show a imfo dialog and ask user to grant the permission from settings.
                        //    Toast.makeText(this, "Camera Permission Denied with never show options.Please manually enable the permission and re-start the app to use this feature.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;


            default:
        }
    }



    @Override
    public int getLayout() {
        return R.layout.activity_home;
    }
    int selectedValue=0;


    @OnClick({R.id.settings_btn, R.id.logout_btn,R.id.camera_container})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settings_btn:
//                finish();
                getArchiveStatus();
                startActivity(LeaderSettingsActivity.createIntent(HomeActivity.this, userModel,total));
                break;
            case R.id.logout_btn:
                logoutDialog();
                break;
            case R.id.camera_container:
//                startActivity(new Intent(this,AddReceiptActivity.class));
                selectedValue=1;
                getArchiveStatus();
               /* viewReceipt=false;
                if (android.os.Build.VERSION.SDK_INT >= 23){
                    checkWriteExternalPermission();
                }else{
                    startActivity(new Intent(this,CameraActivity.class));
                }*/

                break;
        }
    }

    boolean showTripMembers;

    private void logoutDialog() {
        final AppDatabase appDatabase = new AppDatabase(this);

        Cursor cursor = appDatabase.getRowCount(SharedPreferencesHandler.getStringValues(this, ApiConstants.TRIP_ID));

        if (cursor.getCount() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You have receipts for this trip that are waiting to be uploaded. Logging out from the app will automatically delete any Saved for later receipts. Would you like to upload them now?")
                    .setCancelable(false)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                            dialog.dismiss();
                        }
                    }).setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //do things
                    appDatabase.clearSavedTable();
                    appDatabase.clearUploadedRowValues();
                    ApiConstants.LEADER_SELECTED_CURRENCYLIST.clear();
                    ApiConstants.SELECTED_CURRENCYLIST.clear();
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, ApiConstants.TRIP_USERNAME, "");
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, ApiConstants.TRIP_ID, "");
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, ApiConstants.TRIP_NAME, "");
                    SharedPreferencesHandler.clearObject(HomeActivity.this, ApiConstants.PREF_LOGIN_MODEL);
                    finishAffinity();
                    startActivity(new Intent(HomeActivity.this, WelcomeActivity.class));
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

        } else {

            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage(
                            "Are you sure you want to log out?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    ApiConstants.LEADER_SELECTED_CURRENCYLIST.clear();
                                    ApiConstants.SELECTED_CURRENCYLIST.clear();
                                    SharedPreferencesHandler.setStringValues(HomeActivity.this, ApiConstants.TRIP_USERNAME, "");
                                    SharedPreferencesHandler.setStringValues(HomeActivity.this, ApiConstants.TRIP_ID, "");
                                    SharedPreferencesHandler.setStringValues(HomeActivity.this, ApiConstants.TRIP_NAME, "");
                                    SharedPreferencesHandler.setStringValues(HomeActivity.this, "user_id", "");
                                    SharedPreferencesHandler.clearObject(HomeActivity.this, ApiConstants.PREF_LOGIN_MODEL);
                                    finishAffinity();
                                    startActivity(new Intent(HomeActivity.this, WelcomeActivity.class));
                                }
                            })
                    .setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                        }
                    }).show();

        }
    }

    private void getBudgetDetails(){
        if (isNetworkAvailableWithNoDialog(this)) {
            showProgress();
        }
        if (SharedPreferencesHandler.getStringValues(HomeActivity.this,"usertype").equalsIgnoreCase(ApiConstants.USERTYPE_LEADER)) {
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", SharedPreferencesHandler.getStringValues(HomeActivity.this,ApiConstants.TRIP_ID));
            jsonRequest.addProperty("first_name", SharedPreferencesHandler.getStringValues(HomeActivity.this,"first_name"));
            jsonRequest.addProperty("middle_name",SharedPreferencesHandler.getStringValues(HomeActivity.this,"middle_name"));
            jsonRequest.addProperty("last_name", SharedPreferencesHandler.getStringValues(HomeActivity.this,"last_name"));

            ApiClient.getApiClient().getBudgetDetails(jsonRequest, new CancelableCallback<Example>(new Callback<Example>() {
                @Override
                public void success(Example budgetDetails, Response response) {
                    SharedPreferencesHandler.setStringValues(HomeActivity.this,
                            ApiConstants.Budget_Total, budgetDetails.getTripDetails().getBudget());
                    SharedPreferencesHandler.setStringValues(HomeActivity.this,
                            ApiConstants.Budget_spent, budgetDetails.getTripDetails().getTotalSpent());

                    spent = SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.Budget_spent);
                    totalSpendLabel.setText("$ "+spent);
                    total= SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.Budget_Total);

                    spent = SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.Budget_spent);
                    totalBudgetLabel.setText(Html.fromHtml("<b>$ " +total));
                    String t="<b>$ " +spent;
                    totalSpendLabel.setText(Html.fromHtml(t));
                    double value=Float.valueOf(total) - Float.valueOf(spent);
                    value=value+0.0;
                    Double truncatedDouble=new BigDecimal(value ).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                    totalRemainingLabel.setText(Html.fromHtml("<b>$" +truncatedDouble ));
                    hideProgress();
                }

                @Override
                public void failure(RetrofitError error) {
                    hideProgress();
                    //  Toast.makeText(SelectUserActivity.this,"Oops some error occured, try after some time",Toast.LENGTH_LONG).show();
                }
            }));

        } else {
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", SharedPreferencesHandler.getStringValues(HomeActivity.this,ApiConstants.TRIP_ID));
            jsonRequest.addProperty("first_name", SharedPreferencesHandler.getStringValues(HomeActivity.this,"first_name"));
            jsonRequest.addProperty("middle_name",SharedPreferencesHandler.getStringValues(HomeActivity.this,"middle_name"));
            jsonRequest.addProperty("last_name", SharedPreferencesHandler.getStringValues(HomeActivity.this,"last_name"));

            ApiClient.getApiClient().getStudentBudgetDetails(jsonRequest, new CancelableCallback<StudentBudgetDetails>(new Callback<StudentBudgetDetails>() {
                @Override
                public void success(StudentBudgetDetails budgetDetails, Response response) {
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, ApiConstants.Budget_spent, budgetDetails.getTripDetails().getTotalSpent());

                    spent = SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.Budget_spent);
                    totalSpendLabel.setText("$ "+spent);
                 //   total= SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.Budget_Total);

                   // spent = SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.Budget_spent);
                   // totalBudgetLabel.setText(Html.fromHtml("<b>$ " +total));
                    //String t="<b>$ " +spent;
                   // totalSpendLabel.setText(Html.fromHtml(t));
                  //  double value=Float.valueOf(total) - Float.valueOf(spent);
                   // value=value+0.0;
                    //Double truncatedDouble=new BigDecimal(value ).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                    //totalRemainingLabel.setText(Html.fromHtml("<b>$" +truncatedDouble ));
                    hideProgress();
                    //  startActivity(HomeActivity.createIntent(HomeActivity.this, userModel, ""));
                }

                @Override
                public void failure(RetrofitError error) {
                }
            }));
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isRecieptAdded){
            getBudgetDetails();
            isRecieptAdded=false;
        }
    }

    private void getArchiveStatus() {

        String url ="http://betheltripreceipts.com/services/isArchived";
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("trip_id",SharedPreferencesHandler.getStringValues(this, ApiConstants.TRIP_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isNetworkAvailableWithNoDialog(this)) {
            showProgress();
            JsonObjectRequest jsonObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, jsonRequest, new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideProgress();
                            try {
                                if (response.getString("status").equalsIgnoreCase("success")) {
                                    if (response.getJSONObject("trip_details").getString("archived").equalsIgnoreCase("1")) {
                                        //open Archive screen here
                                        startActivity(new Intent(HomeActivity.this, ArchiveActivity.class));

                                    } else {
                                        //startActivity(new Intent(HomeActivity.this,ArchiveActivity.class));
                                        openActivity();
                                    }
                                } else {
                                    openActivity();
                                    //startActivity(LeaderSettingsActivity.createIntent(HomeActivity.this, userModel,total));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //    Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                            // parseApiResponse(response);

                        }
                    },
                            new com.android.volley.Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    hideProgress();
                                }
                            });

            // Add the request to the RequestQueue.
            jsonObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetworkSingleton.getInstance(this).addToRequestQueue(jsonObjRequest);
        }else{
            openActivity();
        }
    }


    private void openActivity(){
        if(selectedValue==1){
            viewReceipt=false;
            if (android.os.Build.VERSION.SDK_INT >= 23){
                checkWriteExternalPermission();
            }else{
                startActivity(new Intent(HomeActivity.this,CameraActivity.class));
            }
        }else if(selectedValue==2){
            if (android.os.Build.VERSION.SDK_INT >= 23){
                viewReceipt=true;
                checkWriteExternalPermission();
            }else{
                startActivity(new Intent(HomeActivity.this,ViewReceiptsActivity.class));
            }
        }else if(selectedValue==3){
            startActivity(LeaderSettingsActivity.createIntent(HomeActivity.this, userModel,total));
        }else if(selectedValue==4){
            if (android.os.Build.VERSION.SDK_INT >= 23){
                showTripMembers=true;
                checkWriteExternalPermission();
            }else{
                startActivity(new Intent(HomeActivity.this,ViewTripMembers.class));
                showTripMembers=false;
            }
        }
    }
    private void getTripCurrencies() {
        if (isNetworkAvailableWithNoDialog(this)) {
            showProgress();
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", SharedPreferencesHandler.getStringValues(HomeActivity.this, ApiConstants.TRIP_ID));

            ApiClient.getApiClient().getTripCurrencyList(jsonRequest, mGetCurrenciesCallBack);
        } else {
            // toast(getString(R.string.internet_not_available));
        }
    }
    private CancelableCallback<CurrencyModel> mGetCurrenciesCallBack;

    public  String readFromAssets(Context context, String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

        // do reading, usually loop until end of file reading
        StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine); // process line
            mLine = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }


    private void onUserSelection() {
        if(CommonUtils.isNetworkAvailable(this)) {
            if (userModel != null) {
                showProgress();
                if (userModel.getType().equalsIgnoreCase(ApiConstants.USERTYPE_LEADER)) {
                    JsonObject jsonRequest = new JsonObject();
                    jsonRequest.addProperty("trip_id", userModel.getTrip_id());
                    jsonRequest.addProperty("first_name", userModel.getFirst_name());
                    jsonRequest.addProperty("middle_name", userModel.getMiddle_name());
                    jsonRequest.addProperty("last_name", userModel.getLast_name());
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, "usertype", userModel.getType());
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, "first_name", userModel.getFirst_name());
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, "middle_name", userModel.getMiddle_name());
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, "last_name", userModel.getLast_name());

                    ApiClient.getApiClient().getBudgetDetails(jsonRequest, new CancelableCallback<Example>(new Callback<Example>() {
                        @Override
                        public void success(Example budgetDetails, Response response) {
                            SharedPreferencesHandler.setStringValues(HomeActivity.this, ApiConstants.Budget_Total, budgetDetails.getTripDetails().getBudget());
                            SharedPreferencesHandler.setStringValues(HomeActivity.this, ApiConstants.Budget_spent, budgetDetails.getTripDetails().getTotalSpent());
                            if (budgetDetails.getTripDetails().getBudget().startsWith("0")) {
                                //   startActivity(BudgetScreenActivity.createIntent(HomeActivity.this, userModel));
                                //     finish();
//                                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, ApiConstants.TRIP_USERNAME, userModel.getFirst_name() + " "
//                                        + userModel.getMiddle_name() + " " + userModel.getLast_name());
                            } else {
                                //  startActivity(HomeActivity.createIntent(HomeActivity.this, userModel, ""));
                                //      finish();
                            }
//                                startActivity(BudgetScreenActivity.createIntent(SelectUserActivity.this, userModel));
//                                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, ApiConstants.TRIP_USERNAME, userModel.getFirst_name() + " "
//                                        + userModel.getMiddle_name() + " " + userModel.getLast_name());
                            hideProgress();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            hideProgress();
                            Toast.makeText(HomeActivity.this,"Oops some error occured, try after some time",Toast.LENGTH_LONG).show();
                        }
                    }));

                } else {
                    JsonObject jsonRequest = new JsonObject();
                    jsonRequest.addProperty("trip_id", userModel.getTrip_id());
                    jsonRequest.addProperty("first_name", userModel.getFirst_name());
                    jsonRequest.addProperty("middle_name", userModel.getMiddle_name());
                    jsonRequest.addProperty("last_name", userModel.getLast_name());
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, "usertype", userModel.getType());
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, "first_name", userModel.getFirst_name());
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, "middle_name", userModel.getMiddle_name());
                    SharedPreferencesHandler.setStringValues(HomeActivity.this, "last_name", userModel.getLast_name());

                    ApiClient.getApiClient().getStudentBudgetDetails(jsonRequest, new CancelableCallback<StudentBudgetDetails>(new Callback<StudentBudgetDetails>() {
                        @Override
                        public void success(StudentBudgetDetails budgetDetails, Response response) {
                            try {
                                SharedPreferencesHandler.setStringValues(HomeActivity.this, ApiConstants.Budget_spent, budgetDetails.getTripDetails().getTotalSpent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //     startActivity(HomeActivity.createIntent(HomeActivity.this, userModel, ""));
                            //   finish();
//                                startActivity(HomeActivity.createIntent(SelectUserActivity.this, userModel));
//                                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, ApiConstants.TRIP_USERNAME, userModel.getFirst_name() + " "
//                                        + userModel.getMiddle_name() + " " + userModel.getLast_name());
                            hideProgress();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            hideProgress();
                            Toast.makeText(HomeActivity.this,"Oops some error occured, try after some time",Toast.LENGTH_LONG).show();
                        }
                    }));
                }
            } else {
                CommonUtils.toast("Please select at least one user.");
            }
        }

    }

}
// Oops some error occured, try after some time