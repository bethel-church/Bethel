package com.bethel.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;

import com.bethel.api.RestService;
import com.bethel.constants.ApiConstants;
import com.bethel.utils.ProgressBarHandler;

import butterknife.ButterKnife;
import retrofit.RestAdapter;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseActivity extends AppCompatActivity {

    public Context context;
    private ProgressBarHandler progress;
    private RestService restService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        this.context = this;
    }

    //get layout to inflate

    public abstract
    @LayoutRes
    int getLayout();

    public void showProgress() {
        if(progress == null){
            progress = new ProgressBarHandler(context);
        }
        progress.show();
    }

    public void hideProgress() {
        if (progress!=null)
        progress.hide();
    }

   /* public RestService getRestService() {
        if (restService == null) {
            RestAdapter radapter = new RestAdapter.
                    Builder().
                    setLogLevel(RestAdapter.LogLevel.FULL).
                    setEndpoint(ApiConstants.BASE_URL).build();

            restService = radapter.create(RestService.class);
        }
        return restService;
    }
*/
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}