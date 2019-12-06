package com.bethel.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.model.TripModel;
import com.bethel.model.UserModel;
import com.bethel.utils.SharedPreferencesHandler;
import com.google.gson.JsonObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.bethel.utils.CommonUtils.isNetworkAvailable;
import static com.bethel.utils.CommonUtils.toast;


public class LoginActivity extends BaseActivity {
    private static String TRIP_TYPE = "trip_type";
    private final int CHOOSE_TRIP = 1001;

    int[] imageArray = {R.drawable.a, R.drawable.b,
            R.drawable.c, R.drawable.d,
            R.drawable.e, R.drawable.f};
    Animation fadeIn;
    @BindView(R.id.imgGallery)
    ImageView imgGallery;
    @BindView(R.id.txtChooseTrip)
    TextView txtChooseTrip;
    @BindView(R.id.txt_mission_selected)
    TextView txtMissionSelected;

    @BindView(R.id.txtPassword)
    EditText txtPassword;
    @BindView(R.id.activity_splash)
    RelativeLayout rlSplash;
    String tripType = "Mission Trips";
    String tripId = "";
    private CancelableCallback<UserModel> mCallBack;
    int imagePostion;
    private static String mSelectedTrip;
    public static Intent createIntent(Context context, String tripType,String selectedTrip) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(TRIP_TYPE, tripType);
        intent.putExtra(ApiConstants.SELECTED_TRIP,selectedTrip);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);
        tripType = getIntent().getStringExtra(TRIP_TYPE);
        if(getIntent().hasExtra(ApiConstants.SELECTED_TRIP))
        {
            mSelectedTrip = getIntent().getStringExtra(ApiConstants.SELECTED_TRIP);
            txtChooseTrip.setText(mSelectedTrip);
        }

        txtMissionSelected.setText(tripType);
        imgGallery.setImageResource(imageArray[imagePostion]);
        animate(imgGallery,imageArray,0,true);
        mCallBack = new CancelableCallback<>(new Callback<UserModel>() {
            @Override
            public void success(UserModel userModel, Response response) {
                hideProgress();
                if (userModel.getStatus().equalsIgnoreCase(ApiConstants.SUCCESS)) {
//                    setRecyclerList(userModel.getTrips());
                    SharedPreferencesHandler.setStringValues(LoginActivity.this, ApiConstants.TRIP_ID, tripId);
                    SharedPreferencesHandler.writeObjectOnSharedPreference(LoginActivity.this,ApiConstants.PREF_LOGIN_MODEL,userModel);

                    if (userModel.getTrips().getUser().size() > 0) {
                        startActivity(SelectUserActivity.createIntent(LoginActivity.this, userModel.getTrips().getUser()));
                        finish();
                    }
                    else
                        toast("No user is registered in this Trip.");
                } else {
                    toast(userModel.getMessage());
                }
            }
            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                toast(getString(R.string.something_went));
            }
        });
    }
    private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {
        //imageView <-- The View which displays the images
        //images[] <-- Holds R references to the images to display
        //imageIndex <-- index of the first image to show in images[]
        //forever <-- If equals true then after the last image it starts all over again with the first image resulting in an infinite loop. You have been warned.
        int fadeInDuration = 500; // Configure time values here
        int timeBetween = 200;
        int fadeOutDuration = 4000;

        imageView.setVisibility(View.INVISIBLE);    //Visible or invisible by default - this will apply when the animation ends
  //      imageView.setImageResource(images[imageIndex]);

       /* Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);
*/
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false); // change to false
    //    animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        imageView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (images.length - 1 > imageIndex) {
                    animate(imageView, images, imageIndex + 1,forever); //Calls itself until it gets to the end of the array
                }
                else {
                    if (forever == true){
                        animate(imageView, images, 0,forever);  //Calls itself to start the animation all over again in a loop if forever = true
                    }
                }
            }
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                if ( imageIndex+1<images.length) {
                    rlSplash.setBackgroundResource(images[imageIndex + 1]);
                }else{
                    rlSplash.setBackgroundResource(images[0]);
                }
                imageView.setImageDrawable(ContextCompat.getDrawable(LoginActivity.this,images[imageIndex]));

            }
        });
    }
    @Override
    public int getLayout() {
        return R.layout.activity_login;
    }

    @OnClick({R.id.txtChooseTrip, R.id.txtLogin,R.id.txt_mission_selected})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtChooseTrip:
                startActivityForResult(ChooseTripActivity.createIntent(LoginActivity.this, tripType), CHOOSE_TRIP);
                break;
            case R.id.txtLogin:

                validateUser();
                break;
            case R.id.txt_mission_selected:
                startActivity(WelcomeActivity.createIntent(LoginActivity.this,mSelectedTrip));
                finish();
                break;
        }
    }

    private void validateUser() {
           if (isNetworkAvailable(this)) {
               if (TextUtils.isEmpty(tripId)) {
                   toast("Please select the Trip.");
               }else if (TextUtils.isEmpty(txtPassword.getText().toString())) {
                   toast("Passcode should not be empty.");
               }else {
                   showProgress();
                   JsonObject jsonRequest = new JsonObject();
                   jsonRequest.addProperty("trip_id", tripId);
                   jsonRequest.addProperty("passcode", txtPassword.getText().toString());
                   ApiClient.getApiClient().getLoginData(jsonRequest, mCallBack);
               }
           } else {
               toast(getString(R.string.internet_not_available));
           }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_TRIP && resultCode == RESULT_OK) {
            TripModel.TripsEntity tripsEntity = data.getExtras().getParcelable("trip_obj");
            if (tripsEntity != null) {
                txtChooseTrip.setText(tripsEntity.getTrip().getName());
                mSelectedTrip = tripsEntity.getTrip().getName();
                tripId = tripsEntity.getTrip().getId();
            }
        }
    }
}
