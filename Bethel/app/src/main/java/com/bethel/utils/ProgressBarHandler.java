package com.bethel.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bethel.R;

public class ProgressBarHandler {
    private ProgressDialog mProgressBar;

    public ProgressBarHandler(Context context) {

        ViewGroup layout = (ViewGroup) ((Activity) context).findViewById(android.R.id.content).getRootView();

        mProgressBar = new ProgressDialog(context);
        //mProgressBar.setIndeterminate(true);
       mProgressBar.setMessage("Please wait...");
        mProgressBar.setCancelable(false);


        /*mProgressBar.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.MULTIPLY);

        RelativeLayout.LayoutParams params = new
                RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        RelativeLayout rl = new RelativeLayout(context);

        rl.setGravity(Gravity.CENTER);
        rl.addView(mProgressBar);
        //rl.setClickable(true);
        layout.addView(rl, params);

        */hide();
    }

    public void show() {

       /* if(mProgressBar.getVisibility()!=View.VISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
        }*/
        mProgressBar.show();
    }

    public void hide() {
        mProgressBar.hide();
    }
}