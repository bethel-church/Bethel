package com.bethel.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.adapter.SelectCurrencyAdapter;
import com.bethel.adapter.SelectedCurrencyAdapter;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.model.CurrencyModel;
import com.bethel.model.CurrencyModel1;
import com.bethel.model.GenricModel;
import com.bethel.model.UserModel;
import com.bethel.utils.CommonUtils;
import com.bethel.utils.SharedPreferencesHandler;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.bethel.utils.CommonUtils.isNetworkAvailable;
import static com.bethel.utils.CommonUtils.toast;

/**
 * Created by siddharth.brahmi on 10/7/2016.
 */

public class LeaderSettingsActivity extends BaseActivity implements SelectedCurrencyAdapter.onItemDeletedListener {

    private static final String USER_TYPE = "user.type.model";
    @BindView(R.id.activity_leader_budget)
    EditText topLabel;

    @BindView(R.id.activity_leader_saveBudget)
    TextView saveBudget;

    @BindView(R.id.add_currency_list)
    TextView currencyList;

    @BindView(R.id.activity_leader_currencyList)
    ListView currencyListView;
    @BindView(R.id.linearlayout)
    LinearLayout linearLayout;

    private CancelableCallback<GenricModel> mSaveCallBack;

    private CancelableCallback<CurrencyModel> mGetCurrenciesCallBack;


    private SelectedCurrencyAdapter mCurrenciesAdapter;
    private List<CurrencyModel1> mSelectedCurrencyModelList;
    private CancelableCallback<GenricModel> mCallBack;
    UserModel.TripsEntity.UserEntity userModel;

    public static Intent createIntent(Context context, UserModel.TripsEntity.UserEntity userModel,String budget) {
        Intent intent = new Intent(context, LeaderSettingsActivity.class);
        intent.putExtra(USER_TYPE, userModel);
        intent.putExtra("budget",budget);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        CurrencyModel1 defaultCurrencyModel = new CurrencyModel1();
        defaultCurrencyModel.setName("U.S Dollar");
        defaultCurrencyModel.setCode("USD");
        defaultCurrencyModel.setDisabled(true);
        if(ApiConstants.LEADER_SELECTED_CURRENCYLIST!=null){
            if(ApiConstants.LEADER_SELECTED_CURRENCYLIST.size()==0){
                ApiConstants.LEADER_SELECTED_CURRENCYLIST.add(0,defaultCurrencyModel);
            }
        }

    }


    private void getTripCurrencies() {
        if (isNetworkAvailable(this)) {
            showProgress();
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", SharedPreferencesHandler.getStringValues(LeaderSettingsActivity.this, ApiConstants.TRIP_ID));

            ApiClient.getApiClient().getTripCurrencyList(jsonRequest, mGetCurrenciesCallBack);
        } else {
            toast(getString(R.string.internet_not_available));
        }
    }

    LayoutInflater mLayoutInflater;
    int temp;
    @Override
    protected void onResume() {
        super.onResume();
        userModel = (UserModel.TripsEntity.UserEntity) getIntent().getSerializableExtra(USER_TYPE);
        topLabel.setText(getIntent().getExtras().getString("budget"));

       //mCurrenciesAdapter = new SelectedCurrencyAdapter(LeaderSettingsActivity.this, this,mSelectedCurrencyModelList);

//        mCurrenciesAdapter.setList(mSelectedCurrencyModelList);
       // currencyListView.setAdapter(mCurrenciesAdapter);

        mCallBack = new CancelableCallback<>(new Callback<GenricModel>() {

            @Override
            public void success(GenricModel genricModel, Response response) {
                hideProgress();

                if (genricModel.getStatus().equalsIgnoreCase(ApiConstants.SUCCESS)) {
                    ApiConstants.LEADER_SELECTED_CURRENCYLIST.clear();
                    startActivity(HomeActivity.createIntent(LeaderSettingsActivity.this, userModel,topLabel.getText().toString()));
                    finish();
                }
                toast(genricModel.getMessage());
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                toast(getString(R.string.something_went));
            }
        });


        mGetCurrenciesCallBack = new CancelableCallback<>(new Callback<CurrencyModel>() {

            @Override
            public void success(CurrencyModel genricModel, Response response) {




                for (int i = 0; i < genricModel.getTrips().size(); i++) {
                    ApiConstants.SELECTED_CURRENCYLIST.add(genricModel.getTrips().get(i).getCurrency().getCurrency());
                }


                try{
                    ArrayList<CurrencyModel1> currencyModel1List = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(readFromAssets(LeaderSettingsActivity.this,"country_code.txt"));
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
                        ApiConstants.LEADER_SELECTED_CURRENCYLIST.addAll(currencyModel1List);
//                setRecyclerList(currencyModel1List);
                        addViews();

                    }
                }catch (JSONException e)
                {

                }catch (IOException e)
                {
                    e.printStackTrace();
                }


                hideProgress();

            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                toast(getString(R.string.something_went));
            }
        });

        if(ApiConstants.SELECTED_CURRENCYLIST.size()==0) {
            getTripCurrencies();
        }else{
            addViews();
        }


        mSaveCallBack = new CancelableCallback<>(new Callback<GenricModel>() {

            @Override
            public void success(GenricModel genricModel, Response response) {
                Log.e("genric","");

            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                toast(getString(R.string.something_went));
            }
        });


    }

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

    private void addViews(){
        mSelectedCurrencyModelList = new ArrayList<>();
        mSelectedCurrencyModelList.addAll(ApiConstants.LEADER_SELECTED_CURRENCYLIST);
   /*  boolean hasDefaultValue=false;
        for(int i=0;i<mSelectedCurrencyModelList.size();i++){
         if(mSelectedCurrencyModelList.get(i).getName().equalsIgnoreCase("U.S Dollar")){
             hasDefaultValue=true;
             break;
         }else{
             hasDefaultValue=false;
         }
     }
        if(!hasDefaultValue){
            CurrencyModel1 defaultCurrencyModel = new CurrencyModel1();
            defaultCurrencyModel.setName("U.S Dollar");
            defaultCurrencyModel.setCode("USD");
            defaultCurrencyModel.setDisabled(true);
            mSelectedCurrencyModelList.add(0,defaultCurrencyModel);
        }
*/

        mLayoutInflater = LayoutInflater.from(this);
        linearLayout.removeAllViews();
        for(int i=0;i<ApiConstants.LEADER_SELECTED_CURRENCYLIST.size();i++){
            temp=i;
            View convertView = mLayoutInflater.inflate(R.layout.activity_leader_settings_selected_currencies, null);
            TextView mCurrencyname = (TextView) convertView.findViewById(R.id.activity_leader_setting_selected_currency);
            ImageView mDeleteCurrency = (ImageView)convertView.findViewById(R.id.activity_leader_setting_selected_currency_delete);
            TextView name=(TextView)convertView.findViewById(R.id.currencnyname);
            RelativeLayout rlBackground=(RelativeLayout)convertView.findViewById(R.id.currencybgrl);
            mDeleteCurrency.setTag(i);
            mDeleteCurrency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ApiConstants.SELECTED_CURRENCYLIST.remove(ApiConstants.LEADER_SELECTED_CURRENCYLIST.get((int)v.getTag()).getCode());
                    mSelectedCurrencyModelList.remove((int)v.getTag());
                    ApiConstants.LEADER_SELECTED_CURRENCYLIST.remove((int)v.getTag());
                    linearLayout.removeViewAt((Integer) v.getTag());
                    saveTripCurrencies();
                    addViews();
                }
            });

            Drawable d;
            if(!mSelectedCurrencyModelList.get(i).isDisabled())
            {/*
                d = mCurrencyname.getBackground();
                PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
                d.setColorFilter(Color.parseColor("#DBDBDB"), mMode);*/
               /* mDeleteCurrency.setImageDrawable(ContextCompat.getDrawable(LeaderSettingsActivity.this,R.drawable.light_grey_bg));
                mCurrencyname.setTextColor(Color.parseColor("#9A9A9A"));*/
                d = mDeleteCurrency.getDrawable();
                PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
                d.setColorFilter(Color.parseColor("#DADADA"), mMode);
                mDeleteCurrency.setImageDrawable(d);
                mDeleteCurrency.setVisibility(View.VISIBLE);
                rlBackground.setBackgroundResource(R.drawable.grey_rounded);

//                mDeleteCurrency.setVisibility(View.INVISIBLE);
            }else{
                /*d = mDeleteCurrency.getDrawable();
                PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
                d.setColorFilter(Color.parseColor("#DADADA"), mMode);
                mDeleteCurrency.setImageDrawable(d);
                mDeleteCurrency.setVisibility(View.VISIBLE);
                mCurrencyname.setBackground(ContextCompat.getDrawable(this,R.drawable.grey_rounded));*/
//                mDeleteCurrency.setImageDrawable(ContextCompat.getDrawable(LeaderSettingsActivity.this,R.drawable.light_grey_bg));
                mCurrencyname.setTextColor(Color.parseColor("#9A9A9A"));
                name.setTextColor(Color.parseColor("#9A9A9A"));
//                rlBackground.setBackground(ContextCompat.getDrawable(this,R.drawable.light_grey_bg));
            }
            mCurrencyname.setText(mSelectedCurrencyModelList.get(i).getCode());
            name.setText(mSelectedCurrencyModelList.get(i).getName());
            linearLayout.addView(convertView);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.activity_leader_settings;
    }

    @OnClick({R.id.activity_leader_saveBudget, R.id.add_currency_list})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_leader_saveBudget:
                setBudget();
                break;
            case R.id.add_currency_list:
                startActivity(new Intent(LeaderSettingsActivity.this, CurrencyList.class));
                break;
        }
    }

    @Override
    public void deletedItemPosition(int position) {
        if (mSelectedCurrencyModelList != null) {
            mSelectedCurrencyModelList.remove(position);
//            mCurrenciesAdapter.setList(mSelectedCurrencyModelList);
            mCurrenciesAdapter.notifyDataSetChanged();
        }
    }

    private void setBudget() {

        if (isNetworkAvailable(this)) {

            if (TextUtils.isEmpty(topLabel.getText().toString())) {
                CommonUtils.toast("Please enter trip amount.");
                return;
            }
            showProgress();
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", userModel.getTrip_id());
            jsonRequest.addProperty("trip_budget", topLabel.getText().toString());
            ApiClient.getApiClient().setTripBudget(jsonRequest, mCallBack);
        } else {
            toast(getString(R.string.internet_not_available));
        }

    }

    private void saveTripCurrencies() {
        if (isNetworkAvailable(this)) {
            ArrayList<String> mCurrencies = new ArrayList<>();
            //ApiConstants.SELECTED_CURRENCYLIST.clear();
           // ApiConstants.LEADER_SELECTED_CURRENCYLIST.clear();
            if (ApiConstants.LEADER_SELECTED_CURRENCYLIST!= null) {
                for (int i = 0; i < ApiConstants.LEADER_SELECTED_CURRENCYLIST.size(); i++) {
                    if(!ApiConstants.LEADER_SELECTED_CURRENCYLIST.get(i).getCode().equalsIgnoreCase("USD"))
                        mCurrencies.add(ApiConstants.LEADER_SELECTED_CURRENCYLIST.get(i).getCode());
                }
            }
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", SharedPreferencesHandler.getStringValues(LeaderSettingsActivity.this, ApiConstants.TRIP_ID));

            Gson gson=new Gson();
            JsonArray jsonArray=new JsonArray();
            for(int i=0;i<mCurrencies.size();i++){
                jsonArray.add(mCurrencies.get(i));
            }
            jsonRequest.add("currencies", jsonArray);

            ApiClient.getApiClient().setTripCurrencyList(jsonRequest, mSaveCallBack);
        } else {
            toast(getString(R.string.internet_not_available));
        }
    }

}

