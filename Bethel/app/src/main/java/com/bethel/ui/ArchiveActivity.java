package com.bethel.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.database.AppDatabase;
import com.bethel.utils.SharedPreferencesHandler;

public class ArchiveActivity extends BaseActivity{

    TextView tvLabel,tvContact,tvLogout;
    @Override
    public int getLayout() {
        return R.layout.archive_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvLabel=(TextView)findViewById(R.id.labeltv);
        tvContact=(TextView)findViewById(R.id.contactemailtv);
        String labelText="This trip has been <b>Archived</b>.if you feel that more receipts need to " +
                "entered for this trip, email";
        tvLabel.setText(Html.fromHtml(labelText));
        String strEmail="<u>missions@ibethel.org</u>";
        tvContact.setText(Html.fromHtml(strEmail));
        tvContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","missions@ibethel.org", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
        tvLogout=(TextView)findViewById(R.id.logout_btn);
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                logoutDialog();
            }
        });
    }
    private void logoutDialog() {
        final AppDatabase appDatabase = new AppDatabase(this);

        Cursor cursor = appDatabase.getRowCount(SharedPreferencesHandler.getStringValues(this, ApiConstants.TRIP_ID));



            new AlertDialog.Builder(ArchiveActivity.this)
                    .setMessage(
                            "Are you sure you want to log out?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    ApiConstants.LEADER_SELECTED_CURRENCYLIST.clear();
                                    ApiConstants.SELECTED_CURRENCYLIST.clear();
                                    SharedPreferencesHandler.setStringValues(ArchiveActivity.this, ApiConstants.TRIP_USERNAME, "");
                                    SharedPreferencesHandler.setStringValues(ArchiveActivity.this, ApiConstants.TRIP_ID, "");
                                    SharedPreferencesHandler.setStringValues(ArchiveActivity.this, ApiConstants.TRIP_NAME, "");
                                    SharedPreferencesHandler.setStringValues(ArchiveActivity.this, ApiConstants.TRIP_NAME, "");
                                    SharedPreferencesHandler.setStringValues(ArchiveActivity.this, "user_id", "");

                                    SharedPreferencesHandler.clearObject(ArchiveActivity.this, ApiConstants.PREF_LOGIN_MODEL);
                                    finish();
                                    startActivity(new Intent(ArchiveActivity.this, WelcomeActivity.class));
                                }
                            })
                    .setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                        }
                    }).show();

    }

}
