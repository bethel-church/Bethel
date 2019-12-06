package com.bethel.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.adapter.CustomAdapter;
import com.bethel.adapter.SelectTripAdapter;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.model.TripModel;
import com.bethel.utils.SharedPreferencesHandler;
import com.google.gson.JsonObject;

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

public class ChooseTripActivity extends BaseActivity implements SelectTripAdapter.OnTripClickListener {
    private static String TRIP_TYPE = "trip_type";
    @BindView(R.id.recyclerTrips)
    ListView recyclerTrips;
    @BindView(R.id.txtContinue)
    TextView txtContinue;
    @BindView(R.id.tripsearchet)
    EditText etTripSearch;
    @BindView(R.id.cancel_button)
    TextView tvCancel;
    String tripType = "Mission Trips";
    CustomAdapter mAdapter;
    private CancelableCallback<TripModel> mCallBack;
    TripModel.TripsEntity tripsEntity;
    List<TripModel.TripsEntity> trips = new ArrayList<>();

    public static Intent createIntent(Context context, String tripType) {
        Intent intent = new Intent(context, ChooseTripActivity.class);
        intent.putExtra(TRIP_TYPE, tripType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        tripType = getIntent().getStringExtra(TRIP_TYPE);
        //recyclerTrips.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CustomAdapter(ChooseTripActivity.this, new ArrayList<TripModel.TripsEntity>());
        recyclerTrips.setAdapter(mAdapter);
        etTripSearch.addTextChangedListener(new TextWatcher() {
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
        etTripSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // your additional processing...
                    return true;
                } else {
                    return false;
                }
            }
        });
        getTripList();

    }

    private void getTripList() {
        mCallBack = new CancelableCallback<>(new Callback<TripModel>() {

            @Override
            public void success(TripModel TripModel, Response response) {
                hideProgress();

                if (TripModel.getStatus().equalsIgnoreCase(ApiConstants.SUCCESS)) {
                    setRecyclerList(TripModel.getTrips());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                toast(getString(R.string.something_went));
            }
        });
        if (isNetworkAvailable(this)) {
            showProgress();
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_category", tripType);
            ApiClient.getApiClient().getTripList(jsonRequest, mCallBack);
        } else {
            toast(getString(R.string.internet_not_available));
        }
    }

    private void setRecyclerList(List<TripModel.TripsEntity> trips) {
        this.trips = trips;
        mAdapter.updateList(trips);

    }

    @Override
    public int getLayout() {
        return R.layout.activity_choose_trip;
    }

    @OnClick(R.id.txtContinue)
    public void onClick() {

    }

    @OnClick({R.id.txtContinue, R.id.cancel_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtContinue:

                if (tripsEntity == null) {
                    toast("Please select at least one Trip");
                    return;
                }
                SharedPreferencesHandler.setStringValues(ChooseTripActivity.this, ApiConstants.TRIP_NAME, tripsEntity.getTrip().getName());
                Intent resultIntent = new Intent();
                resultIntent.putExtra("trip_obj", tripsEntity);
                setResult(RESULT_OK, resultIntent);
                finish();
                break;
            case R.id.cancel_button:
                if(getCurrentFocus()!=null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                etTripSearch.setText("");
                for (int i = 0; i < trips.size(); i++) {
                    trips.get(i).getTrip().setChecked(false);
                }
                mAdapter.notifyDataSetChanged();
                tripsEntity = null;
                break;
        }
    }

    @Override
    public void OnTripClicked(TripModel.TripsEntity tripsEntity) {
        try {
            this.tripsEntity = tripsEntity;
          /*  new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    mAdapter.notifyDataSetChanged()   ;
                }
            },2000);
*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
