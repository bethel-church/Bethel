package com.bethel.ui;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.constants.ApiConstants;
import com.bethel.database.AppDatabase;
import com.bethel.interfaces.UpdateReceiptFilter;
import com.bethel.interfaces.UpdateSaveReceiptCallback;
import com.bethel.utils.SharedPreferencesHandler;

import butterknife.ButterKnife;

public class ViewReceiptsActivity extends FragmentActivity implements UpdateReceiptFilter,UpdateSaveReceiptCallback {

    //TABS
    private  String TAB_ALL = "Uploaded";
    private final String TAB_MOST_ACTIVE = "Saved For Later";
    //LISTENERS
    private TabHost.OnTabChangeListener mTabChangeListener;
    private String mCurrentTab;
    static Context context;
    SavedReceiptsFragment savedReceiptsFragment;
    UploadedReceiptsFragment uploadedReceiptsFragment;
    public boolean isClickAvailable=false;

    public static Context getInstance(){
        return context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;

        ButterKnife.bind(this);
        setContentView(R.layout.view_receipts);
        ((TextView)findViewById(R.id.filterlabel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startActivity(new Intent(ViewReceiptsActivity.this, FilterReceiptsActivity.class));
            }
        });
        prepareTabs();

    }
    /**
     * Prepare All the Tabs on top bar
     */
    TabHost mTabHost;
    private void prepareTabs() {

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        // Setting Up Tabs
        mTabHost.setup();
        initializeActionBarTabs(mTabHost);

        AppDatabase appDatabase=new AppDatabase(this);
        Cursor cursor=appDatabase.getRowCount(SharedPreferencesHandler.getStringValues(this, ApiConstants.TRIP_ID));

        updateSavedTabsCount(cursor.getCount());
        cursor.close();
        appDatabase.close();
    }
    TextView tvNews, tvSocial;
    /**
     * Setup tab icons and content views.
     */
    public void initializeActionBarTabs(TabHost mTabHost) {
        TabHost.TabSpec mTabSpec;

        mCurrentTab=TAB_ALL;
        /* Setup tab icons and content views.. */
        mTabSpec = mTabHost.newTabSpec(TAB_ALL);
        mTabSpec.setContent(new FragmentTabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        });
        View socialTab = LayoutInflater.from(this).inflate(
                R.layout.group_tabs, null);
        tvSocial = (TextView) socialTab
                .findViewById(R.id.tab_icon_title);
        tvSocial.setText(TAB_ALL);
        final View bottomBarView=(View)socialTab.findViewById(R.id.bottomtabbar);
        bottomBarView.setBackgroundColor(Color.parseColor("#37575d"));
        bottomBarView.setVisibility(View.VISIBLE);
        mTabSpec.setIndicator(socialTab);
        mTabHost.addTab(mTabSpec);


        mTabSpec = mTabHost.newTabSpec(TAB_MOST_ACTIVE);
        mTabSpec.setContent(new FragmentTabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        });
        View newsTab = LayoutInflater.from(this).inflate(
                R.layout.group_tabs, null);
        tvNews = (TextView) newsTab.findViewById(R.id.tab_icon_title);
        tvNews.setText(TAB_MOST_ACTIVE);
        final View bottomBarMySchduleView=(View)newsTab.findViewById(R.id.bottomtabbar);
        bottomBarMySchduleView.setBackgroundColor(Color.parseColor("#37575d"));
        bottomBarMySchduleView.setVisibility(View.GONE);
//        changeTabColor(tvNews);
        mTabSpec.setIndicator(newsTab);
        mTabHost.addTab(mTabSpec);

        // POSTS TAB
       /* mTabSpec = mTabHost.newTabSpec(TAB_MOST_ACTIVE);
       */
        mTabSpec.setContent(new FragmentTabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        });
        //bottomBarView.setVisibility(View.GONE);

		/* Comes here when user switch tab, or we do programmatically */
        mTabChangeListener = new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                    if (tabId.toString().equalsIgnoreCase("Uploaded")) {
                        bottomBarMySchduleView.setVisibility(View.GONE);
                        bottomBarView.setVisibility(View.VISIBLE);
                        pushFragments(new UploadedReceiptsFragment());
                        mCurrentTab = "uploaded";
                    } else {
                        bottomBarMySchduleView.setVisibility(View.VISIBLE);
                        bottomBarView.setVisibility(View.GONE);
                        pushFragments(savedReceiptsFragment);
                        mCurrentTab = "saved";
                }
            }
        };
//        mTabHost.setVisibility(View.GONE);
        mTabHost.setOnTabChangedListener(mTabChangeListener);
//        mTabHost.setOnTabChangedListener(null);
        uploadedReceiptsFragment=new UploadedReceiptsFragment();
        savedReceiptsFragment=new SavedReceiptsFragment();
        pushFragments(uploadedReceiptsFragment);
        mCurrentTab="uploaded";
    }

   public void setTabChangeListener(){
       mTabHost.setOnTabChangedListener(mTabChangeListener);
   }

    @Override
    protected void onResume() {
        super.onResume();
       /* if(uploadedReceiptsFragment!=null){
            if(uploadedReceiptsFragment.isVisible()){
                uploadedReceiptsFragment.fetchReceipts();
            }
        }else{
            savedReceiptsFragment.fetchReceipts();
        }*/
    }

    public void pushFragments(Fragment fragment) {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
       /* if (shouldAnimate) {
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        }*/
        ft.replace(R.id.container, fragment);
        ft.commitAllowingStateLoss();
    }

    private int uploadedCount;
    public void addSaveCount(){
        uploadedCount=uploadedCount+1;
        tvSocial.setText("Uploaded("+(uploadedCount)+")");
    }
    public void updateTabsCount(int count){
        uploadedCount=count;
        tvSocial.setText("Uploaded("+count+")");
    }
      public  void  updateSavedTabsCount(int count){
        tvNews.setText("Saved For Later("+count+")");
    }

    @Override
    public void applyReceipts() {
        if(mCurrentTab.equalsIgnoreCase("saved")){
            savedReceiptsFragment.applyFilters();
        }else{
            uploadedReceiptsFragment.applyFilters();
        }
    }



    @Override
    public void updateSaveFragment() {
            savedReceiptsFragment.fetchReceipts();
    }
}
