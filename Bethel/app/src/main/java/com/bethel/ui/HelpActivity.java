package com.bethel.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bethel.R;

public class HelpActivity extends Activity{

    private CheckBox checkBox;
    private Button btnGotit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);
        prepareViews();
    }

    private void prepareViews() {
        checkBox=(CheckBox)findViewById(R.id.checkbox);
        btnGotit=(Button)findViewById(R.id.gotitbtn);
        ((TextView)findViewById(R.id.canceltv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SharedPreferences sp = getSharedPreferences("betheltrips", Activity.MODE_PRIVATE);
        if(sp.getBoolean("show", true)){
            checkBox.setChecked(false);
        }else{
            ((LinearLayout)findViewById(R.id.checkboxll)).setVisibility(View.GONE);
            checkBox.setChecked(true);
        }

        if(getIntent().getExtras()!=null){
            if(getIntent().getExtras().getBoolean("fromCamera")){
                ((LinearLayout)findViewById(R.id.checkboxll)).setVisibility(View.GONE);
            }
        }

        btnGotit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences("betheltrips", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                if(checkBox.isChecked()){
                    editor.putBoolean("show", false);
                }else{
                    editor.putBoolean("show", true);
                }
                editor.commit();
                finish();
                startActivity(new Intent(HelpActivity.this,CameraActivity.class));
            }
        });


    }
}
