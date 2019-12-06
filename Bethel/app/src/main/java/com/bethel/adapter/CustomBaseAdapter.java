package com.bethel.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class CustomBaseAdapter<T> extends BaseAdapter{
    private Context context;


    public CustomBaseAdapter(Context context) {
        // TODO Auto-generated constructor stub
        this.context = context;
        if (context == null)
            return;
    }

    /******************************************************************************
     * This method is used to setList containing ListArray<T>
     *
     * @param items
     *****************************************************************************/
    public abstract void setList(List<T> items);

    /******************************************************************************
     * This method is used to get the setListArray
     *
     * @return ListArray<T>
     *****************************************************************************/
    public abstract List<T> getList();

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public T getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        return convertView;
    }

    /******************************************************************************
     * This method is used to get the context
     *
     * @return {@link Context}
     *****************************************************************************/
    public Context getContext() {
        return context;
    }

    /******************************************************************************
     * This method is used to get the Fragment activity context
     *
     * @return {@link FragmentActivity}
     *****************************************************************************/
    public FragmentActivity activityContext() {
        return (FragmentActivity) context;
    }

}
