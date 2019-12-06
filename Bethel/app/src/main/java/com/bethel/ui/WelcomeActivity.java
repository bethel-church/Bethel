package com.bethel.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bethel.R;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.model.UserModel;
import com.bethel.utils.SharedPreferencesHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WelcomeActivity extends BaseActivity {
    int[] imageArray = {R.drawable.a, R.drawable.b,
            R.drawable.c, R.drawable.d,
            R.drawable.e, R.drawable.f};
    Animation fadeIn;
    @BindView(R.id.imgGallery)
    ImageView imgGallery;
    int imagePostion;
    private static String mSelectedTrip = "";
    @BindView(R.id.activity_splash)
    RelativeLayout rlSplash;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* if(SharedPreferencesHandler.getBooleanValues(this,"isLoggedIn")){

        }*/
        ButterKnife.bind(this);
        //imgGallery.setImageResource(imageArray[imagePostion]);
        if(getIntent().hasExtra(ApiConstants.SELECTED_TRIP)){
            mSelectedTrip = getIntent().getStringExtra(ApiConstants.SELECTED_TRIP);
    }
        animate(imgGallery,imageArray,0,true);



        UserModel userModel =(UserModel) SharedPreferencesHandler.readObjectFromSharedPreference(WelcomeActivity.this, ApiConstants.PREF_LOGIN_MODEL);
        String user_id = SharedPreferencesHandler.getStringValues(WelcomeActivity.this,"user_id");
                if(user_id != null && !user_id.equalsIgnoreCase("")) {
                    startActivity(new Intent(this,HomeActivity.class));
                    finish();
                }

    }
    public static Intent createIntent(Context context, String selectedTrip) {
//        mSelectedTrip = selectedTrip;
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.putExtra(ApiConstants.SELECTED_TRIP, selectedTrip);
        return intent;
    }

    private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {

        //imageView <-- The View which displays the images
        //images[] <-- Holds R references to the images to display
        //imageIndex <-- index of the first image to show in images[]
        //forever <-- If equals true then after the last image it starts all over again with the first image resulting in an infinite loop. You have been warned.

        int fadeInDuration = 2000; // Configure time values here
        int timeBetween = 3000;
        int fadeOutDuration = 4000;

        imageView.setVisibility(View.INVISIBLE);    //Visible or invisible by default - this will apply when the animation ends
        //rlSplash.setBackground(ContextCompat.getDrawable(this,images[imageIndex]));
//        imageView.setImageDrawable(ContextCompat.getDrawable(this,images[imageIndex]));

      /*  Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);
*/
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false); // change to false
       // animation.addAnimation(fadeIn);
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
                        animate(imageView, images, 0,forever);  //Calls itself to start the animation
                        // all over again in a loop if forever = true
                    }
                }
            }
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
               /* if (images.length - 1 > imageIndex) {
                    rlSplash.setBackground(ContextCompat.getDrawable(WelcomeActivity.this,images[imageIndex+1]));
//                    imageView.setImageResource(images[imageIndex-1]);
//                    imageView.setImageResource(images[imageIndex+1]);
               }else{
//                    imageView.setImageResource(images[0]);
                    rlSplash.setBackground(ContextCompat.getDrawable(WelcomeActivity.this,images[0]));
                }*/
                if ( imageIndex+1<images.length) {
                    rlSplash.setBackgroundResource(images[imageIndex + 1]);
                }else{
                    rlSplash.setBackgroundResource(images[0]);
                }
                imageView.setImageDrawable(ContextCompat.getDrawable(WelcomeActivity.this,images[imageIndex]));

            }
        });
    }

    @Override
    public int getLayout() {
        return R.layout.activity_welcome;
    }

//    Handler handler = new Handler();
//    Runnable runnable = new Runnable() {
//        int i = 0;
//
//        public void run() {
//
//            imgGallery.setImageResource(imageArray[i]);
//            fadeIn = AnimationUtils.loadAnimation(WelcomeActivity.this, R.anim.fade_in);
//            imgGallery.startAnimation(fadeIn);
//            fadeIn.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    Animation fadeOut = AnimationUtils.loadAnimation(WelcomeActivity.this, R.anim.fade_out);
//                    imgGallery.startAnimation(fadeOut);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//                }
//            });
//            i++;
//            if (i > imageArray.length - 1) {
//                i = 0;
//            }
//            handler.postDelayed(this, 5000);
//        }
//    };

    @OnClick({R.id.mission_trip, R.id.choose_second_travel })
    public void onClick(View view) {

        UserModel userModel =(UserModel) SharedPreferencesHandler.readObjectFromSharedPreference(WelcomeActivity.this, ApiConstants.PREF_LOGIN_MODEL);
        String user_id = SharedPreferencesHandler.getStringValues(WelcomeActivity.this,"user_id");
        switch (view.getId()) {
            case R.id.mission_trip:
                if(userModel != null && user_id != null && !user_id.equalsIgnoreCase("")) {
                    startActivity(SelectUserActivity.createIntent(WelcomeActivity.this, userModel.getTrips().getUser()));
                    finish();
                }else{
                    startActivity(LoginActivity.createIntent(WelcomeActivity.this, "Mission Trips",mSelectedTrip));
                    finish();
                }
                break;
            case R.id.choose_second_travel:
                if(userModel != null && user_id != null && !user_id.equalsIgnoreCase("")) {
                    startActivity(SelectUserActivity.createIntent(WelcomeActivity.this, userModel.getTrips().getUser()));
                    finish();
                }else{


                        startActivity(LoginActivity.createIntent(WelcomeActivity.this, "2nd Year Travel",mSelectedTrip));
                        finish();
//                    startActivity(LoginActivity.createIntent(WelcomeActivity.this, "Mission Trips"));
                }
                break;
        }
    }
}
