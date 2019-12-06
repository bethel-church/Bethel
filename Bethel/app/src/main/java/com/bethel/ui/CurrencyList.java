package com.bethel.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.bethel.R;
import com.bethel.adapter.SelectCurrencyAdapter;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.model.CurrencyModel;
import com.bethel.model.CurrencyModel1;
import com.bethel.model.GenricModel;
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

public class CurrencyList extends BaseActivity {
    @BindView(R.id.recyclerCurrencies)
    RecyclerView recyclerView;
    List<CurrencyModel1> resources = new ArrayList<>();
    SelectCurrencyAdapter mAdapter;
    private CancelableCallback<GenricModel> mSaveCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getCurrenciesList();

        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        try{
            ArrayList<CurrencyModel1> currencyModel1List = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(readFromAssets(CurrencyList.this,"country_code.txt"));
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
                            break;
                        }
                    }
                    currencyModel1.setCode(code);
                    currencyModel1.setName(name);
                    currencyModel1List.add(currencyModel1);
                }
//                setRecyclerList(currencyModel1List);
                mAdapter = new SelectCurrencyAdapter(CurrencyList.this, currencyModel1List);
                this.resources = currencyModel1List;
                recyclerView.setAdapter(mAdapter);
            }
        }catch (JSONException e)
        {

        }catch (IOException e)
        {
            e.printStackTrace();
        }
    mSaveCallBack = new CancelableCallback<>(new Callback<GenricModel>() {

        @Override
        public void success(GenricModel genricModel, Response response) {
            hideProgress();
            CurrencyModel1 defaultCurrencyModel = new CurrencyModel1();
            defaultCurrencyModel.setName("U.S Dollar");
            defaultCurrencyModel.setCode("USD");
            defaultCurrencyModel.setDisabled(true);
            ApiConstants.LEADER_SELECTED_CURRENCYLIST.add(0,defaultCurrencyModel);

            if (genricModel.getStatus().equalsIgnoreCase(ApiConstants.SUCCESS)) {
                  /*  CurrencyModel1 defaultCurrencyModel = new CurrencyModel1();
                    defaultCurrencyModel.setName("U.S Dollar");
                    defaultCurrencyModel.setCode("USD");
                    defaultCurrencyModel.setDisabled(true);
                    ApiConstants.LEADER_SELECTED_CURRENCYLIST.add(0,defaultCurrencyModel);
*/
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




    }

    @Override
    public int getLayout() {
        return R.layout.activity_currency_list;
    }

    @OnClick({R.id.back_label, R.id.save_label})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_label:
                finish();
                break;
            case R.id.save_label:
                saveTripCurrencies();
                break;
        }
    }

    private void saveTripCurrencies() {
        if (isNetworkAvailable(this)) {
            ArrayList<String> mCurrencies = new ArrayList<>();
            ApiConstants.SELECTED_CURRENCYLIST.clear();
            ApiConstants.LEADER_SELECTED_CURRENCYLIST.clear();
            if (resources != null) {
                for (int i = 0; i < resources.size(); i++) {
                    if (resources.get(i).isChecked()) {
                        mCurrencies.add(resources.get(i).getCode());
                        ApiConstants.SELECTED_CURRENCYLIST.add(resources.get(i).getCode());
                        ApiConstants.LEADER_SELECTED_CURRENCYLIST.add(resources.get(i));
                    }
                }
            }

            if (mCurrencies.size() == 0) {
                toast("Please select at least one currencies");
                return;
            }

            String curranecyArray = TextUtils.join(",", mCurrencies);

            showProgress();
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", SharedPreferencesHandler.getStringValues(CurrencyList.this, ApiConstants.TRIP_ID));

            Gson gson=new Gson();
            JsonArray  jsonArray=new JsonArray();
            for(int i=0;i<mCurrencies.size();i++){
                jsonArray.add(mCurrencies.get(i));
            }
            jsonRequest.add("currencies", jsonArray);

            ApiClient.getApiClient().setTripCurrencyList(jsonRequest, mSaveCallBack);
        } else {
            toast(getString(R.string.internet_not_available));
        }
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




}
