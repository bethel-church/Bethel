package com.bethel.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bethel.R;
import com.bethel.adapter.SelectUserAdapter;
import com.bethel.adapter.ViewMembersAdapter;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.api.NetworkSingleton;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.model.CurrencyModel;
import com.bethel.model.CurrencyModel1;
import com.bethel.model.Example;
import com.bethel.model.UserDetailsModel;
import com.bethel.model.UserModel;
import com.bethel.model.studentbudget.StudentBudgetDetails;
import com.bethel.utils.CommonUtils;
import com.bethel.utils.SharedPreferencesHandler;
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

import static com.bethel.utils.CommonUtils.isNetworkAvailableWithNoDialog;
import static com.bethel.utils.CommonUtils.toast;

public class ViewTripMembers extends BaseActivity implements SelectUserAdapter.OnUserClickListener {

    private static final String USER_TYPE = "com.user.list";
    @BindView(R.id.recyclerTrips)
    ListView recyclerTrips;
    @BindView(R.id.top_label)
    TextView mTopLabel;
    ArrayList<UserModel.TripsEntity.UserEntity> userList = new ArrayList<>();
    ViewMembersAdapter mAdapter;
    @BindView(R.id.usersearchet)
    EditText etUserSearch;
    UserModel.TripsEntity.UserEntity userModel;
    @BindView(R.id.cancel_button)
    TextView tvCancel;

    public static Intent createIntent(Context context, List<UserModel.TripsEntity.UserEntity> user) {
        Intent intent = new Intent(context, ViewTripMembers.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(USER_TYPE, (ArrayList<?>) user);
        intent.putExtras(bundle);
//        intent.putParcelableArrayListExtra(USER_TYPE, (ArrayList<? extends Parcelable>) user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        userList =(ArrayList<UserModel.TripsEntity.UserEntity>) getIntent().getSerializableExtra(USER_TYPE);


        etUserSearch.clearFocus();
        recyclerTrips.requestFocus();
        etUserSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mGetCurrenciesCallBack = new CancelableCallback<>(new Callback<UserModel>() {

            @Override
            public void success(UserModel genricModel, Response response) {

                Log.e("","");

            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                toast(getString(R.string.something_went));
            }
        });



fetchReceipts();
//getTripMembers();

    }

    private CancelableCallback<UserModel> mGetCurrenciesCallBack;

    private void getTripMembers() {
        if (isNetworkAvailableWithNoDialog(this)) {
            showProgress();
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", SharedPreferencesHandler.getStringValues(ViewTripMembers.this, ApiConstants.TRIP_ID));

            ApiClient.getApiClient().getAllTripMembers(jsonRequest, mGetCurrenciesCallBack);
        } else {
            // toast(getString(R.string.internet_not_available));
        }
    }

    @Override
    public int getLayout() {
        return R.layout.viewtripmembers;
    }


    @OnClick({ R.id.cancel_button})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.cancel_button:
                etUserSearch.setText("");
                if(getCurrentFocus()!=null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                break;
        }
    }


    @Override
    public void OnUserClicked(UserModel.TripsEntity.UserEntity userModel) {

    }

    private void fetchReceipts() {
        showProgress();
        String url ="http://betheltripreceipts.com/services/get_user_details";
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("trip_id", SharedPreferencesHandler.getStringValues(this,ApiConstants.TRIP_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
             JsonObjectRequest jsonObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, jsonRequest, new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                          //      Toast.makeText(ViewTripMembers.this, response.toString(), Toast.LENGTH_SHORT).show();
                            parseApiResponse(response);

                        }
                    },
                            new com.android.volley.Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    hideProgress();
                                    //Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

            // Add the request to the RequestQueue.
            jsonObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetworkSingleton.getInstance(this).addToRequestQueue(jsonObjRequest);

    }

    private void parseApiResponse(JSONObject response) {
        try {
            if(response.getString("status").equalsIgnoreCase("success")){
                ArrayList<UserDetailsModel>userDetailsModels=new ArrayList<>();
                JSONArray jsonArray=response.getJSONArray("data");
                    for(int i=0;i<jsonArray.length();i++){
                         JSONObject jsonObject=jsonArray.getJSONObject(i).getJSONObject("User");
                        UserDetailsModel userDetailsModel=new UserDetailsModel();

                        userDetailsModel.setFirst_name(jsonObject.getString("first_name"));
                        userDetailsModel.setId(jsonObject.getString("id"));
                        userDetailsModel.setMiddle_name(jsonObject.getString("middle_name"));
                        userDetailsModel.setLast_name(jsonObject.getString("last_name"));
                        userDetailsModel.setTotal_spent(jsonObject.getString("total_spent"));
                        userDetailsModels.add(userDetailsModel);
                    }
                Log.e("","");
                mAdapter = new ViewMembersAdapter(ViewTripMembers.this, userDetailsModels);
                recyclerTrips.setAdapter(mAdapter);
                hideProgress();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
