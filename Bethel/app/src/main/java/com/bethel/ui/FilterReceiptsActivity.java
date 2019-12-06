package com.bethel.ui;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bethel.R;
import com.bethel.adapter.FilterCategoryAdapter;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.interfaces.FilterCallback;
import com.bethel.interfaces.UpdateReceiptFilter;
import com.bethel.utils.SharedPreferencesHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class FilterReceiptsActivity extends BaseActivity implements FilterCallback {
    ListView filterListView;
    ToggleButton toggleButton,usertogglebtn;
    ArrayList<String>catList;
    String selectedCatValue;
    TextView tvSave;
    boolean dateFilterValue,showMyReceipts;
    LinearLayout enddatecontainerll,startdatecontainerll;
    TextView tvStartDate,tvEndDate;
    boolean isReset;

    Calendar myCalendar2 = Calendar.getInstance();
    private boolean isStartDate;
    private String strStartDate,strEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareViews();
    }

    private void prepareViews() {
        filterListView=(ListView)findViewById(R.id.cateogrylv);
        toggleButton=(ToggleButton)findViewById(R.id.togglebtn);
        usertogglebtn=(ToggleButton)findViewById(R.id.usertogglebtn);
        tvSave=(TextView)findViewById(R.id.save_label);
        enddatecontainerll=(LinearLayout)findViewById(R.id.enddatecontainerll);
        startdatecontainerll=(LinearLayout)findViewById(R.id.startdatecontainerll);
        tvStartDate=(TextView)findViewById(R.id.startdatetv);
        tvEndDate=(TextView)findViewById(R.id.enddatetv);
        isStartDate=true;
        updateLabel();
        isStartDate=false;
        updateLabel();


        if (SharedPreferencesHandler.getSharedPreferences(this).contains("showmyreceipts")) {
            if (SharedPreferencesHandler.getBooleanValues(this, "showmyreceipts")) {
                usertogglebtn.setChecked(true);
                showMyReceipts=true;
            }else{
                showMyReceipts=false;
                usertogglebtn.setChecked(false);
            }
        }else{
            showMyReceipts=false;
            usertogglebtn.setChecked(false);
        }
        if(SharedPreferencesHandler.getSharedPreferences(this).contains("alldate")){
            if(SharedPreferencesHandler.getBooleanValues(this,"alldate")){
                toggleButton.setChecked(true);
                enddatecontainerll.setVisibility(View.GONE);
                startdatecontainerll.setVisibility(View.GONE);
                dateFilterValue=true;
            }else{

                dateFilterValue=false;
                if(SharedPreferencesHandler.getStringValues(this,"startDate")!=null){
                    tvStartDate.setText(parseTodaysDate(SharedPreferencesHandler.getStringValues(this,"startDate")));
                    strStartDate=SharedPreferencesHandler.getStringValues(this,"startDate");
                }
                if(SharedPreferencesHandler.getStringValues(this,"endDate")!=null){
                    tvEndDate.setText(parseTodaysDate(SharedPreferencesHandler.getStringValues(this,"endDate")));
                    strEndDate=SharedPreferencesHandler.getStringValues(this,"endDate");
                }

                enddatecontainerll.setVisibility(View.VISIBLE);
                startdatecontainerll.setVisibility(View.VISIBLE);
                toggleButton.setChecked(false);
            }
        }else{
            dateFilterValue=true;
            enddatecontainerll.setVisibility(View.GONE);
            startdatecontainerll.setVisibility(View.GONE);
            toggleButton.setChecked(true);
        }

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                dateFilterValue=b;
                if(b){
                    enddatecontainerll.setVisibility(View.GONE);
                    startdatecontainerll.setVisibility(View.GONE);
                }else {
                    enddatecontainerll.setVisibility(View.VISIBLE);
                    startdatecontainerll.setVisibility(View.VISIBLE);
                }
            }
        });
        enddatecontainerll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStartDate=false;
                new DatePickerDialog(FilterReceiptsActivity.this, R.style.datepicker, date2, myCalendar2
                        .get(Calendar.YEAR), myCalendar2.get(Calendar.MONTH),
                        myCalendar2.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        startdatecontainerll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStartDate=true;
                new DatePickerDialog(FilterReceiptsActivity.this, R.style.datepicker, date2, myCalendar2
                        .get(Calendar.YEAR), myCalendar2.get(Calendar.MONTH),
                        myCalendar2.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        usertogglebtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                showMyReceipts=b;
            }
        });

        prepareCategoryList();

        if(SharedPreferencesHandler.getSharedPreferences(this).contains("selectcat")){
            selectedCatValue=SharedPreferencesHandler.getStringValues(this,"selectcat");
        }else{
            selectedCatValue="All";
        }

        FilterCategoryAdapter filterCategoryAdapter=new FilterCategoryAdapter(this,catList,selectedCatValue);
        filterListView.setAdapter(filterCategoryAdapter);

        if(getIntent().getExtras()!=null) {
            if (getIntent().getExtras().containsKey("isTripMember")) {
                SharedPreferencesHandler.setBooleanValues(FilterReceiptsActivity.this,"showmyreceipts",true);
                ((RelativeLayout)findViewById(R.id.filterviewrl)).setVisibility(View.GONE);
            }
        }

                tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesHandler.setStringValues(FilterReceiptsActivity.this,"selectcat",selectedCatValue);
                if(dateFilterValue){
                    SharedPreferencesHandler.setBooleanValues(FilterReceiptsActivity.this,"alldate",true);
                }else {
                    SharedPreferencesHandler.setBooleanValues(FilterReceiptsActivity.this,"alldate",false);
                    SharedPreferencesHandler.setStringValues(FilterReceiptsActivity.this,"startDate",strStartDate);
                    SharedPreferencesHandler.setStringValues(FilterReceiptsActivity.this,"endDate",strEndDate);
                }

                if(showMyReceipts){
                    SharedPreferencesHandler.setBooleanValues(FilterReceiptsActivity.this,"showmyreceipts",true);
                }else {
                    SharedPreferencesHandler.setBooleanValues(FilterReceiptsActivity.this,"showmyreceipts",false);
                }

                SharedPreferencesHandler.setBooleanValues(FilterReceiptsActivity.this,"isFilterSet",true);
                finish();
                if(getIntent().getExtras()!=null){
                    if(getIntent().getExtras().containsKey("isTripMember")){
                        ((UpdateReceiptFilter)ViewMembersTrips.getInstance()).applyReceipts();
                    }else{
                        ((UpdateReceiptFilter)ViewReceiptsActivity.getInstance()).applyReceipts();
                    }
                }else{
                    ((UpdateReceiptFilter)ViewReceiptsActivity.getInstance()).applyReceipts();
                }

            }
        });

        ((TextView)findViewById(R.id.resetfiltertv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isReset=true;
                selectedCatValue="All";
                SharedPreferencesHandler.setStringValues(FilterReceiptsActivity.this,"selectcat",selectedCatValue);
                FilterCategoryAdapter filterCategoryAdapter=new FilterCategoryAdapter(FilterReceiptsActivity.this,catList,selectedCatValue);
                filterListView.setAdapter(filterCategoryAdapter);
                dateFilterValue=true;
               /* if(dateFilterValue){
                    SharedPreferencesHandler.setBooleanValues(FilterReceiptsActivity.this,"alldate",false);
                }else {*/
                SharedPreferencesHandler.setBooleanValues(FilterReceiptsActivity.this,"alldate",true);
                //}
                toggleButton.setChecked(dateFilterValue);
                if(getIntent().getExtras()!=null) {
                    if (!getIntent().getExtras().containsKey("isTripMember")) {
                        ((UpdateReceiptFilter) ViewReceiptsActivity.getInstance()).applyReceipts();
                    }
                }else{
                    ((UpdateReceiptFilter) ViewReceiptsActivity.getInstance()).applyReceipts();
                }
                SharedPreferencesHandler.setBooleanValues(FilterReceiptsActivity.this,"showmyreceipts",false);
                usertogglebtn.setChecked(false);

            }
        });

        if (SharedPreferencesHandler.getStringValues(FilterReceiptsActivity.this,"usertype").equalsIgnoreCase(ApiConstants.USERTYPE_LEADER)) {
            ((RelativeLayout)findViewById(R.id.filterviewrl)).setVisibility(View.VISIBLE);
        }else {
            ((RelativeLayout)findViewById(R.id.filterviewrl)).setVisibility(View.GONE);
        }

        if(getIntent().getExtras()!=null) {
            if (getIntent().getExtras().containsKey("isTripMember")) {
                SharedPreferencesHandler.setBooleanValues(FilterReceiptsActivity.this,"showmyreceipts",true);
                ((RelativeLayout)findViewById(R.id.filterviewrl)).setVisibility(View.GONE);
            }
        }
    }

    DatePickerDialog.OnDateSetListener date2 = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar2.set(Calendar.YEAR, year);
            myCalendar2.set(Calendar.MONTH, monthOfYear);
            myCalendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };


    private void updateLabel() {

        String myFormat = "yyyy-MM-dd hh:mm a"; //In which you need put here
        String wsMyFormat = "MMM d, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(wsMyFormat);
        SimpleDateFormat sdfMyFormat = new SimpleDateFormat(myFormat);
        if(isStartDate){
            tvStartDate.setText(sdf.format(myCalendar2.getTime()));
            strStartDate = sdfMyFormat.format(myCalendar2.getTime());
        }else {
            strEndDate = sdfMyFormat.format(myCalendar2.getTime());
            tvEndDate.setText(sdf.format(myCalendar2.getTime()));
        }
//        tvDate.setText(sdf.format(myCalendar2.getTime()));

        //   Log.e("date", strDate);
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("isTripMember")) {
                if (isReset) {
                    setResult(100);
                    finish();
                } else {
                    setResult(101);
                    finish();
                }

            } else {
                super.onBackPressed();
            }
        }else {
            super.onBackPressed();
        }
    }

    public  String parseTodaysDate(String time) {



        String inputPattern = "yyyy-MM-dd hh:mm a";

        String outputPattern = "MMM d, yyyy";

        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);

            Log.i("mini", "Converted Date Today:" + str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }


    private void prepareCategoryList() {
        catList=new ArrayList<>();
        catList.add("All");
        catList.add("Food");
        catList.add("Baggage/Visa/Departure Tax");
        catList.add("Airport Fees");
        catList.add("Transportation");
        catList.add("Lodging");
        catList.add("Supplies");
        catList.add("Missions $25 per person (Not 2nd Year)");
        catList.add("Gifts/Donations");
        catList.add("Other Expenses");
    }

    @Override
    public int getLayout() {
        return R.layout.activity_filters;
    }

    @Override
    public void filterSelection(String value,boolean add) {
        if (add) {
            if (value.equalsIgnoreCase("All")) {
                selectedCatValue = value;
            } else {
                if (selectedCatValue.contains("All")) {
                    selectedCatValue = "";
                }
                if (selectedCatValue.length() > 0) {
                    selectedCatValue = selectedCatValue + "," + value;
                } else {
                    selectedCatValue = value;
                }
            }
        } else {
            final List<String> items = new LinkedList<String>(Arrays.asList(selectedCatValue.split("\\s*,\\s*")));

            if (items.contains(value)) {
                items.remove(value);
                selectedCatValue = "";
                for (int i = 0; i < items.size(); i++) {
                    if (i == 0) {
                        selectedCatValue = items.get(i);
                    } else {
                        selectedCatValue = selectedCatValue + "," + items.get(i);
                    }
                }
            }
        }
    }


}
