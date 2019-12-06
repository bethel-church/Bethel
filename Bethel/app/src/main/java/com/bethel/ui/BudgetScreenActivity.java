package com.bethel.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.model.GenricModel;
import com.bethel.model.UserModel;
import com.bethel.utils.CommonUtils;
import com.google.gson.JsonObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.bethel.utils.CommonUtils.isNetworkAvailable;
import static com.bethel.utils.CommonUtils.toast;

public class BudgetScreenActivity extends BaseActivity {

    private static final String USER_TYPE = "user.type.model";
    @BindView(R.id.top_label)
    TextView topLabel;
    @BindView(R.id.txtAmount)
    EditText txtAmount;
    @BindView(R.id.select_currency)
    TextView selectCurrency;
    @BindView(R.id.txtContinue)
    TextView txtContinue;
    @BindView(R.id.display_currencies)
    TextView displayCurrency;

    UserModel.TripsEntity.UserEntity userModel;
    private CancelableCallback<GenricModel> mCallBack;
//    private CancelableCallback<CurrencyModel> mSelectedCurrencyCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);
        userModel = (UserModel.TripsEntity.UserEntity) getIntent().getSerializableExtra(USER_TYPE);
        topLabel.setText(Html.fromHtml(getResources().getString(R.string.hey_user).replace("****", userModel.getFirst_name())));


        mCallBack = new CancelableCallback<>(new Callback<GenricModel>() {

            @Override
            public void success(GenricModel genricModel, Response response) {
                hideProgress();

                if (genricModel.getStatus().equalsIgnoreCase(ApiConstants.SUCCESS)) {
                    ApiConstants.SELECTED_CURRENCYLIST.clear();
                    startActivity(HomeActivity.createIntent(BudgetScreenActivity.this, userModel, txtAmount.getText().toString()));
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

    public static Intent createIntent(Context context, UserModel.TripsEntity.UserEntity userModel) {
        Intent intent = new Intent(context, BudgetScreenActivity.class);
        intent.putExtra(USER_TYPE, userModel);
        return intent;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (isNetworkAvailable()) {

            if (TextUtils.isEmpty(userModel.getTrip_id())) {
                CommonUtils.toast("Please select trip.");
                return;
            }

//            showProgress();
//            JsonObject jsonRequest = new JsonObject();
//            jsonRequest.addProperty("trip_id", userModel.getTrip_id());
//            ApiClient.getApiClient().getTripCurrencyList(jsonRequest, mSelectedCurrencyCallBack);
            if(ApiConstants.SELECTED_CURRENCYLIST.size() > 0)
            {
                selectCurrency.setText("Edit Currencies");
                displayCurrency.setVisibility(View.VISIBLE);
                displayCurrency.setText(TextUtils.join(",", ApiConstants.SELECTED_CURRENCYLIST));
            }else{
                displayCurrency.setVisibility(View.GONE);
                selectCurrency.setText("Select Currencies");
            }
//        } else {
//            toast(getString(R.string.internet_not_available));
//        }


    }

    @Override
    public int getLayout() {
        return R.layout.activity_seetings_screen;
    }

    @OnClick({R.id.txtContinue, R.id.select_currency})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_currency:
                startActivity(new Intent(BudgetScreenActivity.this, CurrencyList.class));
                break;
            case R.id.txtContinue:
                setBudget();
                break;
        }
    }

    private void setBudget() {

        if (isNetworkAvailable(this)) {

            if (TextUtils.isEmpty(txtAmount.getText().toString())) {
                CommonUtils.toast("Please enter trip amount.");
                return;
            }
            showProgress();
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", userModel.getTrip_id());
            jsonRequest.addProperty("trip_budget", txtAmount.getText().toString());
            ApiClient.getApiClient().setTripBudget(jsonRequest, mCallBack);
        } else {
            toast(getString(R.string.internet_not_available));
        }

    }


}
