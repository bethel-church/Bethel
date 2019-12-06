package com.bethel.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import com.bethel.R;

import static com.bethel.base.Application.getContext;

/**
 * Created by kuljeetsingh on 9/30/16.
 */

public class CommonUtils
{

    /**
     * check if n/w is available or not
     *
     * @return is connected or not
     */
    public static boolean isNetworkAvailable(Activity activity)
    {
        ConnectivityManager connectivity = (ConnectivityManager) getContext().getSystemService
                (Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
            {
                for (NetworkInfo anInfo : info)
                {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }

        }
        internetAlert(activity);
        return false;
    }

    public static boolean isNetworkAvailableWithNoDialog(Activity activity)
    {
        ConnectivityManager connectivity = (ConnectivityManager) getContext().getSystemService
                (Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
            {
                for (NetworkInfo anInfo : info)
                {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    static void internetAlert(Activity activity){
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.datepicker));
        builder.setMessage("You don't seem to be connected to the Internet. Please get connected and try again.");
        builder.setTitle("No Internet").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    /**
     * @param message message to be displayed in Toast.
     */
    public static void longtoast(@NonNull String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * @param message message to be displayed in Toast.
     */
    public static void longtoast(@StringRes int message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * @param message message to be displayed in Toast.
     */
    public static void toast(@NonNull String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static ProgressDialog initIndeterminateDialog(Context context)
    {
        try
        {
            ProgressDialog dialog = new ProgressDialog(context, R.style.IndeterminateDialog);
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            return dialog;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static void showDialog(ProgressDialog dialog)
    {
        try
        {

            if (dialog != null && !dialog.isShowing())
            {
                dialog.show();
            }
        }
        catch (Exception ex)
        {
        }
    }

    public static void dismissDialog(Dialog dialog)
    {
        if (dialog != null && dialog.isShowing())
        {
            try
            {
                dialog.dismiss();
            }
            catch (Exception ex)
            {
            }
        }
    }

}
