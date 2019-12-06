package com.bethel.base;

import android.content.Context;

import com.bethel.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by krishan on 27-09-2016.
 */

public class Application extends android.app.Application {
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    public static Context getContext() {
        return mContext;
    }
}
