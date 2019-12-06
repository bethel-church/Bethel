package com.bethel.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bethel.R;
import com.bethel.adapter.SelectUserAdapter;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.api.NetworkSingleton;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.model.Example;
import com.bethel.model.UserModel;
import com.bethel.model.studentbudget.StudentBudgetDetails;
import com.bethel.utils.CommonUtils;
import com.bethel.utils.SharedPreferencesHandler;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ViewTripMembersActivity extends BaseActivity implements SelectUserAdapter.OnUserClickListener {

    private static final String USER_TYPE = "com.user.list";
    @BindView(R.id.recyclerTrips)
    RecyclerView recyclerTrips;
    @BindView(R.id.txtContinue)
    TextView txtContinue;
    @BindView(R.id.top_label)
    TextView mTopLabel;
    ArrayList<UserModel.TripsEntity.UserEntity> userList = new ArrayList<>();
    SelectUserAdapter mAdapter;
    @BindView(R.id.usersearchet)
    EditText etUserSearch;
    UserModel.TripsEntity.UserEntity userModel;
    @BindView(R.id.cancel_button)
    TextView tvCancel;

    public static Intent createIntent(Context context, List<UserModel.TripsEntity.UserEntity> user) {
        Intent intent = new Intent(context, ViewTripMembersActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(USER_TYPE, (ArrayList<?>) user);
        intent.putExtras(bundle);
//        intent.putParcelableArrayListExtra(USER_TYPE, (ArrayList<? extends Parcelable>) user);
        return intent;
    }


    private void fetchReceipts() {
        showProgress();
        String url ="http://betheltripreceipts.com/services/get_all_receipts";
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("trip_id", SharedPreferencesHandler.getStringValues(this,ApiConstants.TRIP_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(CommonUtils.isNetworkAvailableWithNoDialog(this)) {
            JsonObjectRequest jsonObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, jsonRequest, new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //    Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                            //parseApiResponse(response);

                        }
                    },
                            new com.android.volley.Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    hideProgress();
                                    Toast.makeText(ViewTripMembersActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

            // Add the request to the RequestQueue.
            jsonObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetworkSingleton.getInstance(this).addToRequestQueue(jsonObjRequest);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

       /* userList =(ArrayList<UserModel.TripsEntity.UserEntity>) getIntent().getSerializableExtra(USER_TYPE);
        recyclerTrips.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SelectUserAdapter(ViewTripMembersActivity.this, userList);
        recyclerTrips.setAdapter(mAdapter);

        etUserSearch.clearFocus();
        recyclerTrips.requestFocus();
        mTopLabel.setText(Html.fromHtml(getResources().getString(R.string.hey_welcome)));
        */etUserSearch.addTextChangedListener(new TextWatcher() {
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


    }

    @Override
    public int getLayout() {
        return R.layout.activity_select_user;
    }


    @OnClick({R.id.txtContinue, R.id.cancel_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtContinue:
                if(CommonUtils.isNetworkAvailable(this)) {
                    txtContinue.setEnabled(false);
                    if (userModel != null) {
                        txtContinue.setEnabled(false);
                        showProgress();
                        if (userModel.getType().equalsIgnoreCase(ApiConstants.USERTYPE_LEADER)) {
                            JsonObject jsonRequest = new JsonObject();
                            jsonRequest.addProperty("trip_id", userModel.getTrip_id());
                            jsonRequest.addProperty("first_name", userModel.getFirst_name());
                            jsonRequest.addProperty("middle_name", userModel.getMiddle_name());
                            jsonRequest.addProperty("last_name", userModel.getLast_name());
                            SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, "usertype", userModel.getType());
                            SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, "first_name", userModel.getFirst_name());
                            SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, "middle_name", userModel.getMiddle_name());
                            SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, "last_name", userModel.getLast_name());

                            ApiClient.getApiClient().getBudgetDetails(jsonRequest, new CancelableCallback<Example>(new Callback<Example>() {
                                @Override
                                public void success(Example budgetDetails, Response response) {
                                    txtContinue.setEnabled(true);
                                    SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, ApiConstants.Budget_Total, budgetDetails.getTripDetails().getBudget());
                                    SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, ApiConstants.Budget_spent, budgetDetails.getTripDetails().getTotalSpent());
                                    if (budgetDetails.getTripDetails().getBudget().startsWith("0")) {
                                        txtContinue.setEnabled(true);
                                        startActivity(BudgetScreenActivity.createIntent(ViewTripMembersActivity.this, userModel));
                                        finish();
//                                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, ApiConstants.TRIP_USERNAME, userModel.getFirst_name() + " "
//                                        + userModel.getMiddle_name() + " " + userModel.getLast_name());
                                    } else {
                                        txtContinue.setEnabled(true);
                                        startActivity(HomeActivity.createIntent(ViewTripMembersActivity.this, userModel, ""));
                                        finish();
                                    }
//                                startActivity(BudgetScreenActivity.createIntent(SelectUserActivity.this, userModel));
//                                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, ApiConstants.TRIP_USERNAME, userModel.getFirst_name() + " "
//                                        + userModel.getMiddle_name() + " " + userModel.getLast_name());
                                hideProgress();
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    txtContinue.setEnabled(true);
                                    hideProgress();
                                    Toast.makeText(ViewTripMembersActivity.this,"Oops some error occured, try after some time",Toast.LENGTH_LONG).show();
                                }
                            }));

                        } else {
                            txtContinue.setEnabled(false);
                            JsonObject jsonRequest = new JsonObject();
                            jsonRequest.addProperty("trip_id", userModel.getTrip_id());
                            jsonRequest.addProperty("first_name", userModel.getFirst_name());
                            jsonRequest.addProperty("middle_name", userModel.getMiddle_name());
                            jsonRequest.addProperty("last_name", userModel.getLast_name());
                            SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, "usertype", userModel.getType());
                            SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, "first_name", userModel.getFirst_name());
                            SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, "middle_name", userModel.getMiddle_name());
                            SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, "last_name", userModel.getLast_name());

                            ApiClient.getApiClient().getStudentBudgetDetails(jsonRequest, new CancelableCallback<StudentBudgetDetails>(new Callback<StudentBudgetDetails>() {
                                @Override
                                public void success(StudentBudgetDetails budgetDetails, Response response) {
                                    SharedPreferencesHandler.setStringValues(ViewTripMembersActivity.this, ApiConstants.Budget_spent, budgetDetails.getTripDetails().getTotalSpent());
                                    txtContinue.setEnabled(true);
                                    startActivity(HomeActivity.createIntent(ViewTripMembersActivity.this, userModel, ""));
                                    finish();
//                                startActivity(HomeActivity.createIntent(SelectUserActivity.this, userModel));
//                                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, ApiConstants.TRIP_USERNAME, userModel.getFirst_name() + " "
//                                        + userModel.getMiddle_name() + " " + userModel.getLast_name());
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    txtContinue.setEnabled(true);
                                }
                            }));
                        }
                    } else {
                        txtContinue.setEnabled(true);
                        CommonUtils.toast("Please select at least one user.");
                    }
                }

                break;
            case R.id.cancel_button:
                etUserSearch.setText("");
                for (int i = 0; i < userList.size(); i++) {
                    userList.get(i).setChecked(false);
                }
                mAdapter.notifyDataSetChanged();
                userModel = null;
                break;
        }
    }


    @Override
    public void OnUserClicked(UserModel.TripsEntity.UserEntity userModel) {
        this.userModel = userModel;
    }
}
