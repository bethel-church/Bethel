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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bethel.R;
import com.bethel.adapter.CustomUserAdapter;
import com.bethel.adapter.SelectUserAdapter;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.model.CurrencyModel;
import com.bethel.model.CurrencyModel1;
import com.bethel.model.Example;
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

public class SelectUserActivity extends BaseActivity implements CustomUserAdapter.OnUserClickListener {

    private static final String USER_TYPE = "com.user.list";
    @BindView(R.id.recyclerTrips)
    ListView recyclerTrips;
    @BindView(R.id.txtContinue)
    TextView txtContinue;
    @BindView(R.id.top_label)
    TextView mTopLabel;
    ArrayList<UserModel.TripsEntity.UserEntity> userList = new ArrayList<>();
    CustomUserAdapter mAdapter;
    @BindView(R.id.usersearchet)
    EditText etUserSearch;
    UserModel.TripsEntity.UserEntity userModel;
    @BindView(R.id.cancel_button)
    TextView tvCancel;

    public static Intent createIntent(Context context, List<UserModel.TripsEntity.UserEntity> user) {
        Intent intent = new Intent(context, SelectUserActivity.class);
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
      //  recyclerTrips.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CustomUserAdapter(SelectUserActivity.this, userList);
        recyclerTrips.setAdapter(mAdapter);

        etUserSearch.clearFocus();
        recyclerTrips.requestFocus();
        mTopLabel.setText(Html.fromHtml(getResources().getString(R.string.hey_welcome)));
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

        mGetCurrenciesCallBack = new CancelableCallback<>(new Callback<CurrencyModel>() {

            @Override
            public void success(CurrencyModel genricModel, Response response) {

                for (int i = 0; i < genricModel.getTrips().size(); i++) {
                    ApiConstants.SELECTED_CURRENCYLIST.add(genricModel.getTrips().get(i).getCurrency().getCurrency());
                }


                try{
                    ArrayList<CurrencyModel1> currencyModel1List = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(readFromAssets(SelectUserActivity.this,"country_code.txt"));
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


    }

    @Override
    public int getLayout() {
        return R.layout.activity_select_user;
    }


    @OnClick({R.id.txtContinue, R.id.cancel_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtContinue:
                getTripCurrencies();

                break;
            case R.id.cancel_button:
                etUserSearch.setText("");
                for (int i = 0; i < userList.size(); i++) {
                    userList.get(i).setChecked(false);
                }
                mAdapter.notifyDataSetChanged();
                userModel = null;
                if(getCurrentFocus()!=null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                break;
        }
    }

    private void onUserSelection() {
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
                    SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "usertype", userModel.getType());
                    SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "first_name", userModel.getFirst_name());
                    SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "middle_name", userModel.getMiddle_name());
                    SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "last_name", userModel.getLast_name());

                    ApiClient.getApiClient().getBudgetDetails(jsonRequest, new CancelableCallback<Example>(new Callback<Example>() {
                        @Override
                        public void success(Example budgetDetails, Response response) {
                            txtContinue.setEnabled(true);
                            SharedPreferencesHandler.setStringValues(SelectUserActivity.this, ApiConstants.Budget_Total, budgetDetails.getTripDetails().getBudget());
                            SharedPreferencesHandler.setStringValues(SelectUserActivity.this, ApiConstants.Budget_spent, budgetDetails.getTripDetails().getTotalSpent());
                            if (budgetDetails.getTripDetails().getBudget().startsWith("0")) {
                                txtContinue.setEnabled(true);
                                startActivity(BudgetScreenActivity.createIntent(SelectUserActivity.this, userModel));
                                finish();
//                                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, ApiConstants.TRIP_USERNAME, userModel.getFirst_name() + " "
//                                        + userModel.getMiddle_name() + " " + userModel.getLast_name());
                            } else {
                                txtContinue.setEnabled(true);
                                startActivity(HomeActivity.createIntent(SelectUserActivity.this, userModel, ""));
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
                            Toast.makeText(SelectUserActivity.this,"Oops some error occured, try after some time",Toast.LENGTH_LONG).show();
                        }
                    }));

                } else {
                    txtContinue.setEnabled(false);
                    JsonObject jsonRequest = new JsonObject();
                    jsonRequest.addProperty("trip_id", userModel.getTrip_id());
                    jsonRequest.addProperty("first_name", userModel.getFirst_name());
                    jsonRequest.addProperty("middle_name", userModel.getMiddle_name());
                    jsonRequest.addProperty("last_name", userModel.getLast_name());
                    SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "usertype", userModel.getType());
                    SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "first_name", userModel.getFirst_name());
                    SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "middle_name", userModel.getMiddle_name());
                    SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "last_name", userModel.getLast_name());

                    ApiClient.getApiClient().getStudentBudgetDetails(jsonRequest, new CancelableCallback<StudentBudgetDetails>(new Callback<StudentBudgetDetails>() {
                        @Override
                        public void success(StudentBudgetDetails budgetDetails, Response response) {
                            SharedPreferencesHandler.setStringValues(SelectUserActivity.this, ApiConstants.Budget_spent, budgetDetails.getTripDetails().getTotalSpent());
                            txtContinue.setEnabled(true);
                            startActivity(HomeActivity.createIntent(SelectUserActivity.this, userModel, ""));
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

                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "usertype", userModel.getType());
                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "first_name", userModel.getFirst_name());
                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "middle_name", userModel.getMiddle_name());
                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "last_name", userModel.getLast_name());
                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "user_id", userModel.getId());
                SharedPreferencesHandler.setStringValues(SelectUserActivity.this, "created_date", userModel.getCreated());


            } else {
                txtContinue.setEnabled(true);
                CommonUtils.toast("Please select at least one user.");
            }
        }

    }


    @Override
    public void OnUserClicked(UserModel.TripsEntity.UserEntity userModel) {
        this.userModel = userModel;
    }


    private CancelableCallback<CurrencyModel> mGetCurrenciesCallBack;
    private void getTripCurrencies() {
        if (isNetworkAvailableWithNoDialog(this)) {
            showProgress();
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", SharedPreferencesHandler.getStringValues(SelectUserActivity.this, ApiConstants.TRIP_ID));

            ApiClient.getApiClient().getTripCurrencyList(jsonRequest, mGetCurrenciesCallBack);
        } else {
            // toast(getString(R.string.internet_not_available));
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
