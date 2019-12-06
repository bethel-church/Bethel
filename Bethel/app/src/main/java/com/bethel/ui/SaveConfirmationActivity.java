package com.bethel.ui;


import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.base.BaseActivity;
import com.bethel.utils.SharedPreferencesHandler;

public class SaveConfirmationActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckBox checkBox=(CheckBox)findViewById(R.id.dontshowcheckbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    SharedPreferencesHandler.setBooleanValues(SaveConfirmationActivity.this, "saveconfirm", true);
                }else{
                    SharedPreferencesHandler.setBooleanValues(SaveConfirmationActivity.this, "saveconfirm", false);
                }
            }
        });

        TextView gotItBtn=(TextView)findViewById(R.id.gotitbtn);
        gotItBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public int getLayout() {
        return R.layout.offlinereceiptsubmission;
    }
}
